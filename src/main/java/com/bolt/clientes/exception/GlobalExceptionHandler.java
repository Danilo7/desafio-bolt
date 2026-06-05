package com.bolt.clientes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Tratador GLOBAL de exceções.
 *
 * @RestControllerAdvice intercepta exceções lançadas por qualquer controller e
 * as converte em respostas HTTP padronizadas (ApiError em JSON). Assim os
 * controllers e services ficam limpos: eles só lançam a exceção certa e o
 * mapeamento para status HTTP acontece aqui, num único lugar.
 *
 * Mapeamento das regras de negócio para status HTTP:
 *  - Documento duplicado / unidade já vinculada -> 409 Conflict
 *  - Região não atendida / CEP inválido         -> 422 Unprocessable Entity
 *  - Cliente não encontrado                      -> 404 Not Found
 *  - Falha de validação (@Valid)                 -> 400 Bad Request
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleNaoEncontrado(ClienteNaoEncontradoException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({DocumentoDuplicadoException.class, UnidadeJaVinculadaException.class})
    public ResponseEntity<ApiError> handleConflito(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({RegiaoNaoAtendidaException.class, CepInvalidoException.class})
    public ResponseEntity<ApiError> handleRegraNegocio(RuntimeException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /**
     * Disparada quando a validação dos DTOs (@Valid) falha. Coletamos a mensagem
     * de cada campo inválido na lista "details".
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacao(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erro de validação nos dados enviados",
                erros
        );
        return ResponseEntity.badRequest().body(error);
    }

    /** Rede de segurança: qualquer erro inesperado vira 500 sem vazar stack trace. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenerico(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado: " + ex.getMessage());
    }

    /** Monta a resposta de erro padronizada (sem lista de detalhes). */
    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        ApiError error = new ApiError(status.value(), status.getReasonPhrase(), message, null);
        return ResponseEntity.status(status).body(error);
    }
}
