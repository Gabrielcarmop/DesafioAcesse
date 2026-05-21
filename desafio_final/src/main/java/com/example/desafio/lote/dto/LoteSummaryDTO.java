package com.example.desafio.lote.dto;

import com.example.desafio.lote.domain.LoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteSummaryDTO {
    private UUID id;
    private String nomeArquivo;
    private LoteStatus status;
    private Integer totalLinhas;
    private Integer linhasSucesso;
    private Integer linhasErro;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}