# 05 — Roteiro de Estudo Guiado

Siga as etapas **na ordem**. Em cada uma: abra o arquivo, leia o que está indicado,
e só avance quando conseguir responder o **"✅ Cheque você mesmo"** sem olhar a resposta.
A resposta está logo abaixo, escondida pela frase "Resposta:" — tente antes de ler.

Tempo estimado total: ~1h30 com calma.

---

## Etapa 0 — O mapa antes do território (5 min)
**Leia:** [`docs/01-visao-geral.md`](01-visao-geral.md) inteiro.

Você não precisa decorar nada aqui, só formar a imagem mental:
`Controller → Service → (Repository + ViaCepClient + Evento) → Banco`.

> ✅ **Cheque você mesmo:** em qual camada moram as regras de negócio? E qual camada NÃO
> pode conter regra de negócio?
>
> Resposta: regras ficam no **Service**. O **Controller** não pode ter regra — ele só
> recebe HTTP, valida o corpo e delega.

---

## Etapa 1 — O ponto de entrada (5 min)
**Abra:** `ClientesApplication.java`

Entenda o que `@SpringBootApplication` liga (auto-config + component scan + configuração).
Esse é o "liga-desliga" da aplicação — o `main` sobe o Tomcat embarcado.

> ✅ **Cheque você mesmo:** por que não precisamos configurar um servidor web (Apache/Nginx)
> à parte?
>
> Resposta: o Spring Boot traz um **Tomcat embarcado**; o `SpringApplication.run` sobe o
> servidor junto com a aplicação. O `.jar` é autossuficiente.

---

## Etapa 2 — O modelo de dados (15 min) ⭐ base de tudo
**Abra nesta ordem:**
1. `domain/Endereco.java`
2. `domain/UnidadeConsumidora.java`
3. `domain/Cliente.java`

Foque em entender as anotações: `@Entity`, `@Id`/`@GeneratedValue`, `@Column` (repare em
`unique = true` no documento e no número de instalação), `@Embedded`/`@Embeddable`,
`@OneToMany` e os `@CreationTimestamp`/`@UpdateTimestamp`.

Apoio: tabela de anotações JPA em [`docs/02-conceitos-java-spring.md`](02-conceitos-java-spring.md).

> ✅ **Cheque você mesmo:** (a) por que `Endereco` é `@Embeddable` e não `@Entity`?
> (b) Onde fica a flag que implementa o soft delete?
>
> Resposta: (a) porque não queremos uma tabela separada de endereços — os campos são
> "achatados" como colunas de `cliente` e `unidade_consumidora`, e o mesmo formato é
> reutilizado nos dois. (b) o campo `boolean ativo` no `Cliente` (deletar = `ativo=false`).

---

## Etapa 3 — Como falamos com o banco (10 min)
**Abra:**
1. `repository/ClienteRepository.java`
2. `repository/UnidadeConsumidoraRepository.java`

O ponto-chave: são **interfaces vazias** que estendem `JpaRepository`. O Spring gera o
código. Os métodos `findByDocumento`, `findByAtivoTrue`,
`findTop20ByAtivoTrueOrderByIdDesc` viram SQL **pelo nome** (derived queries).

> ✅ **Cheque você mesmo:** sem escrever SQL, como o método
> `findTop20ByAtivoTrueOrderByIdDesc()` atende ao requisito "últimos 20 em ordem
> decrescente"?
>
> Resposta: o Spring traduz o nome em `WHERE ativo = true ORDER BY id DESC LIMIT 20`.
> `Top20` = limite, `OrderByIdDesc` = ordenação decrescente, `AtivoTrue` = só ativos.

---

## Etapa 4 — A integração externa (ViaCEP) (15 min)
**Abra nesta ordem:**
1. `client/ViaCepResponse.java`  ← *(o arquivo que você já abriu)*
2. `client/ViaCepClient.java`
3. `config/RestClientConfig.java`
4. `mapper/EnderecoMapper.java`

História: `RestClientConfig` cria o cliente HTTP (bean) → `ViaCepClient` faz o GET e trata
CEP inexistente → `ViaCepResponse` é o molde do JSON de resposta (repare no `@JsonProperty`
mapeando `localidade`→`cidade`, e no campo `erro`) → `EnderecoMapper` usa tudo isso para
montar a entidade `Endereco`.

> ✅ **Cheque você mesmo:** (a) o ViaCEP responde com qual status HTTP quando o CEP não
> existe, e como o código detecta isso? (b) Quais campos do endereço o usuário informa, e
> quais vêm do ViaCEP?
>
> Resposta: (a) responde **200 OK** com `{"erro": true}` — por isso checamos o campo
> `erro` (e não o status) e lançamos `CepInvalidoException`. (b) o usuário informa só
> **cep, número e complemento**; logradouro, bairro, cidade e UF vêm do ViaCEP.

---

## Etapa 5 — Os DTOs e a validação de entrada (10 min)
**Abra:**
1. `dto/ClienteRequest.java` (e `EnderecoRequest`, `UnidadeConsumidoraRequest`)
2. `dto/ClienteResponse.java` (e os outros `*Response`)

