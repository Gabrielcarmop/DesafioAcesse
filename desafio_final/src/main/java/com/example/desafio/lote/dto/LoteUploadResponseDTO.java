package com.example.desafio.lote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteUploadResponseDTO {
    private UUID loteId;
    private String mensagem;
    private String status;
}
