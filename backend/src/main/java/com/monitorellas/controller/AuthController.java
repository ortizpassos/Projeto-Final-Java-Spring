package com.monitorellas.controller;

import com.monitorellas.dto.CadastroRequest;
import com.monitorellas.dto.LoginRequest;
import com.monitorellas.dto.LoginResponse;
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
            response.put("message", "Usuário cadastrado com sucesso");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoginResponse errorResponse = LoginResponse.builder()
                    .success(false)
                    .error(LoginResponse.ErrorWrapper.builder()
                            .message(e.getMessage())
                            .build())
                    .build();
            
            HttpStatus status = e.getMessage().equals("Usuário não encontrado") 
                    ? HttpStatus.BAD_REQUEST 
                    : HttpStatus.UNAUTHORIZED;
            
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(Authentication authentication) {
        try {
            String userId = (String) authentication.getPrincipal();
            Usuario usuario = authService.getUsuarioById(userId);
            
            // Remove senha da resposta
            usuario.setSenha(null);
            
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
