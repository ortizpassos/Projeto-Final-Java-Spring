package com.monitorellas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dispositivos")
public class Dispositivo {

    @Id
    @JsonProperty("_id")
    private String id;

    @DBRef
    private Usuario usuario;

    @Indexed(unique = true)
    private String deviceToken;

    private String nome;

    @DBRef
    private Operacao operacao;

    private String setor = "";

    private String status = "offline"; // online, offline, ocioso, em_producao

    @DBRef
    private Funcionario funcionarioLogado;

    private Integer producaoAtual = 0;

    private LocalDateTime ultimaAtualizacao = LocalDateTime.now();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
