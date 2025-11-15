package com.monitorellas.controller;

import com.monitorellas.model.Operacao;
import com.monitorellas.model.Usuario;
import com.monitorellas.service.AuthService;
import com.monitorellas.service.OperacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operacoes")
public class OperacaoController {

    private static final Logger logger = LoggerFactory.getLogger(OperacaoController.class);

    @Autowired
    private OperacaoService operacaoService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Operacao operacao, Authentication authentication) {
        try {
            logger.debug("POST /api/operacoes");
            logger.debug("req.body: {}", operacao);
            
            String userId = (String) authentication.getPrincipal();
            logger.debug("req.usuario: {}", userId);
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Usuário não autenticado"));
            }
            
            Usuario usuario = authService.getUsuarioById(userId);
            Operacao novaOperacao = operacaoService.criar(operacao, usuario);
            
            logger.debug("Operação criada: {}", novaOperacao);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaOperacao);
        } catch (Exception e) {
            logger.error("Erro ao criar operação:", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> listar(Authentication authentication) {
        try {
            String userId = (String) authentication.getPrincipal();
            Usuario usuario = authService.getUsuarioById(userId);
            
            List<Operacao> operacoes = operacaoService.listarPorUsuario(usuario);
            return ResponseEntity.ok(operacoes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        try {
            Operacao operacao = operacaoService.buscarPorId(id);
            return ResponseEntity.ok(operacao);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody Operacao operacao) {
        try {
            Operacao operacaoAtualizada = operacaoService.atualizar(id, operacao);
            return ResponseEntity.ok(operacaoAtualizada);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable String id) {
        try {
            operacaoService.desativar(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Operação desativada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
