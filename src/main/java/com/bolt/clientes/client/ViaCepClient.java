package com.bolt.clientes.client;

import com.bolt.clientes.exception.CepInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente de integração com o ViaCEP (https://viacep.com.br).
 *
 * Responsabilidade única: dado um CEP, consultar a API e devolver os dados de
 * endereço. Isolar a integração aqui facilita testar o resto do sistema
 * (basta "mockar" este componente) e trocar a API no futuro sem afetar o
 * service.
 *
 * @Component registra a classe como um bean gerenciado pelo Spring, para que
 * possa ser injetada no service.
 */
@Component
public class ViaCepClient {

    private static final Logger log = LoggerFactory.getLogger(ViaCepClient.class);

    private final RestClient restClient;

    /**
     * Injeção de dependência via construtor (forma recomendada no Spring):
     * o Spring entrega o bean "viaCepRestClient" criado em RestClientConfig.
     */
    public ViaCepClient(RestClient viaCepRestClient) {
        this.restClient = viaCepRestClient;
    }

    /**
     * Consulta o endereço de um CEP.
     *
     * @param cep CEP com 8 dígitos (com ou sem traço).
     * @return dados do endereço retornados pelo ViaCEP.
     * @throws CepInvalidoException se o CEP for nulo/curto ou não existir.
     */
    public ViaCepResponse buscarEndereco(String cep) {
        String cepLimpo = limparCep(cep);
        if (cepLimpo.length() != 8) {
            throw new CepInvalidoException(cep);
        }

        log.debug("Consultando ViaCEP para o CEP {}", cepLimpo);

        // GET https://viacep.com.br/ws/{cep}/json/ -> desserializa em ViaCepResponse.
        ViaCepResponse resposta = restClient.get()
                .uri("/{cep}/json/", cepLimpo)
                .retrieve()
                .body(ViaCepResponse.class);

        // O ViaCEP devolve HTTP 200 com {"erro": true} quando o CEP não existe.
        if (resposta == null || Boolean.TRUE.equals(resposta.erro())) {
            throw new CepInvalidoException(cep);
        }

        return resposta;
    }

    /** Remove tudo que não for dígito (traços, pontos, espaços). */
    private String limparCep(String cep) {
        if (cep == null) {
            return "";
        }
        return cep.replaceAll("\\D", "");
    }
}