Entenda: requests trazem `@NotBlank`/`@NotEmpty` e o `@Valid` em cascata; responses têm o
método estático `from(...)` que converte a entidade em DTO de saída.

> ✅ **Cheque você mesmo:** por que não devolvemos a entidade `Cliente` direto na resposta,
> em vez de um `ClienteResponse`?
>
> Resposta: para **não acoplar o contrato da API ao modelo do banco** — DTOs nos deixam
> controlar o que entra (validação) e o que sai, e blindam a API contra mudanças internas
> da entidade.

---

## Etapa 6 — O coração: as regras de negócio (20 min) ⭐⭐ a mais importante
**Abra:** `service/ClienteService.java` com [`docs/03-regras-de-negocio.md`](03-regras-de-negocio.md) ao lado.

Leia método a método: `cadastrar`, `atualizar`, `deletar`, as consultas, e os privados
`montarUnidade` / `buscarAtivoOuFalhar` / `publicarSeMg`. Cada regra do desafio está
ancorada num trecho — cruze o código com o doc 03.

Repare também no `@Transactional` (tudo ou nada: se uma regra falha, faz rollback).

> ✅ **Cheque você mesmo:** percorra mentalmente um cadastro de cliente com unidade em SP.
> Em que linha/lógica ele é barrado, e qual exceção é lançada?
>
> Resposta: em `montarUnidade`, após buscar o endereço no ViaCEP, checamos
> `ESTADOS_NAO_ATENDIDOS.contains(uf)`. Como "SP" está no set, lança
> `RegiaoNaoAtendidaException`. Como tudo está em `@Transactional`, nada é salvo.

---

## Etapa 7 — O evento de MG (10 min)
**Abra:**
1. `event/ClienteMgEvent.java`
2. `event/ClienteMgEventListener.java`
3. Reveja o método `publicarSeMg` no `ClienteService`.

Entenda o trio: o service **publica** (`eventPublisher.publishEvent`), o Spring **entrega**,
o listener **consome** (`@EventListener`) e loga. É mensageria interna (sem Kafka).

> ✅ **Cheque você mesmo:** se amanhã pedirem para publicar de verdade no Kafka, qual classe
> você mexeria — e o `ClienteService` mudaria?
>
> Resposta: você mexeria só no **`ClienteMgEventListener`** (trocaria o log por um envio ao
> Kafka). O `ClienteService` **não muda** — ele só publica o evento; quem reage é o listener.
> Esse desacoplamento é proposital.

---

## Etapa 8 — Erros e a borda HTTP (10 min)
**Abra:**
1. `exception/GlobalExceptionHandler.java`
2. `exception/ApiError.java`
3. `controller/ClienteController.java`

Veja como o `@RestControllerAdvice` transforma cada exceção de negócio no status HTTP certo
(409/422/404/400) com corpo `ApiError`. Depois veja o controller: ele é fino de propósito —
recebe, chama o service, devolve `ResponseEntity` com o status (201/200/204).

> ✅ **Cheque você mesmo:** onde está escrito que "documento duplicado = 409"? É no service
> ou no controller?
>
> Resposta: em **nenhum dos dois diretamente**. O service só lança
> `DocumentoDuplicadoException`; o **`GlobalExceptionHandler`** mapeia essa exceção para
> `409 Conflict`. Centralizar isso mantém controller e service limpos.

---

## Etapa 9 — Os testes (15 min)
**Abra:**
1. `test/.../service/ClienteServiceTest.java` (unitário, com Mockito)
2. `test/.../controller/ClienteControllerIntegrationTest.java` (integração, MockMvc)

Entenda a diferença: o unitário **isola** o service e mocka tudo ao redor (`@Mock`); o de
integração **sobe a app inteira** e bate na API via HTTP simulado, com `@Transactional`
para limpar o banco entre testes. Em ambos o ViaCEP é mockado (testes não dependem de rede).

> ✅ **Cheque você mesmo:** por que a classe de integração tem `@Transactional`?
>
> Resposta: para fazer **rollback automático ao fim de cada teste**, deixando o banco H2
> limpo e os testes isolados (sem um teste "sujar" o estado do outro).

---

## Como rodar enquanto estuda
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
mvn spring-boot:run
```
- Brinque no Swagger: http://localhost:8082/swagger-ui.html
- Veja os dados no H2: http://localhost:8082/h2-console (JDBC `jdbc:h2:mem:clientesdb`, user `sa`)
- Faça um cadastro com CEP de MG e procure no terminal a linha
  `[TÓPICO analise_cliente_mg]`.

---

## Resumo de uma frase por camada (para revisar na véspera)
- **Controller** — porta de entrada HTTP; valida e delega, sem regra.
- **Service** — onde vivem TODAS as regras de negócio; transacional.
- **Repository** — acesso ao banco via interfaces (Spring gera o SQL).
- **Domain** — as entidades/tabelas (Cliente, UnidadeConsumidora, Endereco).
- **DTO** — contrato da API (entrada validada / saída controlada).
- **Client/Mapper** — integração ViaCEP e montagem do endereço.
- **Event** — tópico interno `analise_cliente_mg` (Spring ApplicationEvent).
- **Exception** — erros de negócio + tradução central para status HTTP.
