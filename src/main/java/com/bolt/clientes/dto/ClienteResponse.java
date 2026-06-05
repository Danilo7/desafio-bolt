package com.bolt.clientes.dto;

import com.bolt.clientes.domain.Cliente;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cliente como ele SAI da API (resposta dos endpoints GET/POST/PUT).
 *
 * Inclui os campos de auditoria (createdAt/updatedAt/ativo) exigidos pelo
 * desafio. A conversão a partir da entidade fica no método "from".
 */
public record ClienteResponse(
        Long id,
        String nome,
        String documento,
        EnderecoResponse endereco,
        List<UnidadeConsumidoraResponse> unidadesConsumidoras,
        boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClienteResponse from(Cliente c) {
        // Converte cada unidade da entidade para o DTO correspondente.
        List<UnidadeConsumidoraResponse> unidades = c.getUnidadesConsumidoras().stream()
                .map(UnidadeConsumidoraResponse::from)
                .toList();

        return new ClienteResponse(
                c.getId(),
                c.getNome(),
                c.getDocumento(),
                EnderecoResponse.from(c.getEndereco()),
                unidades,
                c.isAtivo(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
