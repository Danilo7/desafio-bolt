# 04 — Decisões Técnicas e Trade-offs

Por que cada escolha foi feita, e quais eram as alternativas. Útil para responder
"por que você fez assim?" numa entrevista.

---

## 1. Java 17 (em vez de Kotlin)
- **Por quê:** o desafio aceita os dois; Java tem mais material e é mais fácil de **ler e
  explicar** para quem não é da stack. Java 17 traz `record`, `var`, `switch` moderno —
  reduz a verbosidade clássica.
- **Trade-off:** Kotlin seria mais conciso, mas adicionaria curva de aprendizado.

## 2. Spring ApplicationEvent (em vez de Kafka/RabbitMQ)
- **Por quê:** o desafio **permite explicitamente** evento interno. Mantém a app simples,
  sem subir broker. Alinha-se ao valor de "simplicidade" da Bolt.
- **Trade-off:** não é mensageria distribuída real (eventos vivem só dentro do processo).
  Para evoluir, bastaria publicar num broker dentro do `ClienteMgEventListener` — o resto
  do código não mudaria. **Esse isolamento é proposital.**

## 3. H2 em memória (em vez de Postgres/MySQL)
- **Por quê:** recomendado no desafio; zero setup para quem avalia. Sobe junto com a app.
- **Trade-off:** os dados somem ao desligar. Para persistir, trocaríamos a `datasource` no
  `application.yml` e adicionaríamos o driver — o código JPA continuaria igual (vantagem do ORM).

## 4. DTOs separados das entidades
- **Por quê:** não expor a entidade JPA diretamente na API. DTOs permitem validar a
  entrada, controlar o que sai e blindar contra mudanças internas do modelo.
- **Trade-off:** mais classes e conversões (`from`/`mapper`). O ganho em clareza e segurança
  compensa — é prática padrão em APIs de produção.

## 5. Endereço como `@Embeddable` reutilizável
- **Por quê:** cliente e unidade têm o mesmo formato de endereço. Um `@Embeddable` evita
  duplicação (DRY) e mantém os campos como colunas das próprias tabelas (sem JOIN extra).
- **Alternativa:** tabela separada de endereços — seria overkill para este caso.

## 6. Tratamento de erros com `@RestControllerAdvice`
- **Por quê:** centraliza o mapeamento exceção → status HTTP. Controllers e services ficam
  limpos: só lançam a exceção de negócio. Respostas de erro ficam **padronizadas** (`ApiError`).
- **Trade-off:** uma indireção a mais, mas é o padrão recomendado pelo Spring.

## 7. Validação no service além do banco
- **Por quê:** a constraint `unique` protege a integridade, mas lança um erro técnico feio.
  A checagem no service (`existsBy...`) entrega uma **mensagem de negócio clara** antes disso.
- **Trade-off:** uma consulta a mais, aceitável para a clareza da mensagem de erro.

## 8. `RestClient` para o ViaCEP
- **Por quê:** é o cliente HTTP **moderno** do Spring 6 (o `RestTemplate` está em
  manutenção). Isolado em `ViaCepClient`, é trivial de mockar nos testes.

## 9. Soft delete via flag + métodos de repositório
- **Por quê:** simples e explícito (`findByAtivoTrue`). Deixa claro nas assinaturas que só
  buscamos ativos.
- **Alternativa:** `@SQLRestriction`/`@Where` do Hibernate aplicaria o filtro globalmente,
  mas esconde o comportamento. Preferimos a forma explícita pela legibilidade.

## 10. Testes em dois níveis
- **Unitários** (`ClienteServiceTest`): rápidos, focados nas regras, com mocks. Garantem a
  lógica isolada.
- **Integração** (`ClienteControllerIntegrationTest`): sobem o contexto real (HTTP + JPA +
  H2) e validam o fluxo ponta a ponta; usam `@Transactional` para **rollback automático** e
  isolamento entre testes.
- **ViaCEP é sempre mockado** nos testes — não dependemos de rede externa, então a suíte é
  determinística e roda offline.
