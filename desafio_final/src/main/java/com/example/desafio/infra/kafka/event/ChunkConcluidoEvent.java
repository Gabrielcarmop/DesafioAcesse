package com.example.desafio.infra.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChunkConcluidoEvent(
        UUID loteId,
        int chunkIndex,
        int totalNoChunk,
        int salvos,
        int duplicatas,
        int erros,
        LocalDateTime concluidoEm
) {}
