package com.monitorellas.service;

import com.monitorellas.dto.CadastroRequest;
import com.monitorellas.dto.LoginRequest;
import com.monitorellas.dto.LoginResponse;
import com.monitorellas.model.Usuario;
import com.monitorellas.repository.UsuarioRepository;
import com.monitorellas.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public void cadastrar(CadastroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));

        usuarioRepository.save(usuario);
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        String token = tokenProvider.generateToken(usuario.getId(), usuario.getEmail());

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
}
