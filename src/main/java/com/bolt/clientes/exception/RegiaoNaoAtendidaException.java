package com.bolt.clientes.exception;

/**
 * Regra: clientes com unidade consumidora em SP, RS ou PR não podem ser
 * cadastrados (não atendemos essas regiões). Vira HTTP 422 (Unprocessable Entity).
 */
public class RegiaoNaoAtendidaException extends RuntimeException {

    public RegiaoNaoAtendidaException(String uf) {
        super("Não atendemos a região do estado: " + uf
                + ". Estados não atendidos: SP, RS, PR.");
    }
}
