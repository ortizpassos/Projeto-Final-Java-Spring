package com.monitorellas.controller;

import com.monitorellas.model.Dispositivo;
import com.monitorellas.model.Funcionario;
import com.monitorellas.model.Usuario;
import com.monitorellas.model.Operacao;
import com.monitorellas.repository.OperacaoRepository;
import com.monitorellas.service.AuthService;
import com.monitorellas.service.DispositivoService;
import com.monitorellas.service.FuncionarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    @Autowired
    private FuncionarioService funcionarioService;

    @Autowired
    private AuthService authService;

    @Autowired
    private DispositivoService dispositivoService;

    @Autowired
    private OperacaoRepository operacaoRepository;

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Funcionario funcionario, Authentication authentication) {
        try {
            String userId = (String) authentication.getPrincipal();
            Usuario usuario = authService.getUsuarioById(userId);
            funcionario.setUsuario(usuario);
            Funcionario novoFuncionario = funcionarioService.criar(funcionario);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoFuncionario);
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
            List<Funcionario> funcionarios = funcionarioService.listarPorUsuario(usuario);
            return ResponseEntity.ok(funcionarios);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        try {
            Funcionario funcionario = funcionarioService.buscarPorId(id);
            return ResponseEntity.ok(funcionario);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/login-por-codigo")
    public ResponseEntity<?> loginPorCodigo(@RequestBody Map<String, String> payload) {
        String codigo = payload.get("codigo");
        String deviceToken = payload.get("deviceToken");
        if (codigo == null || deviceToken == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Código e deviceToken são obrigatórios");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        // Buscar dispositivo pelo deviceToken
        Dispositivo dispositivo = dispositivoService.buscarPorDeviceToken(deviceToken);
        if (dispositivo == null || dispositivo.getUsuario() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Dispositivo não encontrado ou sem usuário associado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        // Buscar funcionário pelo código e usuário do dispositivo
        Funcionario funcionario = funcionarioService.buscarPorCodigoEUsuario(codigo, dispositivo.getUsuario());
        if (funcionario == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Funcionário não cadastrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        // Buscar operações do mesmo usuário do dispositivo
        List<Operacao> operacoes = operacaoRepository.findByUsuarioAndAtivoOrderByNomeAsc(dispositivo.getUsuario(), true);
        Map<String, Object> result = new HashMap<>();
        result.put("funcionario", funcionario);
        result.put("operacoes", operacoes);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody Funcionario funcionario) {
        try {
            Funcionario funcionarioAtualizado = funcionarioService.atualizar(id, funcionario);
            return ResponseEntity.ok(funcionarioAtualizado);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable String id) {
        try {
            funcionarioService.deletar(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Funcionário deletado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
