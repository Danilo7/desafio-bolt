package com.bolt.clientes.exception;

/** Lançada quando um cliente não existe ou foi logicamente removido. Vira HTTP 404. */
public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(Long id) {
        super("Cliente não encontrado para o id: " + id);
    }
}
