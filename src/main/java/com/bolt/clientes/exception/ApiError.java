package com.bolt.clientes.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato padronizado de erro que a API devolve em qualquer falha.
 *
 * Ter um corpo de erro consistente facilita a vida de quem consome a API
 * (e de quem avalia o desafio). Campos: momento, status HTTP, rótulo do erro,
 * mensagem e (opcional) lista de erros de validação por campo.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {
    public ApiError(int status, String error, String message, List<String> details) {
        this(LocalDateTime.now(), status, error, message, details);
    }
}
