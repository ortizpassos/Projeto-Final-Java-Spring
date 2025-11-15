package com.monitorellas.controller;

import com.monitorellas.model.Dispositivo;
import com.monitorellas.model.Usuario;
import com.monitorellas.service.AuthService;
import com.monitorellas.service.DispositivoService;
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
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    private static final Logger logger = LoggerFactory.getLogger(DispositivoController.class);

    @Autowired
    private DispositivoService dispositivoService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Dispositivo dispositivo, Authentication authentication) {
        try {
            logger.debug("REQ.BODY: {}", dispositivo);
            
            String userId = (String) authentication.getPrincipal();
            Usuario usuario = authService.getUsuarioById(userId);
            
            Dispositivo novoDispositivo = dispositivoService.criar(dispositivo, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoDispositivo);
        } catch (Exception e) {
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
            
            List<Dispositivo> dispositivos = dispositivoService.listarPorUsuario(usuario);
            return ResponseEntity.ok(dispositivos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        try {
            Dispositivo dispositivo = dispositivoService.buscarPorId(id);
            return ResponseEntity.ok(dispositivo);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody Dispositivo dispositivo) {
        try {
            Dispositivo dispositivoAtualizado = dispositivoService.atualizar(id, dispositivo);
            return ResponseEntity.ok(dispositivoAtualizado);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable String id) {
        try {
            dispositivoService.deletar(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Dispositivo deletado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
