package com.bolt.clientes.dto;

import com.bolt.clientes.domain.Endereco;

/**
 * Endereço como ele SAI da API (já com os dados preenchidos pelo ViaCEP).
 *
 * O método estático "from" é uma fábrica que converte a entidade Endereco
 * neste DTO — assim a regra de conversão fica num só lugar.
 */
public record EnderecoResponse(
        String cep,
        String logradouro,
        String bairro,
        String cidade,
        String uf,
        String numero,
        String complemento
) {
    public static EnderecoResponse from(Endereco e) {
        if (e == null) {
            return null;
        }
        return new EnderecoResponse(
                e.getCep(), e.getLogradouro(), e.getBairro(), e.getCidade(),
                e.getUf(), e.getNumero(), e.getComplemento()
        );
    }
}
