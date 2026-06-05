package com.bolt.clientes.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * "Consumidor" do tópico analise_cliente_mg.
 *
 * O desafio diz que a implementação do consumer NÃO é necessária; ainda assim
 * incluímos um listener simples que registra (log) a publicação, para tornar o
 * fluxo observável e fácil de demonstrar na avaliação.
 *
 * @EventListener faz o Spring chamar este método sempre que um ClienteMgEvent
 * for publicado via ApplicationEventPublisher.
 *
 * Num cenário real, aqui entraria a publicação num broker (Kafka/RabbitMQ) ou
 * a lógica de análise do cliente.
 */
@Component
public class ClienteMgEventListener {

    private static final Logger log = LoggerFactory.getLogger(ClienteMgEventListener.class);

    @EventListener
    public void handle(ClienteMgEvent event) {
        log.info("[TÓPICO analise_cliente_mg] Cliente publicado para análise -> id={}, documento={}, nome={}",
                event.clienteId(), event.documento(), event.nomeCliente());
    }
}
