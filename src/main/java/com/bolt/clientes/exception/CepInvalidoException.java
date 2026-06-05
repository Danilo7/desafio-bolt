package com.bolt.clientes.exception;

/**
 * Lançada quando o CEP informado não é encontrado no ViaCEP ou é inválido.
 *
 * Estende RuntimeException (exceção "não checada"): não obriga try/catch em
 * todo lugar. Quem trata centralmente é o GlobalExceptionHandler, que converte
 * em uma resposta HTTP 422 com mensagem amigável.
 */
public class CepInvalidoException extends RuntimeException {

    public CepInvalidoException(String cep) {
        super("CEP inválido ou não encontrado: " + cep);
    }
}
