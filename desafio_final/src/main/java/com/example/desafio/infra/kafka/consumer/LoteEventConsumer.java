package com.example.desafio.infra.kafka.consumer;

import com.example.desafio.infra.kafka.event.ChunkConcluidoEvent;
import com.example.desafio.infra.kafka.event.LoteFinalizadoEvent;
import com.example.desafio.infra.kafka.event.LoteIniciadoEvent;
import com.example.desafio.infra.kafka.producer.LoteEventProducer;
import com.example.desafio.infra.sse.SseProgressService;
import com.example.desafio.infra.sse.SseProgressService.ProgressoPayload;
import com.example.desafio.lote.domain.Lote;
import com.example.desafio.lote.domain.LoteStatus;
import com.example.desafio.lote.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer Kafka que consolida os eventos de ciclo de vida do lote
 * e dispara notificações SSE em tempo real para o frontend.
 *
 * Fluxo:
 *   lote.iniciado        → SSE com progresso 0%
 *   lote.chunk.concluido → SSE com progresso real buscado do banco
 *   lote.finalizado      → SSE com 100% + fecha a conexão SSE
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoteEventConsumer {

    private final SseProgressService sseProgressService;
    private final LoteRepository     loteRepository;

    // ── lote.iniciado ──────────────────────────────────────────────────────

    @KafkaListener(
            topics = LoteEventProducer.TOPIC_LOTE_INICIADO,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onLoteIniciado(@Payload LoteIniciadoEvent event, Acknowledgment ack) {
        log.info("Lote iniciado: id={} arquivo={} totalLinhas={}",
                event.loteId(), event.nomeArquivo(), event.totalLinhas());

        ProgressoPayload payload = new ProgressoPayload(
                event.loteId(),
                LoteStatus.PROCESSING,
                LoteStatus.PROCESSING.getDescricao(),
                event.totalLinhas(),
                0,   // linhasProcessadas
                0,   // linhasSucesso
                0,   // linhasErro
                0.0, // progressoPercentual
                false
        );

        sseProgressService.notificar(event.loteId(), payload, false);
        ack.acknowledge();
    }

    // ── lote.chunk.concluido ───────────────────────────────────────────────

    @KafkaListener(
            topics = LoteEventProducer.TOPIC_CHUNK_CONCLUIDO,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onChunkConcluido(@Payload ChunkConcluidoEvent event, Acknowledgment ack) {
        log.debug("Chunk concluído: loteId={} chunk={} salvos={} duplicatas={} erros={}",
                event.loteId(), event.chunkIndex(), event.salvos(), event.duplicatas(), event.erros());

        // Busca o estado atualizado do lote para calcular o progresso real
        loteRepository.findById(event.loteId()).ifPresent(lote -> {
            ProgressoPayload payload = buildPayload(lote, false);
            sseProgressService.notificar(event.loteId(), payload, false);
        });

        ack.acknowledge();
    }

    // ── lote.finalizado ────────────────────────────────────────────────────

    @KafkaListener(
            topics = LoteEventProducer.TOPIC_LOTE_FINALIZADO,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onLoteFinalizado(@Payload LoteFinalizadoEvent event, Acknowledgment ack) {
        log.info("Lote finalizado: id={} status={} sucesso={} erros={} taxaErro={}% tempo={}ms",
                event.loteId(), event.status(), event.linhasSucesso(),
                event.linhasErro(), String.format("%.2f", event.taxaErro()), event.tempoTotalMs());

        // Monta payload com dados definitivos do evento (banco já foi atualizado)
        ProgressoPayload payload = new ProgressoPayload(
                event.loteId(),
                event.status(),
                event.status().getDescricao(),
                event.totalLinhas(),
                event.totalLinhas(),    // processadas = total ao finalizar
                event.linhasSucesso(),
                event.linhasErro(),
                100.0,
                true
        );

        sseProgressService.notificar(event.loteId(), payload, true);
        ack.acknowledge();
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private ProgressoPayload buildPayload(Lote lote, boolean finalizado) {
        return new ProgressoPayload(
                lote.getId(),
                lote.getStatus(),
                lote.getStatus().getDescricao(),
                lote.getTotalLinhas()       != null ? lote.getTotalLinhas()       : 0,
                lote.getLinhasProcessadas() != null ? lote.getLinhasProcessadas() : 0,
                lote.getLinhasSucesso()     != null ? lote.getLinhasSucesso()     : 0,
                lote.getLinhasErro()        != null ? lote.getLinhasErro()        : 0,
                lote.calcularProgresso(),
                finalizado
        );
    }
}
