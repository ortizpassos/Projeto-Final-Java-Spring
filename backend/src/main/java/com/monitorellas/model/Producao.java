package com.monitorellas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "producao")
public class Producao {

    @Id
    private String id;

    @DBRef
    private Funcionario funcionario;

    @DBRef
    private Dispositivo dispositivo;

    @DBRef
    private Operacao operacao;

    private Integer quantidade;

    private Integer tempoProducao; // em segundos ou milissegundos

    private LocalDateTime dataHora = LocalDateTime.now();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
