package com.example.desafio.lote.processamento.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa o resultado de um único chunk dentro de um lote,
 * retornado pelo endpoint GET /api/lotes/{id}/status na lista de chunks.
 */
public record LoteProcessamentoDTO(
        UUID id,
        Integer chunkIndex,
        Integer totalLinhas,
        Integer linhasSucesso,
        Integer linhasDuplicadas,
        Integer linhasErro,
        Long tempoProcessamentoMs,
        LocalDateTime iniciadoEm,
        LocalDateTime finalizadoEm
) {}
