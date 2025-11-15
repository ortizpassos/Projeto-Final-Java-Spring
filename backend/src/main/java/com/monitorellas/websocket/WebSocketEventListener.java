package com.monitorellas.websocket;

import com.monitorellas.model.Dispositivo;
import com.monitorellas.repository.DispositivoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        MessageHeaders headers = accessor.getMessageHeaders();
        logger.info("STOMP CONNECT received. sessionId={}, command={}, headers={}",
                accessor.getSessionId(), accessor.getCommand(), headers);
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        logger.info("STOMP CONNECTED sent. sessionId={}, command={}", accessor.getSessionId(), accessor.getCommand());
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        logger.info("STOMP SUBSCRIBE. sessionId={}, destination={}, command={}",
                accessor.getSessionId(), accessor.getDestination(), accessor.getCommand());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        logger.info("WebSocket Disconnected: {} reason={} command={}",
                headerAccessor.getSessionId(), headerAccessor.getReceipt(), headerAccessor.getCommand());

        // Atualizar dispositivos online com última atualização antiga (>2 minutos)
        LocalDateTime doisMinutosAtras = LocalDateTime.now().minusMinutes(2);
        List<Dispositivo> dispositivos = dispositivoRepository
                .findByStatusAndUltimaAtualizacaoLessThan("online", doisMinutosAtras);

        for (Dispositivo dispositivo : dispositivos) {
            dispositivo.setStatus("offline");
            dispositivo.setUltimaAtualizacao(LocalDateTime.now());
            dispositivoRepository.save(dispositivo);
            messagingTemplate.convertAndSend("/topic/deviceStatusUpdate", dispositivo);
        }
    }
}
