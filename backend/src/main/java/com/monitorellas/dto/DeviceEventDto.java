package com.monitorellas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEventDto {
    private String deviceToken;
    private String codigo;
    private String funcionarioId;
    private String operacaoId;
    private Integer quantidade;
    private Integer tempoProducao;
}
