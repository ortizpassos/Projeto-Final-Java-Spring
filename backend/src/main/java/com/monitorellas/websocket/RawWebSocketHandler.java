package com.monitorellas.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.monitorellas.model.*;
import com.monitorellas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RawWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RawWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private OperacaoRepository operacaoRepository;

    @Autowired
    private ProducaoRepository producaoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("[WS-RAW] Nova conexão: {} (remote: {})", session.getId(), session.getRemoteAddress());
        sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.warn("[WS-RAW] Conexão encerrada: {} (status: {}, remote: {})", session.getId(), status, session.getRemoteAddress());
        // Buscar dispositivo pelo sessionId (assumindo que deviceToken foi registrado nesta sessão)
        // Se não houver mapeamento direto, pode ser necessário mapear sessionId <-> deviceToken
        // Aqui, tentamos buscar pelo último deviceToken usado nesta sessão
        // (Sugestão: se possível, mantenha um Map<sessionId, deviceToken> para precisão)
        // Como fallback, buscar dispositivos online e setar offline se IP bater
        try {
            // Buscar todos dispositivos online
            for (Dispositivo d : dispositivoRepository.findAll()) {
                if (d.getStatus() != null && (d.getStatus().equals("online") || d.getStatus().equals("em_producao"))) {
                    // Opcional: comparar IP do dispositivo com session.getRemoteAddress()
                    // Aqui, setamos offline todos que estavam online
                    d.setStatus("offline");
                    d.setFuncionarioLogado(null);
                    d.setOperacao(null);
                    d.setProducaoAtual(0);
                    d.setUltimaAtualizacao(LocalDateTime.now());
                    dispositivoRepository.save(d);
                    // Broadcast para frontend
                    messagingTemplate.convertAndSend("/topic/deviceStatusUpdate", d);
                }
            }
        } catch (Exception e) {
            logger.error("[WS-RAW] Erro ao atualizar status dos dispositivos para offline ao desconectar: ", e);
        }
        sessions.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("[WS-RAW] Mensagem recebida: {} (session: {})", payload, session.getId());
        try {
            JsonNode json = objectMapper.readTree(payload);
            String type = json.has("type") ? json.get("type").asText() : "";
            String deviceToken = json.has("deviceToken") ? json.get("deviceToken").asText() : "";

            switch (type) {
                case "registerDevice":
                    handleRegisterDevice(session, deviceToken);
                    break;
                case "loginFuncionario":
                    String codigo = json.has("codigo") ? json.get("codigo").asText() : "";
                    handleLoginFuncionario(session, deviceToken, codigo);
                    break;
                case "selecionarOperacao":
                    String operacaoId = json.has("operacaoId") ? json.get("operacaoId").asText() : "";
                    handleSelecionarOperacao(session, deviceToken, operacaoId);
                    break;
                case "producao":
                    int quantidade = json.has("quantidade") ? json.get("quantidade").asInt() : 1;
                    int tempoProducao = json.has("tempoProducao") ? json.get("tempoProducao").asInt() : 0;
                    handleProducao(session, deviceToken, quantidade, tempoProducao);
                    break;
                default:
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Tipo não reconhecido\"}"));
            }
        } catch (Exception e) {
            logger.error("[WS-RAW] Erro ao processar mensagem", e);
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Erro ao processar: " + e.getMessage() + "\"}"));
        }
    }

    private void handleRegisterDevice(WebSocketSession session, String deviceToken) throws Exception {
        Optional<Dispositivo> dispositivoOpt = dispositivoRepository.findByDeviceToken(deviceToken);

        if (dispositivoOpt.isPresent()) {
            Dispositivo dispositivo = dispositivoOpt.get();
            dispositivo.setStatus("online");
            dispositivo.setUltimaAtualizacao(LocalDateTime.now());
            dispositivoRepository.save(dispositivo);

            // Broadcast para clientes STOMP (frontend web)
            messagingTemplate.convertAndSend("/topic/deviceStatusUpdate", dispositivo);

            session.sendMessage(new TextMessage("{\"type\":\"deviceRegistered\",\"success\":true,\"message\":\"Dispositivo registrado com sucesso!\"}"));
            logger.info("[WS-RAW] Dispositivo {} registrado", deviceToken);
        } else {
            session.sendMessage(new TextMessage("{\"type\":\"deviceRegistered\",\"success\":false,\"message\":\"Dispositivo não encontrado no sistema\"}"));
            logger.warn("[WS-RAW] Tentativa de registro de dispositivo não cadastrado: {}", deviceToken);
        }
    }

    private void handleLoginFuncionario(WebSocketSession session, String deviceToken, String codigo) throws Exception {

        logger.info("[WS-RAW] Tentativa de loginFuncionario: deviceToken={}, codigo={}, session={}", deviceToken, codigo, session.getId());
        Optional<Dispositivo> dispositivoOpt = dispositivoRepository.findByDeviceToken(deviceToken);

        if (dispositivoOpt.isEmpty()) {
            logger.error("[WS-RAW] Dispositivo não encontrado para token {} (session: {})", deviceToken, session.getId());
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Dispositivo não encontrado\"}"));
            return;
        }

        Dispositivo dispositivo = dispositivoOpt.get();

        Optional<Funcionario> funcionarioOpt = Optional.empty();
        if (dispositivo.getUsuario() != null) {
            funcionarioOpt = funcionarioRepository.findByCodigoAndUsuario(codigo, dispositivo.getUsuario());
        }


        if (funcionarioOpt.isEmpty()) {
            // Limpar funcionarioLogado do dispositivo para permitir nova tentativa
            dispositivo.setFuncionarioLogado(null);
            dispositivoRepository.save(dispositivo);
            logger.warn("[WS-RAW] Funcionário não encontrado para código {} (token: {}, session: {}). Permitir nova tentativa.", codigo, deviceToken, session.getId());
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Funcionário não encontrado. Digite novamente.\"}"));
            return;
        }

        Funcionario funcionario = funcionarioOpt.get();

        dispositivo.setFuncionarioLogado(funcionario);
        dispositivo.setStatus("online");
        dispositivo.setUltimaAtualizacao(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);

        // Broadcast para clientes STOMP (frontend web)
        messagingTemplate.convertAndSend("/topic/deviceStatusUpdate", dispositivo);

        // Buscar operações ativas do usuário do dispositivo
        List<Operacao> operacoes = List.of();
        if (dispositivo.getUsuario() != null) {
            operacoes = operacaoRepository.findByUsuarioAndAtivoOrderByNomeAsc(
                    dispositivo.getUsuario(), true);
        }

        // Montar resposta JSON
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "loginSuccess");

        ObjectNode data = objectMapper.createObjectNode();
        ObjectNode funcionarioData = objectMapper.createObjectNode();
        funcionarioData.put("nome", funcionario.getNome());
        data.set("funcionario", funcionarioData);

        ArrayNode operacoesArray = objectMapper.createArrayNode();
        for (Operacao op : operacoes) {
            ObjectNode opNode = objectMapper.createObjectNode();
            opNode.put("_id", op.getId());
            opNode.put("nome", op.getNome());
            opNode.put("metaDiaria", op.getMetaDiaria());
            operacoesArray.add(opNode);
        }
        data.set("operacoes", operacoesArray);
        response.set("data", data);

        // Só envia loginSuccess se houver operações disponíveis
        if (!operacoes.isEmpty()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            logger.info("[WS-RAW] Funcionário {} logado, {} operações disponíveis (session: {})", funcionario.getNome(), operacoes.size(), session.getId());
        } else {
            logger.warn("[WS-RAW] Nenhuma operação disponível para usuário do dispositivo {} (session: {})", deviceToken, session.getId());
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Nenhuma operação disponível para este usuário.\"}"));
        }
    }

    private void handleSelecionarOperacao(WebSocketSession session, String deviceToken, String operacaoId) throws Exception {
        Optional<Dispositivo> dispositivoOpt = dispositivoRepository.findByDeviceToken(deviceToken);
        Optional<Operacao> operacaoOpt = operacaoRepository.findById(operacaoId);

        if (dispositivoOpt.isEmpty() || operacaoOpt.isEmpty()) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Dispositivo ou operação não encontrada\"}"));
            return;
        }

        Dispositivo dispositivo = dispositivoOpt.get();
        Operacao operacao = operacaoOpt.get();

        dispositivo.setOperacao(operacao);
        dispositivo.setStatus("em_producao");

        // Buscar produção atual do dia
        LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);

        int producaoAtual = 0;
        Optional<Producao> producaoOpt = producaoRepository.findByFuncionarioAndDispositivoAndOperacaoAndDataHoraBetween(
                dispositivo.getFuncionarioLogado(),
                dispositivo,
                operacao,
                inicioDoDia,
                fimDoDia
        );

        if (producaoOpt.isPresent()) {
            producaoAtual = producaoOpt.get().getQuantidade();
        }

        dispositivo.setProducaoAtual(producaoAtual);
        dispositivoRepository.save(dispositivo);

        // Broadcast para clientes STOMP (frontend web)
        messagingTemplate.convertAndSend("/topic/deviceStatusUpdate", dispositivo);

        // Montar resposta JSON
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "operacaoSelecionada");

        ObjectNode data = objectMapper.createObjectNode();
        ObjectNode operacaoData = objectMapper.createObjectNode();
        operacaoData.put("_id", operacao.getId());
        operacaoData.put("nome", operacao.getNome());
        operacaoData.put("metaDiaria", operacao.getMetaDiaria());
        data.set("operacao", operacaoData);
        data.put("producaoAtual", producaoAtual);
        response.set("data", data);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        logger.info("[WS-RAW] Operação {} selecionada, produção atual: {}", operacao.getNome(), producaoAtual);
    }

    private void handleProducao(WebSocketSession session, String deviceToken, int quantidade, int tempoProducao) throws Exception {
        Optional<Dispositivo> dispositivoOpt = dispositivoRepository.findByDeviceToken(deviceToken);

        if (dispositivoOpt.isEmpty()) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Dispositivo não encontrado\"}"));
            return;
        }

        Dispositivo dispositivo = dispositivoOpt.get();

        if (dispositivo.getFuncionarioLogado() == null || dispositivo.getOperacao() == null) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Funcionário ou operação não selecionados\"}"));
            return;
        }

        Integer producaoAnterior = dispositivo.getProducaoAtual() != null ? dispositivo.getProducaoAtual() : 0;

        dispositivo.setProducaoAtual(quantidade);
        dispositivo.setUltimaAtualizacao(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);

        Integer incremento = quantidade - producaoAnterior;

        if (incremento > 0) {
            LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);

            Optional<Producao> producaoOpt = producaoRepository
                    .findByFuncionarioAndDispositivoAndOperacaoAndDataHoraBetween(
                            dispositivo.getFuncionarioLogado(),
                            dispositivo,
                            dispositivo.getOperacao(),
                            inicioDoDia,
                            fimDoDia
                    );

            if (producaoOpt.isPresent()) {
                Producao producao = producaoOpt.get();
                producao.setQuantidade(quantidade);
                producao.setTempoProducao(
                        (producao.getTempoProducao() != null ? producao.getTempoProducao() : 0)
                                + tempoProducao
                );
                producao.setDataHora(LocalDateTime.now());
                producaoRepository.save(producao);
                logger.info("[WS-RAW] Produção atualizada: {} peças", quantidade);
            } else {
                Producao novaProducao = new Producao();
                novaProducao.setFuncionario(dispositivo.getFuncionarioLogado());
                novaProducao.setDispositivo(dispositivo);
                novaProducao.setOperacao(dispositivo.getOperacao());
                novaProducao.setQuantidade(quantidade);
                novaProducao.setTempoProducao(tempoProducao);
                novaProducao.setDataHora(LocalDateTime.now());
                producaoRepository.save(novaProducao);
                logger.info("[WS-RAW] Nova produção registrada: {} peças", quantidade);
            }
        }


        // ✅ BROADCAST EM TEMPO REAL apenas para o usuário dono do dispositivo
        if (dispositivo.getUsuario() != null && dispositivo.getUsuario().getId() != null) {
            String userTopic = "/topic/productionUpdate." + dispositivo.getUsuario().getId();
            logger.info("[WS-RAW] Broadcasting produção para {}", userTopic);
            messagingTemplate.convertAndSend(userTopic, Map.of("dispositivo", dispositivo));
        } else {
            logger.warn("[WS-RAW] Dispositivo sem usuário ao tentar enviar produção!");
        }

        session.sendMessage(new TextMessage("{\"type\":\"producaoSuccess\",\"message\":\"Produção registrada com sucesso!\"}"));
    }
}
