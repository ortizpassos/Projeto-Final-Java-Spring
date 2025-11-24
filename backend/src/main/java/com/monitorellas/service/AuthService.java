package com.monitorellas.service;

import com.monitorellas.dto.CadastroRequest;
import com.monitorellas.dto.LoginRequest;
import com.monitorellas.dto.LoginResponse;
import com.monitorellas.dto.VerifyEmailRequest;
import com.monitorellas.dto.ResendVerificationRequest;
import com.monitorellas.model.Usuario;
import com.monitorellas.repository.UsuarioRepository;
import com.monitorellas.security.JwtTokenProvider;
import com.monitorellas.mq.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private EventPublisher eventPublisher;

    @Value("${verif.code.exp.minutes}")
    private int codeExpMinutes;

    @Value("${verif.code.attempt.limit}")
    private int attemptLimit;

    public void cadastrar(CadastroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setEmailVerificado(false);

        // Gerar código inicial de verificação
        String codigo = gerarCodigo();
        usuario.setVerifCodigoHash(passwordEncoder.encode(codigo));
        usuario.setVerifExpiresAt(LocalDateTime.now().plusMinutes(codeExpMinutes));
        usuario.setVerifTentativas(0);

        usuarioRepository.save(usuario);

        // Publicar evento para envio de email de verificação
        eventPublisher.publishEmailVerification(usuario, codigo, OffsetDateTime.now().plusMinutes(codeExpMinutes));
    }

    public LoginResponse login(LoginRequest request) {
        System.out.println("Login attempt for email: " + request.getEmail());
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    System.out.println("User not found: " + request.getEmail());
                    return new RuntimeException("Usuário não encontrado");
                });

        System.out.println("User found: " + usuario.getEmail());
        System.out.println("Stored hash: " + usuario.getSenha());
        
        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            System.out.println("Password mismatch for user: " + request.getEmail());
            throw new RuntimeException("Senha incorreta");
        }

        System.out.println("Password matched. Email verified status: " + usuario.getEmailVerificado());

        if (Boolean.FALSE.equals(usuario.getEmailVerificado())) {
            System.out.println("Email not verified for user: " + request.getEmail());
            return LoginResponse.builder()
                    .success(false)
                    .error(LoginResponse.ErrorWrapper.builder()
                            .message("E-mail não verificado")
                            .needsVerification(true)
                            .email(usuario.getEmail())
                            .build())
                    .build();
        }

        System.out.println("Generating token for user: " + request.getEmail());
        String token;
        try {
            token = tokenProvider.generateToken(usuario.getId(), usuario.getEmail());
        } catch (Exception e) {
            System.out.println("Error generating token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar token de autenticação", e);
        }
        System.out.println("Token generated successfully");

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .build();

        LoginResponse.DataWrapper data = LoginResponse.DataWrapper.builder()
                .user(userInfo)
                .token(token)
                .expiresIn(86400) // 1 dia em segundos
                .build();

        return LoginResponse.builder()
                .success(true)
                .data(data)
                .build();
    }

    public Usuario getUsuarioById(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public void verificarEmail(VerifyEmailRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            return; // já verificado, idempotente
        }

        if (usuario.getVerifExpiresAt() == null || usuario.getVerifExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado. Solicite novo código.");
        }

        if (usuario.getVerifTentativas() != null && usuario.getVerifTentativas() >= attemptLimit) {
            throw new RuntimeException("Limite de tentativas excedido. Solicite novo código.");
        }

        Integer tentativas = usuario.getVerifTentativas();
        if (tentativas == null) tentativas = 0;
        tentativas++;
        usuario.setVerifTentativas(tentativas);
        if (!passwordEncoder.matches(request.getCodigo(), usuario.getVerifCodigoHash())) {
            usuarioRepository.save(usuario); // persistir incremento de tentativas
            throw new RuntimeException("Código inválido");
        }

        // Sucesso
        usuario.setEmailVerificado(true);
        usuario.setVerifCodigoHash(null);
        usuario.setVerifExpiresAt(null);
        usuario.setVerifTentativas(0);
        usuarioRepository.save(usuario);
    }

    public void reenviarCodigo(ResendVerificationRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            throw new RuntimeException("E-mail já verificado");
        }

        String codigo = gerarCodigo();
        usuario.setVerifCodigoHash(passwordEncoder.encode(codigo));
        usuario.setVerifExpiresAt(LocalDateTime.now().plusMinutes(codeExpMinutes));
        usuario.setVerifTentativas(0);
        usuarioRepository.save(usuario);

        eventPublisher.publishEmailVerification(usuario, codigo, OffsetDateTime.now().plusMinutes(codeExpMinutes));
    }

    private String gerarCodigo() {
        Random random = new Random();
        int num = random.nextInt(900000) + 100000; // 100000 - 999999
        return String.valueOf(num);
    }
}
