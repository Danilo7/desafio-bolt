# 02 — Conceitos Java/Spring (para quem vem de PHP/Python)

Glossário prático dos conceitos usados no projeto, com analogias para PHP/Python.

## Java: o básico que aparece no código

| Conceito | O que é | Em PHP/Python |
|----------|---------|----------------|
| **Pacote** (`package com.bolt...`) | Namespace; espelha a estrutura de pastas | `namespace` (PHP) / módulos (Python) |
| **Classe** | Molde de objeto, fortemente tipado | classe |
| **Tipagem estática** | Todo tipo é declarado e checado na compilação | type hints, mas obrigatório |
| **Getter/Setter** | Métodos para ler/escrever atributos privados | `__get/__set` / `@property` |
| **`record`** | Classe imutável e enxuta para dados | `dataclass` (Python) / DTO readonly |
| **`Optional<T>`** | "Pode ou não ter valor"; evita `null` solto | `Optional` / `None` com checagem |
| **`stream()`** | Processamento funcional de listas (map/filter) | `array_map`/`array_filter` / list comprehension |
| **Interface** | Contrato sem implementação | interface |
| **Anotações** (`@Algo`) | Metadados que ligam comportamentos | atributos PHP 8 / decorators Python |

## Spring: o framework

O Spring Boot gira em torno de **dois pilares**:

### 1. Injeção de Dependência (DI) e o "contêiner de beans"
Você **não dá `new`** nos componentes principais. O Spring cria e gerencia objetos
(chamados **beans**) e os "injeta" onde forem necessários — geralmente pelo **construtor**.

```java
@Service
public class ClienteService {
    private final ClienteRepository repository;

    // O Spring injeta automaticamente o repositório aqui.
    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }
}
```
> É o mesmo conceito do **Service Container** do Laravel ou de DI em frameworks Python.
> Vantagem: baixo acoplamento e facilidade para testar (troca-se o bean real por um mock).

### 2. Anotações que registram beans

| Anotação | Significado |
|----------|-------------|
| `@SpringBootApplication` | Classe principal; liga auto-configuração e varredura de componentes |
| `@RestController` | Controller cujos retornos viram JSON |
| `@Service` | Componente de regra de negócio |
| `@Repository` | Componente de acesso a dados |
| `@Component` | Componente genérico gerenciado pelo Spring |
| `@Configuration` + `@Bean` | Classe que define beans manualmente |

## JPA / Hibernate (o ORM)

**JPA** é a especificação; **Hibernate** é a implementação. Mapeiam objetos Java ↔ tabelas.

| Anotação | Papel |
|----------|-------|
| `@Entity` | A classe vira uma tabela |
| `@Id` + `@GeneratedValue` | Chave primária autoincrementada |
| `@Column` | Configura uma coluna (nome, `nullable`, `unique`...) |
| `@OneToMany` | Relacionamento 1:N (um cliente, várias unidades) |
| `@Embeddable` / `@Embedded` | Objeto de valor "achatado" em colunas (nosso `Endereco`) |
| `@CreationTimestamp` / `@UpdateTimestamp` | Preenchem `createdAt`/`updatedAt` automaticamente |

### Spring Data JPA: repositórios "mágicos"
Declaramos só uma **interface** e o Spring gera a implementação:

```java
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDocumento(String documento);     // vira: WHERE documento = ?
    List<Cliente> findTop20ByAtivoTrueOrderByIdDesc();       // vira: WHERE ativo=true ORDER BY id DESC LIMIT 20
}
```
> O Spring lê o **nome do método** e gera o SQL. Chama-se *derived query*.

## Bean Validation (validação de entrada)
Anotações nos DTOs declaram regras; o Spring valida quando o controller usa `@Valid`:

```java
public record ClienteRequest(
    @NotBlank String nome,        // não pode ser vazio/nulo
    @NotEmpty List<...> unidades  // a lista precisa ter ao menos 1 item
) {}
```
> Equivale às *Form Request rules* do Laravel ou aos validators de serializers do Django.

## Tratamento de erros centralizado
`@RestControllerAdvice` captura exceções de qualquer controller e devolve um JSON
padronizado (`ApiError`). Assim, em vez de `try/catch` espalhado, o `service` só **lança**
a exceção certa e o handler decide o status HTTP. (Detalhes em
[`03-regras-de-negocio.md`](03-regras-de-negocio.md).)
