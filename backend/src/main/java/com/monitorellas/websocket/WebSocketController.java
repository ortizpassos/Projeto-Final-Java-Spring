package com.monitorellas.websocket;

import com.monitorellas.dto.DeviceEventDto;
import com.monitorellas.dto.WebSocketMessage;
import com.monitorellas.model.*;
import com.monitorellas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private OperacaoRepository operacaoRepository;

    @Autowired
    private ProducaoRepository producaoRepository;

    @MessageMapping("/registerDevice")
    public void registerDevice(@Payload DeviceEventDto data) {
        logger.info("Dispositivo registrado: {}", data.getDeviceToken());
        
        dispositivoRepository.findByDeviceToken(data.getDeviceToken()).ifPresent(dispositivo -> {
            dispositivo.setStatus("online");
            dispositivo.setUltimaAtualizacao(LocalDateTime.now());
            dispositivoRepository.save(dispositivo);
            
            // Emitir atualização para todos os clientes
            messagingTemplate.convertAndSend("/topic/deviceStatusUpdate", dispositivo);
        });
        
        WebSocketMessage response = WebSocketMessage.builder()
                .success(true)
                .message("Dispositivo registrado com sucesso!")
                .build();
        
        messagingTemplate.convertAndSend("/topic/deviceRegistered", response);
    }

    @MessageMapping("/loginFuncionario")
    public void loginFuncionario(@Payload DeviceEventDto data) {
        dispositivoRepository.findByDeviceToken(data.getDeviceToken()).ifPresent(dispositivo -> {
            Funcionario funcionario = null;
            
            if (data.getCodigo() != null) {
                funcionario = funcionarioRepository.findByCodigo(data.getCodigo()).orElse(null);
            } else if (data.getFuncionarioId() != null) {
                funcionario = funcionarioRepository.findById(data.getFuncionarioId()).orElse(null);
            }
            
            if (funcionario == null) {
                WebSocketMessage error = WebSocketMessage.builder()
                        .message("Funcionário não encontrado para a senha/código informado.")
                        .build();
                messagingTemplate.convertAndSend("/topic/loginFailed", error);
                return;
            }
            
            dispositivo.setFuncionarioLogado(funcionario);
            dispositivo.setStatus("online");
            dispositivo.setUltimaAtualizacao(LocalDateTime.now());
            dispositivoRepository.save(dispositivo);
            
            // Emitir atualização de status
            messagingTemplate.convertAndSend("/topic/deviceStatusUpdate", dispositivo);
            
            // Buscar operações ativas do usuário
            List<Operacao> operacoes = List.of();
            if (dispositivo.getUsuario() != null) {
                operacoes = operacaoRepository.findByUsuarioAndAtivoOrderByNomeAsc(
                        dispositivo.getUsuario(), true);
            }
            
            Map<String, Object> successData = new HashMap<>();
            successData.put("funcionario", Map.of("nome", funcionario.getNome()));
            successData.put("operacoes", operacoes);
            
            WebSocketMessage success = WebSocketMessage.builder()
                    .data(successData)
                    .build();
            
            messagingTemplate.convertAndSend("/topic/loginSuccess", success);
        });
    }

    @MessageMapping("/selecionarOperacao")
    public void selecionarOperacao(@Payload DeviceEventDto data) {
        dispositivoRepository.findByDeviceToken(data.getDeviceToken()).ifPresent(dispositivo -> {
            if (data.getOperacaoId() != null) {
                operacaoRepository.findById(data.getOperacaoId()).ifPresent(operacao -> {
                    dispositivo.setOperacao(operacao);
                    dispositivo.setStatus("em_producao");
                    
                    // Buscar produção atual do funcionário para esta operação e dispositivo
                    LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
                    LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);
                    
                    producaoRepository.findByFuncionarioAndDispositivoAndOperacaoAndDataHoraBetween(
                            dispositivo.getFuncionarioLogado(),
                            dispositivo,
                            operacao,
                            inicioDoDia,
                            fimDoDia
                    ).ifPresent(producao -> {
                        dispositivo.setProducaoAtual(producao.getQuantidade());
                    });
                    
                    dispositivoRepository.save(dispositivo);
                    
                    // Emitir atualização
                    messagingTemplate.convertAndSend("/topic/deviceStatusUpdate", dispositivo);
                    
                    Map<String, Object> operacaoData = new HashMap<>();
                    operacaoData.put("_id", operacao.getId());
                    operacaoData.put("nome", operacao.getNome());
                    operacaoData.put("metaDiaria", operacao.getMetaDiaria());
                    
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("operacao", operacaoData);
                    responseData.put("producaoAtual", dispositivo.getProducaoAtual());
                    
                    WebSocketMessage response = WebSocketMessage.builder()
                            .data(responseData)
                            .build();
                    
                    messagingTemplate.convertAndSend("/topic/operacaoSelecionada", response);
                });
            } else {
                WebSocketMessage error = WebSocketMessage.builder()
                        .error("Dispositivo ou operação não encontrada")
                        .build();
                messagingTemplate.convertAndSend("/topic/operacaoSelecionada", error);
            }
        });
    }

    @MessageMapping("/producao")
    public void producao(@Payload DeviceEventDto data) {
        logger.info("Produção recebida: {}", data);
        
        dispositivoRepository.findByDeviceToken(data.getDeviceToken()).ifPresent(dispositivo -> {
            Integer quantidade = data.getQuantidade() != null ? data.getQuantidade() : 1;
            Integer tempoProducao = data.getTempoProducao() != null ? data.getTempoProducao() : 0;
            
            Integer producaoAnterior = dispositivo.getProducaoAtual() != null ? dispositivo.getProducaoAtual() : 0;
            
            dispositivo.setProducaoAtual(quantidade);
            dispositivo.setUltimaAtualizacao(LocalDateTime.now());
            dispositivoRepository.save(dispositivo);
            
            Integer incremento = quantidade - producaoAnterior;
            
            if (incremento > 0 && dispositivo.getFuncionarioLogado() != null) {
                LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
                LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);
                
                Producao producaoExistente = producaoRepository
                        .findByFuncionarioAndDispositivoAndDataHoraBetween(
                                dispositivo.getFuncionarioLogado(),
                                dispositivo,
                                inicioDoDia,
                                fimDoDia
                        ).orElse(null);
                
                if (producaoExistente != null) {
                    producaoExistente.setQuantidade(quantidade);
                    producaoExistente.setTempoProducao(
                            (producaoExistente.getTempoProducao() != null ? producaoExistente.getTempoProducao() : 0) 
                            + tempoProducao
                    );
                    producaoExistente.setDataHora(LocalDateTime.now());
                    producaoRepository.save(producaoExistente);
                    logger.info("Registro de produção atualizado: {}", producaoExistente);
                } else {
                    Producao novaProducao = new Producao();
                    novaProducao.setFuncionario(dispositivo.getFuncionarioLogado());
                    novaProducao.setDispositivo(dispositivo);
                    novaProducao.setOperacao(dispositivo.getOperacao());
                    novaProducao.setQuantidade(quantidade);
                    novaProducao.setTempoProducao(tempoProducao);
                    novaProducao.setDataHora(LocalDateTime.now());
                    producaoRepository.save(novaProducao);
                    logger.info("Registro de produção criado: {}", novaProducao);
                }
            }
            
            logger.info("Emitindo productionUpdate para frontend: {}", dispositivo);
            messagingTemplate.convertAndSend("/topic/productionUpdate", 
                    Map.of("dispositivo", dispositivo));
        });
        
        WebSocketMessage response = WebSocketMessage.builder()
                .message("Produção recebida!")
                .build();
        messagingTemplate.convertAndSend("/topic/producaoSuccess", response);
    }
}
