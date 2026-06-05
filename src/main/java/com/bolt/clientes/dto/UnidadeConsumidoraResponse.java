package com.bolt.clientes.dto;

import com.bolt.clientes.domain.UnidadeConsumidora;

/** Unidade consumidora como ela sai da API. */
public record UnidadeConsumidoraResponse(
        Long id,
        String nome,
        String numeroInstalacao,
        EnderecoResponse endereco
) {
    public static UnidadeConsumidoraResponse from(UnidadeConsumidora u) {
        return new UnidadeConsumidoraResponse(
                u.getId(),
                u.getNome(),
                u.getNumeroInstalacao(),
                EnderecoResponse.from(u.getEndereco())
        );
    }
}
