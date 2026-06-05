package com.bolt.clientes.service;

import com.bolt.clientes.domain.Cliente;
import com.bolt.clientes.domain.Endereco;
import com.bolt.clientes.domain.UnidadeConsumidora;
import com.bolt.clientes.dto.ClienteRequest;
import com.bolt.clientes.dto.UnidadeConsumidoraRequest;
import com.bolt.clientes.event.ClienteMgEvent;
import com.bolt.clientes.exception.ClienteNaoEncontradoException;
import com.bolt.clientes.exception.DocumentoDuplicadoException;
import com.bolt.clientes.exception.RegiaoNaoAtendidaException;
import com.bolt.clientes.exception.UnidadeJaVinculadaException;
import com.bolt.clientes.mapper.EnderecoMapper;
import com.bolt.clientes.repository.ClienteRepository;
import com.bolt.clientes.repository.UnidadeConsumidoraRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Camada de SERVIÇO — onde vivem as regras de negócio do desafio.
 *
 * O controller só recebe HTTP e delega para cá; o repositório só fala com o
 * banco. Concentrar as regras aqui mantém a separação de responsabilidades e
 * facilita testar (basta mockar repositórios e ViaCEP).
 *
 * @Service marca a classe como um bean de serviço gerenciado pelo Spring.
 */
@Service
public class ClienteService {

    /** Estados onde NÃO atendemos (regra de negócio). Set = busca rápida. */
    private static final Set<String> ESTADOS_NAO_ATENDIDOS = Set.of("SP", "RS", "PR");

    /** Estado que dispara a publicação no tópico analise_cliente_mg. */
    private static final String ESTADO_ANALISE = "MG";

    private final ClienteRepository clienteRepository;
    private final UnidadeConsumidoraRepository unidadeRepository;
    private final EnderecoMapper enderecoMapper;
    private final ApplicationEventPublisher eventPublisher;

    // Injeção de dependências por construtor: o Spring entrega todos os beans.
    public ClienteService(ClienteRepository clienteRepository,
                          UnidadeConsumidoraRepository unidadeRepository,
                          EnderecoMapper enderecoMapper,
                          ApplicationEventPublisher eventPublisher) {
        this.clienteRepository = clienteRepository;
        this.unidadeRepository = unidadeRepository;
        this.enderecoMapper = enderecoMapper;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================================
    //  CRIAR
    // =========================================================================

    /**
     * Cadastra um novo cliente aplicando todas as regras de negócio.
     *
     * @Transactional: tudo dentro do método roda numa única transação. Se uma
     * regra falhar (lançar exceção), o banco faz rollback — nada é persistido
     * pela metade.
     */
    @Transactional
    public Cliente cadastrar(ClienteRequest request) {
        // Regra 1: documento único.
        if (clienteRepository.existsByDocumento(request.documento())) {
            throw new DocumentoDuplicadoException(request.documento());
        }

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setDocumento(request.documento());
        // Regra 3: endereço do cliente preenchido via ViaCEP.
        cliente.setEndereco(enderecoMapper.toEntity(request.endereco()));

        // Monta e valida cada unidade consumidora.
        for (UnidadeConsumidoraRequest uReq : request.unidadesConsumidoras()) {
            cliente.addUnidade(montarUnidade(uReq));
        }

        Cliente salvo = clienteRepository.save(cliente);

        // Regra 6: se alguma unidade for de MG, publica no tópico analise_cliente_mg.
        publicarSeMg(salvo);

        return salvo;
    }

    // =========================================================================
    //  ATUALIZAR
    // =========================================================================

    @Transactional
    public Cliente atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarAtivoOuFalhar(id);

        // Se o documento mudou, garante que o novo não pertence a outro cliente.
        if (!cliente.getDocumento().equals(request.documento())
                && clienteRepository.existsByDocumento(request.documento())) {
            throw new DocumentoDuplicadoException(request.documento());
        }

        cliente.setNome(request.nome());
        cliente.setDocumento(request.documento());
        cliente.setEndereco(enderecoMapper.toEntity(request.endereco()));

        // Substitui as unidades. orphanRemoval=true remove do banco as antigas.
        cliente.getUnidadesConsumidoras().clear();
        for (UnidadeConsumidoraRequest uReq : request.unidadesConsumidoras()) {
            cliente.addUnidade(montarUnidade(uReq));
        }

        Cliente salvo = clienteRepository.save(cliente);
        publicarSeMg(salvo);
        return salvo;
    }

    // =========================================================================
    //  DELETAR (soft delete)
    // =========================================================================

    /**
     * Regra 4: remoção lógica. Não apagamos a linha; apenas marcamos ativo=false.
     */
    @Transactional
    public void deletar(Long id) {
        Cliente cliente = buscarAtivoOuFalhar(id);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    // =========================================================================
    //  CONSULTAS
    // =========================================================================

    /** Lista todos os clientes ativos. readOnly otimiza a transação de leitura. */
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return buscarAtivoOuFalhar(id);
    }

    /** Funcionalidade: últimos 20 clientes em ordem decrescente. */
    @Transactional(readOnly = true)
    public List<Cliente> listarUltimos20() {
        return clienteRepository.findTop20ByAtivoTrueOrderByIdDesc();
    }

    // =========================================================================
    //  MÉTODOS AUXILIARES (privados)
    // =========================================================================

    /**
     * Monta uma UnidadeConsumidora a partir do request, aplicando as regras de
     * unicidade e de região.
     */
    private UnidadeConsumidora montarUnidade(UnidadeConsumidoraRequest req) {
        // Regra 2: número de instalação não pode estar vinculado a outro cliente.
        if (unidadeRepository.existsByNumeroInstalacao(req.numeroInstalacao())) {
            throw new UnidadeJaVinculadaException(req.numeroInstalacao());
        }

        // Regra 3: endereço da unidade via ViaCEP.
        Endereco endereco = enderecoMapper.toEntity(req.endereco());

        // Regra 5: bloqueia unidades em SP/RS/PR.
        String uf = endereco.getUf();
        if (uf != null && ESTADOS_NAO_ATENDIDOS.contains(uf.toUpperCase())) {
            throw new RegiaoNaoAtendidaException(uf);
        }

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setNome(req.nome());
        unidade.setNumeroInstalacao(req.numeroInstalacao());
        unidade.setEndereco(endereco);
        return unidade;
    }

    /** Busca um cliente ativo ou lança 404. Centraliza a checagem de existência. */
    private Cliente buscarAtivoOuFalhar(Long id) {
        return clienteRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    /**
     * Regra 6: se qualquer unidade do cliente estiver em MG, publica o evento
     * no tópico analise_cliente_mg.
     */
    private void publicarSeMg(Cliente cliente) {
        boolean temUnidadeMg = cliente.getUnidadesConsumidoras().stream()
                .map(u -> u.getEndereco().getUf())
                .filter(uf -> uf != null)
                .anyMatch(uf -> uf.equalsIgnoreCase(ESTADO_ANALISE));

        if (temUnidadeMg) {
            eventPublisher.publishEvent(
                    new ClienteMgEvent(cliente.getId(), cliente.getDocumento(), cliente.getNome())
            );
        }
    }
}
