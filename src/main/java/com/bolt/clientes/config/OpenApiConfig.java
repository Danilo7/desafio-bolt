package com.bolt.clientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da documentação OpenAPI/Swagger.
 *
 * Define o título, a descrição e a versão exibidos na interface do Swagger UI
 * (acessível em http://localhost:8082/swagger-ui.html). O springdoc faz o resto:
 * varre os @RestController e gera a documentação dos endpoints automaticamente.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("API de Cadastro de Clientes - Desafio Bolt")
                .description("Operações REST para cadastro, atualização, remoção e "
                        + "pesquisa de clientes, com integração ViaCEP e regras de negócio.")
                .version("1.0.0"));
    }
}
