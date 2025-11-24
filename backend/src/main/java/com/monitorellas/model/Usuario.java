package com.monitorellas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String email;

    private String senha;

    // Indica se o e-mail já foi verificado (cadastro confirmado)
    private Boolean emailVerificado = false;
    // Hash (BCrypt) do código de verificação atual (nulo se verificado ou após expirar)
    private String verifCodigoHash;
    // Data/hora de expiração do código atual
    private LocalDateTime verifExpiresAt;
    // Número de tentativas de verificação realizadas para o código atual
    private Integer verifTentativas = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
