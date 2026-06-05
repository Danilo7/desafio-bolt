package com.bolt.clientes.exception;

/**
 * Regra: uma unidade consumidora (numeroInstalacao) não pode ser cadastrada
 * para clientes diferentes. Vira HTTP 409 (Conflict).
 */
public class UnidadeJaVinculadaException extends RuntimeException {

    public UnidadeJaVinculadaException(String numeroInstalacao) {
        super("A unidade consumidora com número de instalação " + numeroInstalacao
                + " já está vinculada a um cliente.");
    }
}
