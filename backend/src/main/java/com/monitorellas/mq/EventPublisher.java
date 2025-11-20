package com.monitorellas.mq;

import com.monitorellas.model.Operacao;
import com.monitorellas.model.Usuario;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${app.mq.exchange}")
    private String exchange;

    @Value("${app.mq.routing.operacao.created}")
    private String operacaoCreatedRoutingKey;

    public void publishOperacaoCreated(Operacao operacao, Usuario usuario) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "operacao.created");
        payload.put("timestamp", OffsetDateTime.now().toString());
        payload.put("operacaoId", operacao.getId());
        payload.put("operacaoNome", operacao.getNome());
        payload.put("usuarioId", usuario.getId());
        payload.put("usuarioEmail", usuario.getEmail());
        rabbitTemplate.convertAndSend(exchange, operacaoCreatedRoutingKey, payload);
    }
}
