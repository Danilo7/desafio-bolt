package com.bolt.clientes.repository;

import com.bolt.clientes.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório de Cliente.
 *
 * Ao estender JpaRepository<Cliente, Long> já ganhamos de graça os métodos
 * save, findById, findAll, delete, etc. O Spring gera a implementação em
 * tempo de execução — não escrevemos a classe concreta.
 *
 * Os métodos abaixo são "derived queries": o Spring lê o NOME do método e gera
 * o SQL correspondente automaticamente. Ex.: findByDocumento -> WHERE documento = ?
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Usado para checar a regra de documento único. */
    Optional<Cliente> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    /** Lista apenas clientes ativos (soft delete) ordenados por id. */
    List<Cliente> findByAtivoTrue();

    /** Busca um cliente ativo por id (ignora os logicamente removidos). */
    Optional<Cliente> findByIdAndAtivoTrue(Long id);

    /**
     * Últimos 20 clientes ativos em ordem decrescente de id.
     * "Top20" + "OrderByIdDesc" são traduzidos para LIMIT 20 + ORDER BY id DESC.
     */
    List<Cliente> findTop20ByAtivoTrueOrderByIdDesc();
}
