# Desafio Técnico — API de Cadastro de Clientes (Grupo Bolt)

API REST para **cadastro, atualização, remoção e pesquisa de clientes**, desenvolvida em
**Java 17 + Spring Boot**, com integração ao **ViaCEP**, regras de negócio específicas,
remoção lógica e publicação de evento para clientes de MG.

> **Nota de transparência:** Java não é a minha stack principal (atuo com PHP/Python).
> Encarei este desafio como prova de resiliência e capacidade de aprender uma stack nova,
> apoiado por IA como ferramenta de produtividade. Por isso o projeto é **fortemente
> documentado**: há comentários didáticos no código e uma pasta [`docs/`](docs/) explicando
> cada camada e cada decisão técnica de ponta a ponta.

---

## Sumário
- [Tecnologias](#tecnologias)
- [Como compilar e executar](#como-compilar-e-executar)
- [Endpoints](#endpoints)
- [Regras de negócio](#regras-de-negócio)
- [Documentação interativa (Swagger)](#documentação-interativa-swagger)
- [Banco de dados (H2)](#banco-de-dados-h2)
- [Coleção Postman](#coleção-postman)
- [Testes](#testes)
- [Docker](#docker)
- [Justificativa das escolhas técnicas](#justificativa-das-escolhas-técnicas)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Documentação técnica detalhada](#documentação-técnica-detalhada)

---

## Tecnologias
- **Java 17**
- **Spring Boot 3.3.5** (Web, Data JPA, Validation)
- **JPA / Hibernate**
- **Maven**
- **H2** (banco em memória)
- **springdoc-openapi** (Swagger UI)
- **JUnit 5 + Mockito + MockMvc** (testes)

---

## Como compilar e executar

### Pré-requisitos
- **Java 17+** instalado
- **Maven 3.8+** instalado

> No macOS com Homebrew: `brew install openjdk@17 maven`.
> Garanta que o `JAVA_HOME` aponta para o JDK 17. Exemplo (Apple Silicon):
> ```bash
> export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
> ```

### Rodar a aplicação
```bash
# 1. Compilar e rodar os testes
mvn clean test

# 2. Subir a aplicação (porta 8082)
mvn spring-boot:run
```

A API ficará disponível em **http://localhost:8082**.

### Alternativa: gerar e rodar o .jar
```bash
mvn clean package
java -jar target/clientes-1.0.0.jar
```

---

## Endpoints

| Método | Rota | Descrição | Status sucesso |
|--------|------|-----------|----------------|
| `POST`   | `/api/clientes`          | Cadastrar cliente | `201 Created` |
| `PUT`    | `/api/clientes/{id}`     | Atualizar cliente | `200 OK` |
| `DELETE` | `/api/clientes/{id}`     | Remover (lógico)  | `204 No Content` |
| `GET`    | `/api/clientes`          | Listar todos (ativos) | `200 OK` |
| `GET`    | `/api/clientes/{id}`     | Obter por ID | `200 OK` |
| `GET`    | `/api/clientes/ultimos`  | Últimos 20 (decrescente) | `200 OK` |

### Exemplo de cadastro (POST `/api/clientes`)
```json
{
  "nome": "Maria Silva",
  "documento": "12345678900",
  "endereco": { "cep": "30130-010", "numero": "1500", "complemento": "Apto 302" },
  "unidadesConsumidoras": [
    {
      "nome": "Minha casa",
      "numeroInstalacao": "INST-0001",
      "endereco": { "cep": "30130-010", "numero": "1500" }
    }
  ]
}
```
> O endereço completo (logradouro, bairro, cidade, UF) é preenchido automaticamente a
> partir do CEP, consultando o ViaCEP. O cliente envia apenas CEP, número e complemento.

---

## Regras de negócio

| # | Regra | Onde está implementada | Resposta de erro |
|---|-------|------------------------|------------------|
| 1 | Documento não pode se repetir | `ClienteService.cadastrar/atualizar` + `unique` na entidade | `409 Conflict` |
| 2 | Unidade (nº instalação) não pode ser de clientes diferentes | `ClienteService.montarUnidade` + `unique` | `409 Conflict` |
| 3 | Endereços consultados via ViaCEP | `ViaCepClient` + `EnderecoMapper` | `422` se CEP inválido |
| 4 | Remoção lógica (soft delete) | `ClienteService.deletar` (seta `ativo=false`) | `204 No Content` |
| 5 | Bloqueio de SP, RS, PR | `ClienteService.montarUnidade` | `422 Unprocessable Entity` |
| 6 | Cliente em MG publica evento `analise_cliente_mg` | `ClienteService.publicarSeMg` + `ClienteMgEvent` | — (log do evento) |

---

## Documentação interativa (Swagger)
Com a aplicação rodando:
- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8082/v3/api-docs

---

## Banco de dados (H2)
Banco **em memória** (não persiste após desligar a app). Console web disponível em:
- **URL:** http://localhost:8082/h2-console
- **JDBC URL:** `jdbc:h2:mem:clientesdb`
- **Usuário:** `sa` · **Senha:** *(em branco)*

---

## Coleção Postman
O arquivo [`postman_collection.json`](postman_collection.json) contém todas as requisições
já configuradas (URLs, porta 8082, bodies). Importe no Postman e use a variável
`{{baseUrl}}` (já apontando para `http://localhost:8082`).

Inclui casos de sucesso e de erro (ex.: cadastro em SP que retorna `422`).

---

## Testes
```bash
mvn test
```
- **Unitários** (`ClienteServiceTest`): cobrem cada regra de negócio com mocks (Mockito).
- **Integração** (`ClienteControllerIntegrationTest`): exercitam a API ponta a ponta via
  MockMvc, com o ViaCEP mockado e banco H2.

---

## Docker
```bash
# Construir e subir
docker compose up --build
```
A aplicação ficará disponível em http://localhost:8082. (Não é necessário ter Java/Maven
instalados na máquina — o build acontece dentro do contêiner.)

---

## Justificativa das escolhas técnicas

- **Java 17 + Spring Boot:** stack exigida; Spring Boot reduz configuração (auto-config) e
  é o padrão de mercado para APIs REST.
- **H2 em memória:** recomendado no desafio; zero setup, ideal para avaliação rápida.
- **Spring ApplicationEvent (em vez de Kafka/RabbitMQ):** o desafio permite explicitamente
  essa abordagem. Mantém a solução **simples e sem infraestrutura externa**, alinhada ao
  valor de "simplicidade" citado pela Bolt. A troca por um broker real exigiria apenas
  reescrever o `ClienteMgEventListener`.
- **DTOs + Mapper:** separam o contrato da API do modelo de persistência, evitando expor
  entidades diretamente e permitindo validação na entrada.
- **`@RestControllerAdvice`:** centraliza o tratamento de erros, deixando controllers e
  services limpos e devolvendo respostas de erro padronizadas (`ApiError`).
- **springdoc-openapi:** documentação interativa automática (diferencial), útil para quem avalia.
- **RestClient:** cliente HTTP moderno do Spring 6 (substitui o RestTemplate) para o ViaCEP.

---

## Estrutura do projeto
```
src/main/java/com/bolt/clientes/
├── ClientesApplication.java      # ponto de entrada
├── controller/                   # camada HTTP (REST)
├── service/                      # regras de negócio
├── repository/                   # acesso a dados (Spring Data JPA)
├── domain/                       # entidades JPA (Cliente, UnidadeConsumidora, Endereco)
├── dto/                          # objetos de request/response
├── mapper/                       # conversão request -> entidade (com ViaCEP)
├── client/                       # integração ViaCEP
├── event/                        # evento MG + listener (tópico analise_cliente_mg)
├── exception/                    # exceptions de negócio + handler global
└── config/                       # OpenAPI e RestClient
```

---

## Documentação técnica detalhada
Para entender **a fundo** cada decisão e conceito (especialmente útil para quem vem de
outra stack), veja a pasta [`docs/`](docs/):

- [`docs/01-visao-geral.md`](docs/01-visao-geral.md) — arquitetura em camadas e fluxo de uma requisição
- [`docs/02-conceitos-java-spring.md`](docs/02-conceitos-java-spring.md) — glossário Java/Spring para quem vem de PHP/Python
- [`docs/03-regras-de-negocio.md`](docs/03-regras-de-negocio.md) — cada regra explicada com o código
- [`docs/04-decisoes-e-tradeoffs.md`](docs/04-decisoes-e-tradeoffs.md) — por que cada escolha e suas alternativas
- [`docs/05-roteiro-de-estudo.md`](docs/05-roteiro-de-estudo.md) — passo a passo guiado para estudar o código
```
