package com.example.desafio.infra.sse;

import com.example.desafio.lote.domain.LoteStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gerencia conexões SSE e faz broadcast de progresso para o frontend.
 *
 * Fluxo:
 *   1. Frontend abre GET /api/lotes/{id}/progresso → inscrever()
 *   2. Consumer Kafka chama notificar() a cada chunk concluído
 *   3. Consumer chama notificar() com finalizado=true ao receber lote.finalizado
 *   4. Emitter é fechado e removido do registry
 *
 * Thread-safety: ConcurrentHashMap + CopyOnWriteArrayList — sem locks explícitos.
 * Timeout: 10 min (suficiente para lotes de 100k linhas).
 */
@Slf4j
@Service
public class SseProgressService {

    private static final long TIMEOUT_MS = 10 * 60 * 1000L;

    private final Map<UUID, List<SseEmitter>> registry = new ConcurrentHashMap<>();

    // ── Inscrição ──────────────────────────────────────────────────────────

    public SseEmitter inscrever(UUID loteId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);

        registry.computeIfAbsent(loteId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable remover = () -> removerEmitter(loteId, emitter);
        emitter.onCompletion(remover);
        emitter.onTimeout(remover);
        emitter.onError(ex -> {
            log.debug("SSE error lote={}: {}", loteId, ex.getMessage());
            remover.run();
        });

        log.debug("Cliente SSE inscrito no lote {}", loteId);
        return emitter;
    }

    // ── Notificação ────────────────────────────────────────────────────────

    /**
     * Envia um evento de progresso para todos os clientes inscritos no lote.
     *
     * @param loteId        identificador do lote
     * @param payload       dados completos do progresso
     * @param finalizado    se true, fecha todos os emitters após o envio
     */
    public void notificar(UUID loteId, ProgressoPayload payload, boolean finalizado) {
        List<SseEmitter> emitters = registry.get(loteId);
        if (emitters == null || emitters.isEmpty()) return;

        String eventName = finalizado ? "lote.finalizado" : "lote.progresso";

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(payload));

                if (finalizado) emitter.complete();

            } catch (IOException e) {
                log.debug("Falha SSE lote={}: {}", loteId, e.getMessage());
                removerEmitter(loteId, emitter);
            }
        });

        if (finalizado) registry.remove(loteId);
    }

    // ── Utilitários ────────────────────────────────────────────────────────

    private void removerEmitter(UUID loteId, SseEmitter emitter) {
        List<SseEmitter> lista = registry.get(loteId);
        if (lista != null) {
            lista.remove(emitter);
            if (lista.isEmpty()) registry.remove(loteId);
        }
    }

    public boolean temInscritos(UUID loteId) {
        List<SseEmitter> lista = registry.get(loteId);
        return lista != null && !lista.isEmpty();
    }

    // ── Payload ────────────────────────────────────────────────────────────

    /**
     * Payload enviado via SSE ao frontend.
     * Espelha os campos de LoteStatusResponseDTO para reutilização direta no frontend.
     */
    public record ProgressoPayload(
            UUID loteId,
            LoteStatus status,
            String statusDescricao,
            int totalLinhas,
            int linhasProcessadas,
            int linhasSucesso,
            int linhasErro,
            double progressoPercentual,
            boolean finalizado
    ) {}
}
