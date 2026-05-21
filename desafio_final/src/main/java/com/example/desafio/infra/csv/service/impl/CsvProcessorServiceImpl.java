package com.example.desafio.infra.csv.service.impl;

import com.example.desafio.infra.csv.CsvParser;
import com.example.desafio.infra.kafka.event.ChunkConcluidoEvent;
import com.example.desafio.infra.kafka.event.LoteFinalizadoEvent;
import com.example.desafio.infra.kafka.producer.LoteEventProducer;
import com.example.desafio.infra.csv.service.CsvProcessorService;
import com.example.desafio.lead.dto.LeadRequestDTO;
import com.example.desafio.lead.service.LeadBatchService;
import com.example.desafio.lote.domain.Lote;
import com.example.desafio.lote.processamento.service.LoteProcessamentoService;
import com.example.desafio.lote.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvProcessorServiceImpl implements CsvProcessorService {

    private final CsvParser csvParser;
    private final LeadBatchService leadBatchService;
    private final LoteRepository loteRepository;
    private final LoteEventProducer eventProducer;
    private final LoteProcessamentoService loteProcessamentoService;

    @Async("csvTaskExecutor")
    @Override
    public void processar(UUID loteId, Path filePath) {
        LocalDateTime inicio = LocalDateTime.now();

        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalStateException("Lote não encontrado: " + loteId));

        try {
            lote.iniciarProcessamento();
            loteRepository.save(lote);

            AtomicInteger chunkIndex    = new AtomicInteger(0);
            AtomicInteger totalSalvos  = new AtomicInteger(0);
            AtomicInteger totalErros   = new AtomicInteger(0);

            CsvParser.ParseResult resultado = csvParser.processarEmChunks(filePath, chunk -> {
                int idx = chunkIndex.getAndIncrement();
                processarChunk(lote, chunk, idx, totalSalvos, totalErros);
            });

            lote.setTotalLinhas(resultado.totalLidas());
            lote.setLinhasProcessadas(resultado.totalLidas());
            lote.setLinhasSucesso(totalSalvos.get());
            lote.setLinhasErro(totalErros.get() + resultado.totalIgnoradas());
            lote.setTempoTotalMs(Duration.between(inicio, LocalDateTime.now()).toMillis());
            lote.finalizarProcessamento();
            loteRepository.save(lote);

            double taxaErro = lote.getTotalLinhas() > 0
                    ? (lote.getLinhasErro() * 100.0) / lote.getTotalLinhas()
                    : 0.0;

            eventProducer.publicarLoteFinalizado(new LoteFinalizadoEvent(
                    loteId,
                    lote.getStatus(),
                    lote.getTotalLinhas(),
                    lote.getLinhasSucesso(),
                    lote.getLinhasErro(),
                    taxaErro,
                    lote.getTempoTotalMs(),
                    lote.getFinalizadoEm()
            ));

            log.info("Lote {} concluído: {} válidas, {} salvas, {} erros, {}ms",
                    loteId, resultado.totalValidas(), totalSalvos.get(),
                    totalErros.get(), lote.getTempoTotalMs());

        } catch (Exception ex) {
            log.error("Erro crítico ao processar lote {}: {}", loteId, ex.getMessage(), ex);

            lote.marcarErro();
            loteRepository.save(lote);

            eventProducer.publicarLoteFinalizado(new LoteFinalizadoEvent(
                    loteId, lote.getStatus(), lote.getTotalLinhas(),
                    lote.getLinhasSucesso(), lote.getLinhasErro(),
                    100.0, Duration.between(inicio, LocalDateTime.now()).toMillis(),
                    lote.getFinalizadoEm()
            ));
        }
    }

    private void processarChunk(Lote lote, List<LeadRequestDTO> chunk,
                                int idx, AtomicInteger totalSalvos, AtomicInteger totalErros) {
        log.debug("Processando chunk {} do lote {} ({} itens)", idx, lote.getId(), chunk.size());

        long inicioChunk = System.currentTimeMillis();

        LeadBatchService.BatchResult batchResult = leadBatchService.salvarChunkDetalhado(lote.getId(), chunk);

        long tempoChunkMs = System.currentTimeMillis() - inicioChunk;

        totalSalvos.addAndGet(batchResult.salvos());
        totalErros.addAndGet(batchResult.erros());

        // ── Persiste o registro na tabela lote_processamento ─────────────────
        loteProcessamentoService.registrarChunk(
                lote,
                idx,
                chunk.size(),
                batchResult.salvos(),
                batchResult.duplicados(),
                batchResult.erros(),
                tempoChunkMs
        );

        // ── Publica evento Kafka de chunk concluído ───────────────────────────
        eventProducer.publicarChunkConcluido(new ChunkConcluidoEvent(
                lote.getId(), idx, chunk.size(),
                batchResult.salvos(), batchResult.duplicados(), batchResult.erros(),
                LocalDateTime.now()
        ));
    }
}
