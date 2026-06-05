package com.bolt.clientes.repository;

import com.bolt.clientes.domain.UnidadeConsumidora;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório de Unidade Consumidora.
 *
 * Usado principalmente para a regra "uma unidade (numeroInstalacao) não pode
 * ser cadastrada para clientes diferentes": antes de salvar, o service
 * verifica se o número já existe.
 */
public interface UnidadeConsumidoraRepository extends JpaRepository<UnidadeConsumidora, Long> {

    boolean existsByNumeroInstalacao(String numeroInstalacao);
}
