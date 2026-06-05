package com.bolt.clientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação (equivale ao index.php do Laravel ou ao
 * manage.py/wsgi.py do Django).
 *
 * A anotação @SpringBootApplication ativa três comportamentos:
 *  - @Configuration: esta classe pode definir beans (componentes gerenciados).
 *  - @EnableAutoConfiguration: o Spring configura automaticamente Tomcat, JPA,
 *    JSON, etc. com base nas dependências do pom.xml.
 *  - @ComponentScan: o Spring varre o pacote "com.bolt.clientes" e seus
 *    subpacotes procurando componentes (@RestController, @Service, @Repository...)
 *    para registrar e injetar onde forem necessários (injeção de dependência).
 */
@SpringBootApplication
public class ClientesApplication {

    public static void main(String[] args) {
        // Sobe o servidor embarcado (Tomcat) e inicializa todo o contexto Spring.
        SpringApplication.run(ClientesApplication.class, args);
    }
}
