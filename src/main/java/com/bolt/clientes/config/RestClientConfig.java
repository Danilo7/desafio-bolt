package com.bolt.clientes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuração do cliente HTTP usado para chamar a API do ViaCEP.
 *
 * @Configuration marca a classe como fonte de "beans" (componentes que o
 * Spring cria e gerencia). O método anotado com @Bean devolve um objeto que
 * o Spring guarda no contêiner e injeta em quem precisar (ex.: ViaCepClient).
 *
 * RestClient é o cliente HTTP síncrono moderno do Spring (substitui o antigo
 * RestTemplate). Comparável ao Guzzle (PHP) ou requests (Python).
 */
@Configuration
public class RestClientConfig {

    /** Lê a propriedade "viacep.base-url" do application.yml. */
    @Value("${viacep.base-url}")
    private String viaCepBaseUrl;

    @Bean
    public RestClient viaCepRestClient() {
        return RestClient.builder()
                .baseUrl(viaCepBaseUrl)
                .build();
    }
}
