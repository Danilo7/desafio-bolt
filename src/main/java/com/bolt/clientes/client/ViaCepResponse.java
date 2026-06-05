package com.bolt.clientes.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa a resposta JSON da API do ViaCEP.
 *
 * É um "record" (Java 14+): uma classe imutável e enxuta para carregar dados,
 * com getters, equals/hashCode e construtor gerados automaticamente. Parecido
 * com uma dataclass do Python.
 *
 * @JsonIgnoreProperties(ignoreUnknown = true): o ViaCEP retorna outros campos
 * (ibge, gia, ddd...) que não usamos; isso evita erro de desserialização.
 *
 * Exemplo de retorno do ViaCEP para 30130-010:
 * {
 *   "cep": "30130-010", "logradouro": "Rua dos Carijós",
 *   "bairro": "Centro", "localidade": "Belo Horizonte", "uf": "MG"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
        String cep,
        String logradouro,
        String bairro,
        @JsonProperty("localidade") String cidade, // no ViaCEP a cidade chama "localidade"
        String uf,
        // Quando o CEP não existe, o ViaCEP retorna {"erro": "true"} em vez dos dados.
        Boolean erro
) {
}
