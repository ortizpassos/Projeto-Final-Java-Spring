
package com.monitorellas.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Garante que o backend envie mensagens em JSON para o RabbitMQ
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Garante que o RabbitTemplate use o MessageConverter JSON
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Value("${app.mq.exchange}")
    private String exchangeName;

    @Value("${app.mq.queue.email}")
    private String emailQueueName;

    @Value("${app.mq.routing.operacao.created}")
    private String operacaoCreatedRoutingKey;

    @Bean
    public TopicExchange appExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue emailOutboxQueue() {
        return QueueBuilder.durable(emailQueueName).build();
    }

    @Bean
    public Binding bindingEmailOutbox(TopicExchange appExchange, Queue emailOutboxQueue) {
        return BindingBuilder.bind(emailOutboxQueue).to(appExchange).with(operacaoCreatedRoutingKey);
    }
}
