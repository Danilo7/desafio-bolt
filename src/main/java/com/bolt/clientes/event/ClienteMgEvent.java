package com.bolt.clientes.event;

/**
 * Evento de domínio publicado quando um cliente com unidade consumidora em MG
 * é cadastrado. Representa a publicação no "tópico" analise_cliente_mg.
 *
 * Escolhemos o mecanismo de eventos do próprio Spring (ApplicationEvent) em vez
 * de um broker externo (Kafka/RabbitMQ): o desafio permite explicitamente essa
 * abordagem e ela mantém a aplicação simples, sem infraestrutura adicional.
 *
 * É um record imutável carregando apenas o necessário para o consumidor.
 */
public record ClienteMgEvent(Long clienteId, String documento, String nomeCliente) {
}
