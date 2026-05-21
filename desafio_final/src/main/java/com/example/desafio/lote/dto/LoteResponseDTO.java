package com.example.desafio.lote.dto;

import com.example.desafio.lote.domain.LoteStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoteResponseDTO(
        UUID id,
        String nomeArquivo,
        LoteStatus status,
        LocalDateTime createdAt
) {}
