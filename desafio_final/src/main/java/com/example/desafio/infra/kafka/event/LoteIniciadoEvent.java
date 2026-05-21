package com.example.desafio.infra.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoteIniciadoEvent(
        UUID loteId,
        String nomeArquivo,
        int totalLinhas,
        LocalDateTime iniciadoEm
) {}
