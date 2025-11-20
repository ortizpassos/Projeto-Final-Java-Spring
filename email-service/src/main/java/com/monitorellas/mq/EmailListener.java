
package com.monitorellas.mq;

import com.monitorellas.mail.MailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailListener {

    private final MailService mailService;

    @Value("${spring.application.name}")
    private String appName;

    public EmailListener(MailService mailService) {
        this.mailService = mailService;
    }

    @RabbitListener(queues = "${app.mq.queue}")
    public void onOperacaoCreated(@Payload Map<String, Object> payload) {
        String to = (String) payload.get("usuarioEmail");
        if (to == null || to.isBlank()) return;
        String operacaoNome = (String) payload.getOrDefault("operacaoNome", "Operação");
        String subject = "[" + appName + "] Operação criada";
        String body = "Sua operação '" + operacaoNome + "' foi criada com sucesso.\n\n" +
                "Detalhes: " + payload + "\n";
        mailService.send(to, subject, body);
    }
}
