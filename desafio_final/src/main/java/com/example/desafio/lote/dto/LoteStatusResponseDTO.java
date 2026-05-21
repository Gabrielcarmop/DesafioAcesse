package com.example.desafio.lote.dto;

import com.example.desafio.lote.domain.LoteStatus;
import com.example.desafio.lote.processamento.dto.LoteProcessamentoDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Retorno completo do endpoint GET /api/lotes/{id}/status.
 *
 * Inclui os dados agregados do lote e o detalhe de cada chunk registrado
 * em lote_processamento, permitindo acompanhamento granular do progresso.
 */
public record LoteStatusResponseDTO(
        UUID id,
        String nomeArquivo,
        LoteStatus status,
        String statusDescricao,
        Integer totalLinhas,
        Integer linhasProcessadas,
        Integer linhasSucesso,
        Integer linhasErro,
        Double progressoPercentual,
        Long tempoTotalMs,
        LocalDateTime iniciadoEm,
        LocalDateTime finalizadoEm,
        LocalDateTime createdAt,

        /** Detalhe de cada chunk processado (tabela lote_processamento). */
        List<LoteProcessamentoDTO> chunks
) {}
