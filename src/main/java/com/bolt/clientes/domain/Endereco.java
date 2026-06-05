package com.bolt.clientes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Endereço como Objeto de Valor (Value Object).
 *
 * @Embeddable significa que esta classe NÃO vira uma tabela própria: seus
 * campos são "embutidos" (achatados) como colunas da tabela da entidade que
 * a usa. Ex.: o Cliente terá colunas endereco_cep, endereco_logradouro, etc.
 *
 * Usamos o mesmo Endereco tanto para o Cliente quanto para a Unidade
 * Consumidora, evitando repetição de código (DRY).
 *
 * Os dados de logradouro/bairro/cidade/uf são preenchidos automaticamente a
 * partir do CEP, consultando a API do ViaCEP (regra de negócio do desafio).
 */
@Embeddable
public class Endereco {

    @Column(length = 9)
    private String cep;

    private String logradouro;

    private String bairro;

    private String cidade;

    /** Unidade Federativa (estado), ex.: "MG", "SP". Usada nas regras de região. */
    @Column(length = 2)
    private String uf;

    /** Campos informados pelo usuário (o ViaCEP não retorna número/complemento). */
    private String numero;

    private String complemento;

    // O JPA/Hibernate exige um construtor sem argumentos para instanciar a entidade.
    public Endereco() {
    }

    // ---- Getters e Setters --------------------------------------------------
    // Em Java o acesso a atributos é feito por métodos get/set (encapsulamento),
    // diferente de PHP/Python onde costuma-se acessar a propriedade direto.

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }
}
