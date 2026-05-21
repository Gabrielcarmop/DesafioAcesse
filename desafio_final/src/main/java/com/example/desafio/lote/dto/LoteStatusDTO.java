package com.example.desafio.lote.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LoteStatusDTO {
    private UUID id;
    private String status;
    private Integer totalLinhas;
    private Integer linhasProcessadas;
    private Integer linhasSucesso;
    private Integer linhasErro;
    private Double progressoPercentual;
}