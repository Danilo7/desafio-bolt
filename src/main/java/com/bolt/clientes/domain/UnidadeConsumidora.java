package com.bolt.clientes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Unidade Consumidora — uma "instalação" pertencente a um cliente
 * (ex.: "Minha casa", "Meu comércio"). Um cliente pode ter várias (1:N).
 *
 * @Entity  -> esta classe é mapeada para uma tabela do banco.
 * @Table   -> nome explícito da tabela.
 *
 * Regra de negócio: "numeroInstalacao" é ÚNICO no sistema inteiro — uma mesma
 * instalação não pode ser cadastrada para clientes diferentes. Garantimos isso
 * com a constraint unique na coluna (defesa no banco) + validação no service
 * (mensagem amigável de erro).
 */
@Entity
@Table(name = "unidade_consumidora")
public class UnidadeConsumidora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-incremento no banco
    private Long id;

    /** Nome da unidade, ex.: "Minha casa". */
    @Column(nullable = false)
    private String nome;

    /** Número de instalação — único no sistema (ver regra acima). */
    @Column(name = "numero_instalacao", nullable = false, unique = true)
    private String numeroInstalacao;

    /** Endereço da unidade (embutido). A UF aqui dispara as regras SP/RS/PR e MG. */
    @Embedded
    private Endereco endereco;

    public UnidadeConsumidora() {
    }

    // ---- Getters e Setters --------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNumeroInstalacao() {
        return numeroInstalacao;
    }

    public void setNumeroInstalacao(String numeroInstalacao) {
        this.numeroInstalacao = numeroInstalacao;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
