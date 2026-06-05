package com.bolt.clientes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Corpo da requisição para CADASTRAR ou ATUALIZAR um cliente (POST/PUT).
 *
 * As anotações de validação (@NotBlank, @NotEmpty...) são checadas
 * automaticamente pelo Spring quando o controller marca o parâmetro com @Valid.
 * Se algo falhar, o GlobalExceptionHandler devolve 400 com a lista de erros.
 */
public record ClienteRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O documento é obrigatório")
        String documento,

        @NotNull(message = "O endereço do cliente é obrigatório")
        @Valid
        EnderecoRequest endereco,

        // Pelo menos uma unidade consumidora é exigida.
        @NotEmpty(message = "É necessário informar ao menos uma unidade consumidora")
        @Valid
        List<UnidadeConsumidoraRequest> unidadesConsumidoras
) {
}
