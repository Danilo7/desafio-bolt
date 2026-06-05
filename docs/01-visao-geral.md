# 01 — Visão Geral e Arquitetura

Este documento explica **como o projeto está organizado** e **o que acontece quando uma
requisição chega**. Se você vem de PHP/Python, pense nisto como o "mapa" da aplicação.

## Arquitetura em camadas

A aplicação segue uma separação clássica em camadas. Cada camada tem **uma única
responsabilidade**, o que facilita entender, testar e manter o código.

```
   HTTP (cliente: Postman, navegador, etc.)
        │
        ▼
┌───────────────────┐
│   Controller      │  Recebe a requisição, valida o corpo (@Valid),
│  (camada web)     │  chama o service e devolve a resposta HTTP.
└─────────┬─────────┘  NÃO contém regra de negócio.
          │
          ▼
┌───────────────────┐
│    Service        │  Onde vivem as REGRAS DE NEGÓCIO.
│ (regras/negócio)  │  Orquestra repositório + ViaCEP + evento.
└─────────┬─────────┘
          │
   ┌──────┴───────┬───────────────┐
   ▼              ▼               ▼
┌────────┐  ┌───────────┐  ┌──────────────┐
│Repository│ │ViaCepClient│ │EventPublisher│
│ (banco)  │ │  (HTTP)    │ │  (evento MG) │
└────┬─────┘ └───────────┘ └──────────────┘
     ▼
┌──────────┐
│ Banco H2 │
└──────────┘
```

## Mapa de pastas e responsabilidades

| Pacote | Papel | Analogia (PHP/Python) |
|--------|-------|------------------------|
| `controller` | Entrada HTTP (rotas REST) | Controllers do Laravel / views do Django |
| `service` | Regras de negócio | Services / camada de aplicação |
| `repository` | Acesso a dados | Eloquent / Django ORM / SQLAlchemy |
| `domain` | Entidades (tabelas) | Models |
| `dto` | Objetos de entrada/saída da API | Form Requests / Serializers |
| `mapper` | Converte DTO → entidade (usa ViaCEP) | Transformers |
| `client` | Integração com API externa | Cliente Guzzle / requests |
| `event` | Evento interno (tópico MG) | Eventos/Listeners do Laravel |
| `exception` | Erros de negócio + handler global | Exception Handler |
| `config` | Configuração de beans | Service Providers |

## Fluxo completo de um cadastro (POST /api/clientes)

Acompanhe o caminho dos dados, passo a passo:

1. **Controller** (`ClienteController.cadastrar`)
   - Recebe o JSON e o converte em `ClienteRequest`.
   - `@Valid` dispara as validações (nome/documento obrigatórios, etc.).
   - Se a validação falhar → `GlobalExceptionHandler` devolve `400`.

2. **Service** (`ClienteService.cadastrar`)
   - **Regra 1:** verifica documento duplicado → `409` se já existe.
   - Monta a entidade `Cliente` e busca o **endereço do cliente** via ViaCEP.
   - Para cada unidade consumidora (`montarUnidade`):
     - **Regra 2:** verifica nº de instalação já usado → `409`.
     - Busca o **endereço da unidade** via ViaCEP.
     - **Regra 5:** se UF ∈ {SP, RS, PR} → `422`.
   - Salva tudo no banco (`clienteRepository.save`).
   - **Regra 6:** se alguma unidade for MG → publica `ClienteMgEvent`.

3. **Repository** → grava no banco H2 (Hibernate gera o SQL).

4. **Controller** → converte a entidade salva em `ClienteResponse` e devolve `201 Created`.

## Por que essa separação importa?

- **Testabilidade:** dá para testar as regras do `service` isoladamente, "mockando" o
  banco e o ViaCEP (ver `ClienteServiceTest`).
- **Manutenção:** mudar a forma de buscar CEP afeta só o `client`/`mapper`, não o resto.
- **Clareza:** cada arquivo tem um propósito; é fácil achar onde algo acontece.

Continue em [`02-conceitos-java-spring.md`](02-conceitos-java-spring.md) para o glossário
de conceitos Java/Spring.
