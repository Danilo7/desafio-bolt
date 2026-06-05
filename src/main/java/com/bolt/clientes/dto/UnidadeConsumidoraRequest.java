package com.bolt.clientes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dados de uma unidade consumidora que chegam na requisição.
 *
 * @Valid em "endereco" faz a validação "descer" para dentro do objeto aninhado
 * (validação em cascata) — sem isso o @NotBlank do CEP não seria checado aqui.
 */
public record UnidadeConsumidoraRequest(
        @NotBlank(message = "O nome da unidade é obrigatório")
        String nome,

        @NotBlank(message = "O número de instalação é obrigatório")
        String numeroInstalacao,

        @NotNull(message = "O endereço da unidade é obrigatório")
        @Valid
        EnderecoRequest endereco
) {
}
