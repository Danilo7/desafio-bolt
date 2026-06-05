package com.bolt.clientes.exception;

/**
 * Regra: não é permitido cadastrar dois clientes com o mesmo documento.
 * Vira HTTP 409 (Conflict).
 */
public class DocumentoDuplicadoException extends RuntimeException {

    public DocumentoDuplicadoException(String documento) {
        super("Já existe um cliente cadastrado com o documento: " + documento);
    }
}
