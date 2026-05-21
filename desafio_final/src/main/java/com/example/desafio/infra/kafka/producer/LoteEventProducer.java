package com.example.desafio.infra.kafka.producer;

import com.example.desafio.infra.kafka.event.ChunkConcluidoEvent;
import com.example.desafio.infra.kafka.event.LoteFinalizadoEvent;
import com.example.desafio.infra.kafka.event.LoteIniciadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de ciclo de vida do lote nos tópicos Kafka.
 *
 * A chave da mensagem é sempre o loteId em string, garantindo que todos os eventos
 * de um mesmo lote caiam na mesma partição e sejam consumidos em ordem.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoteEventProducer {

    public static final String TOPIC_LOTE_INICIADO     = "lote.iniciado";
    public static final String TOPIC_CHUNK_CONCLUIDO   = "lote.chunk.concluido";
    public static final String TOPIC_LOTE_FINALIZADO   = "lote.finalizado";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publicarLoteIniciado(LoteIniciadoEvent event) {
        enviar(TOPIC_LOTE_INICIADO, event.loteId().toString(), event);
    }

    public void publicarChunkConcluido(ChunkConcluidoEvent event) {
        enviar(TOPIC_CHUNK_CONCLUIDO, event.loteId().toString(), event);
    }

    public void publicarLoteFinalizado(LoteFinalizadoEvent event) {
        enviar(TOPIC_LOTE_FINALIZADO, event.loteId().toString(), event);
    }

    private void enviar(String topico, String chave, Object payload) {
        kafkaTemplate.send(topico, chave, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Falha ao publicar evento no tópico={} chave={}: {}", topico, chave, ex.getMessage());
                    } else {
                        log.debug("Evento publicado: tópico={} chave={} offset={}",
                                topico, chave, result.getRecordMetadata().offset());
                    }
                });
    }
}
