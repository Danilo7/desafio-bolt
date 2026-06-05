package com.bolt.clientes.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade central do sistema: o Cliente.
 *
 * Campos exigidos pelo desafio: id, nome, documento, unidades consumidoras,
 * endereço do cliente, createdAt, updatedAt e ativo (flag de remoção lógica).
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    /**
     * Documento (CPF/CNPJ). Regra: não pode haver dois clientes com o mesmo
     * documento -> constraint unique no banco + validação no service.
     */
    @Column(nullable = false, unique = true)
    private String documento;

    /** Endereço do próprio cliente (embutido, preenchido via ViaCEP). */
    @Embedded
    private Endereco endereco;

    /**
     * Relacionamento 1:N — um cliente possui várias unidades consumidoras.
     *
     *  - cascade = ALL: ao salvar/remover o cliente, as unidades acompanham.
     *  - orphanRemoval = true: se uma unidade for retirada da lista, ela é
     *    removida do banco.
     *  - @JoinColumn (cliente_id): a FK fica na tabela da unidade.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @jakarta.persistence.JoinColumn(name = "cliente_id")
    private List<UnidadeConsumidora> unidadesConsumidoras = new ArrayList<>();

    /**
     * Remoção lógica (soft delete). O desafio proíbe apagar fisicamente o
     * registro: ao "deletar", apenas marcamos ativo = false e filtramos os
     * inativos nas consultas.
     */
    @Column(nullable = false)
    private boolean ativo = true;

    /**
     * Auditoria automática (Hibernate):
     *  @CreationTimestamp -> preenchido uma vez, na criação.
     *  @UpdateTimestamp   -> atualizado a cada alteração.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Cliente() {
    }

    // ---- Métodos utilitários ------------------------------------------------

    /** Adiciona uma unidade mantendo a lista consistente. */
    public void addUnidade(UnidadeConsumidora unidade) {
        this.unidadesConsumidoras.add(unidade);
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

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public List<UnidadeConsumidora> getUnidadesConsumidoras() {
        return unidadesConsumidoras;
    }

    public void setUnidadesConsumidoras(List<UnidadeConsumidora> unidadesConsumidoras) {
        this.unidadesConsumidoras = unidadesConsumidoras;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
