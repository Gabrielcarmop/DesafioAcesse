package com.example.desafio.infra.kafka.event;

import com.example.desafio.lote.domain.LoteStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoteFinalizadoEvent(
        UUID loteId,
        LoteStatus status,
        int totalLinhas,
        int linhasSucesso,
        int linhasErro,
        double taxaErro,
        long tempoTotalMs,
        LocalDateTime finalizadoEm
) {}
