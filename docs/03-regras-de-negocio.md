# 03 — Regras de Negócio (explicadas com o código)

Cada regra do desafio, onde ela vive e como é testada.

---

## Regra 1 — Documento único
> *Não é permitido cadastrar clientes com o mesmo documento.*

**Onde:** `ClienteService.cadastrar` (e `atualizar`) + `unique` na entidade `Cliente`.

```java
if (clienteRepository.existsByDocumento(request.documento())) {
    throw new DocumentoDuplicadoException(request.documento());
}
```
- **Dupla proteção:** a checagem no service dá uma mensagem amigável; a constraint
  `unique = true` na coluna garante a integridade mesmo em concorrência.
- **Resposta:** `409 Conflict`.
- **Teste:** `deveLancarErroAoCadastrarDocumentoDuplicado`.

---

## Regra 2 — Unidade não pode ser de clientes diferentes
> *Não é permitido cadastrar unidades consumidoras para clientes diferentes.*

**Onde:** `ClienteService.montarUnidade` + `unique` em `numero_instalacao`.

```java
if (unidadeRepository.existsByNumeroInstalacao(req.numeroInstalacao())) {
    throw new UnidadeJaVinculadaException(req.numeroInstalacao());
}
```
- Interpretação: o número de instalação é **único no sistema** — se já existe, pertence a
  algum cliente e não pode ser reaproveitado por outro.
- **Resposta:** `409 Conflict`.
- **Teste:** `deveLancarErroQuandoUnidadeJaVinculada`.

---

## Regra 3 — Endereços via ViaCEP
> *Os endereços do cliente e das unidades devem ser consultados via API do ViaCEP.*

**Onde:** `ViaCepClient` (faz o HTTP) + `EnderecoMapper` (preenche a entidade).

```java
ViaCepResponse via = viaCepClient.buscarEndereco(request.cep());
endereco.setLogradouro(via.logradouro());
endereco.setCidade(via.cidade());
endereco.setUf(via.uf());
```
- O usuário envia **apenas o CEP** (+ número/complemento). Logradouro, bairro, cidade e UF
  vêm do ViaCEP — ele é a "fonte da verdade" do endereço.
- O ViaCEP responde `200 OK` com `{"erro": true}` para CEP inexistente; tratamos isso e
  lançamos `CepInvalidoException` → **`422`**.

---

## Regra 4 — Remoção lógica (soft delete)
> *Nenhum cliente pode ser removido fisicamente; use remoção lógica.*

**Onde:** `ClienteService.deletar` + flag `ativo` + filtros nas consultas.

```java
public void deletar(Long id) {
    Cliente cliente = buscarAtivoOuFalhar(id);
    cliente.setAtivo(false);          // não apaga; apenas inativa
    clienteRepository.save(cliente);
}
```
- As consultas usam métodos como `findByAtivoTrue` e `findByIdAndAtivoTrue`, então clientes
  inativos **somem das listagens** mas continuam no banco.
- **Resposta do DELETE:** `204 No Content`.
- **Teste:** `deveRealizarRemocaoLogica` (deleta e confirma `404` no GET seguinte).

---

## Regra 5 — Bloqueio de SP, RS, PR
> *Clientes com unidade em SP, RS ou PR não podem ser cadastrados.*

**Onde:** `ClienteService.montarUnidade`.

```java
private static final Set<String> ESTADOS_NAO_ATENDIDOS = Set.of("SP", "RS", "PR");
...
if (uf != null && ESTADOS_NAO_ATENDIDOS.contains(uf.toUpperCase())) {
    throw new RegiaoNaoAtendidaException(uf);
}
```
- A UF vem do ViaCEP (não confiamos no que o usuário digita).
- **Resposta:** `422 Unprocessable Entity`.
- **Teste:** `deveBloquearCadastroEmEstadoNaoAtendido` (unit) e
  `deveRetornar422ParaEstadoNaoAtendido` (integração).

---

## Regra 6 — Cliente de MG publica evento `analise_cliente_mg`
> *Clientes com unidade em MG devem, ao final do cadastro, ser publicados num tópico.*

**Onde:** `ClienteService.publicarSeMg` + `ClienteMgEvent` + `ClienteMgEventListener`.

```java
if (temUnidadeMg) {
    eventPublisher.publishEvent(
        new ClienteMgEvent(cliente.getId(), cliente.getDocumento(), cliente.getNome()));
}
```
- Usamos o **mecanismo de eventos do Spring** (`ApplicationEventPublisher`) como "tópico"
  interno — abordagem permitida pelo desafio e que evita infraestrutura externa.
- O `ClienteMgEventListener` "consome" o evento e o registra em log
  (`[TÓPICO analise_cliente_mg] ...`). O desafio diz que o consumer não é obrigatório;
  incluímos um simples para tornar o fluxo visível.
- **Teste:** `devePublicarEventoQuandoUnidadeEmMg` e `naoDevePublicarEventoQuandoForaDeMg`.

---

## Mapa de erros → status HTTP (centralizado no `GlobalExceptionHandler`)

| Exceção | Status |
|---------|--------|
| `ClienteNaoEncontradoException` | `404 Not Found` |
| `DocumentoDuplicadoException`, `UnidadeJaVinculadaException` | `409 Conflict` |
| `RegiaoNaoAtendidaException`, `CepInvalidoException` | `422 Unprocessable Entity` |
| Falha de `@Valid` | `400 Bad Request` |
| Qualquer outro erro | `500 Internal Server Error` |
