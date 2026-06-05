package com.bolt.clientes.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Dados de endereço que CHEGAM na requisição.
 *
 * Repare: o usuário só informa CEP, número e (opcionalmente) complemento.
 * Logradouro, bairro, cidade e UF são preenchidos automaticamente pela
 * aplicação consultando o ViaCEP — é uma regra de negócio do desafio.
 *
 * @NotBlank valida que o campo não é nulo nem vazio (string em branco).
 */
public record EnderecoRequest(
        @NotBlank(message = "O CEP é obrigatório")
        String cep,

        String numero,

        String complemento
) {
}
