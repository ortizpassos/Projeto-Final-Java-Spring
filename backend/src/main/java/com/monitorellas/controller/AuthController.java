package com.monitorellas.controller;

import com.monitorellas.dto.CadastroRequest;
import com.monitorellas.dto.LoginRequest;
import com.monitorellas.dto.LoginResponse;
import com.monitorellas.dto.VerifyEmailRequest;
import com.monitorellas.dto.ResendVerificationRequest;
import com.monitorellas.model.Usuario;
import com.monitorellas.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastro(@Valid @RequestBody CadastroRequest request) {
        try {
            authService.cadastrar(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuário cadastrado. Verifique seu e-mail para confirmar.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("Received login request: " + request);
        try {
            LoginResponse response = authService.login(request);
            if (!response.isSuccess() && response.getError() != null && Boolean.TRUE.equals(response.getError().getNeedsVerification())) {
                // Retornar status 403 para indicar restrição até verificação
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoginResponse errorResponse = LoginResponse.builder()
                    .success(false)
                    .error(LoginResponse.ErrorWrapper.builder()
                            .message(e.getMessage())
                            .needsVerification(false)
                            .build())
                    .build();

            HttpStatus status = e.getMessage().equals("Usuário não encontrado")
                    ? HttpStatus.BAD_REQUEST
                    : HttpStatus.UNAUTHORIZED;

            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @PostMapping("/verificar")
    public ResponseEntity<?> verificar(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            authService.verificarEmail(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "E-mail verificado com sucesso.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("email", request.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/reenviar")
    public ResponseEntity<?> reenviar(@Valid @RequestBody ResendVerificationRequest request) {
        try {
            authService.reenviarCodigo(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Novo código enviado para o e-mail.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("email", request.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(Authentication authentication) {
        try {
            String userId = (String) authentication.getPrincipal();
            Usuario usuario = authService.getUsuarioById(userId);

            usuario.setSenha(null);

            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
