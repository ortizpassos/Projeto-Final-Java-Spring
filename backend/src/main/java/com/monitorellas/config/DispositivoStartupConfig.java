package com.monitorellas.config;

import com.monitorellas.model.Dispositivo;
import com.monitorellas.repository.DispositivoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class DispositivoStartupConfig {
    private static final Logger logger = LoggerFactory.getLogger(DispositivoStartupConfig.class);

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @PostConstruct
    public void marcarTodosOffline() {
        logger.info("[STARTUP] Marcando todos os dispositivos como offline e limpando sessão...");
        for (Dispositivo d : dispositivoRepository.findAll()) {
            d.setStatus("offline");
            d.setFuncionarioLogado(null);
            d.setOperacao(null);
            d.setProducaoAtual(0);
            dispositivoRepository.save(d);
        }
    }
}
