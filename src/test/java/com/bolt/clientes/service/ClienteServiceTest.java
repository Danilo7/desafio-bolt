package com.bolt.clientes.service;

import com.bolt.clientes.client.ViaCepClient;
import com.bolt.clientes.client.ViaCepResponse;
import com.bolt.clientes.domain.Cliente;
import com.bolt.clientes.dto.ClienteRequest;
import com.bolt.clientes.dto.EnderecoRequest;
import com.bolt.clientes.dto.UnidadeConsumidoraRequest;
import com.bolt.clientes.event.ClienteMgEvent;
import com.bolt.clientes.exception.DocumentoDuplicadoException;
import com.bolt.clientes.exception.RegiaoNaoAtendidaException;
import com.bolt.clientes.exception.UnidadeJaVinculadaException;
import com.bolt.clientes.mapper.EnderecoMapper;
import com.bolt.clientes.repository.ClienteRepository;
import com.bolt.clientes.repository.UnidadeConsumidoraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes UNITÁRIOS do ClienteService — focam nas REGRAS DE NEGÓCIO.
 *
 * "Unitário" = testamos a classe isolada; suas dependências (repositórios,
 * ViaCEP, publicador de eventos) são MOCKADAS com Mockito. Assim não tocamos
 * banco nem rede, e os testes ficam rápidos e determinísticos.
 *
 *  @ExtendWith(MockitoExtension.class) -> liga o Mockito ao JUnit 5.
 *  @Mock        -> cria um dublê da dependência.
 *  @InjectMocks -> instancia o service injetando os mocks acima.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UnidadeConsumidoraRepository unidadeRepository;
    @Mock
    private EnderecoMapper enderecoMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ClienteService service;

    // ---- Helpers de construção de request ----------------------------------

    /** Monta um request de cliente (sem mexer em mocks). */
    private ClienteRequest request(String documento, String uf) {
        EnderecoRequest endereco = new EnderecoRequest("30130-010", "100", null);
        UnidadeConsumidoraRequest unidade =
                new UnidadeConsumidoraRequest("Minha casa", "INST-" + uf, endereco);
        return new ClienteRequest("Fulano", documento, endereco, List.of(unidade));
    }

    /** Configura o EnderecoMapper mockado para devolver um endereço com a UF dada. */
    private void mapperRetornaUf(String uf) {
        com.bolt.clientes.domain.Endereco endereco = new com.bolt.clientes.domain.Endereco();
        endereco.setUf(uf);
        endereco.setCep("30130-010");
        when(enderecoMapper.toEntity(any(EnderecoRequest.class))).thenReturn(endereco);
    }

    // ========================================================================
    //  Regra 1: documento único
    // ========================================================================
    @Test
    void deveLancarErroAoCadastrarDocumentoDuplicado() {
        when(clienteRepository.existsByDocumento("123")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(request("123", "MG")))
                .isInstanceOf(DocumentoDuplicadoException.class);

        verify(clienteRepository, never()).save(any());
    }

    // ========================================================================
    //  Regra 2: unidade já vinculada a outro cliente
    // ========================================================================
    @Test
    void deveLancarErroQuandoUnidadeJaVinculada() {
        when(clienteRepository.existsByDocumento(any())).thenReturn(false);
        mapperRetornaUf("MG");
        when(unidadeRepository.existsByNumeroInstalacao(any())).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(request("123", "MG")))
                .isInstanceOf(UnidadeJaVinculadaException.class);
    }

    // ========================================================================
    //  Regra 5: estados não atendidos (SP, RS, PR)
    // ========================================================================
    @Test
    void deveBloquearCadastroEmEstadoNaoAtendido() {
        when(clienteRepository.existsByDocumento(any())).thenReturn(false);
        mapperRetornaUf("SP");
        when(unidadeRepository.existsByNumeroInstalacao(any())).thenReturn(false);

        assertThatThrownBy(() -> service.cadastrar(request("123", "SP")))
                .isInstanceOf(RegiaoNaoAtendidaException.class);
    }

    // ========================================================================
    //  Regra 6: cliente em MG publica evento no tópico
    // ========================================================================
    @Test
    void devePublicarEventoQuandoUnidadeEmMg() {
        when(clienteRepository.existsByDocumento(any())).thenReturn(false);
        mapperRetornaUf("MG");
        when(unidadeRepository.existsByNumeroInstalacao(any())).thenReturn(false);
        // O save devolve o próprio cliente recebido (simula persistência).
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.cadastrar(request("123", "MG"));

        // Captura o evento publicado e confirma que é um ClienteMgEvent.
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(ClienteMgEvent.class);
    }

    @Test
    void naoDevePublicarEventoQuandoForaDeMg() {
        when(clienteRepository.existsByDocumento(any())).thenReturn(false);
        mapperRetornaUf("RJ");
        when(unidadeRepository.existsByNumeroInstalacao(any())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.cadastrar(request("123", "RJ"));

        verify(eventPublisher, never()).publishEvent(any());
    }
}
