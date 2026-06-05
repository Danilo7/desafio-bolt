package com.bolt.clientes.mapper;

import com.bolt.clientes.client.ViaCepClient;
import com.bolt.clientes.client.ViaCepResponse;
import com.bolt.clientes.domain.Endereco;
import com.bolt.clientes.dto.EnderecoRequest;
import org.springframework.stereotype.Component;

/**
 * Converte um EnderecoRequest (que traz só o CEP + número/complemento) em uma
 * entidade Endereco COMPLETA, consultando o ViaCEP para obter logradouro,
 * bairro, cidade e UF.
 *
 * Esta classe concentra a regra "endereços devem ser consultados via ViaCEP".
 * Mantê-la separada do service deixa o service focado nas regras de negócio.
 */
@Component
public class EnderecoMapper {

    private final ViaCepClient viaCepClient;

    public EnderecoMapper(ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    /**
     * Monta um Endereco a partir do request, preenchendo os dados via ViaCEP.
     */
    public Endereco toEntity(EnderecoRequest request) {
        ViaCepResponse via = viaCepClient.buscarEndereco(request.cep());

        Endereco endereco = new Endereco();
        // Dados vindos do ViaCEP (fonte da verdade para o endereço):
        endereco.setCep(via.cep());
        endereco.setLogradouro(via.logradouro());
        endereco.setBairro(via.bairro());
        endereco.setCidade(via.cidade());
        endereco.setUf(via.uf());
        // Dados que o ViaCEP não fornece, informados pelo usuário:
        endereco.setNumero(request.numero());
        endereco.setComplemento(request.complemento());
        return endereco;
    }
}
