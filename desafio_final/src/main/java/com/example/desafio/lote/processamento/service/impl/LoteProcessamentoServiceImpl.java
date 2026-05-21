package com.example.desafio.lote.processamento.service.impl;

import com.example.desafio.lote.domain.Lote;
import com.example.desafio.lote.processamento.domain.LoteProcessamento;
import com.example.desafio.lote.processamento.dto.LoteProcessamentoDTO;
import com.example.desafio.lote.processamento.repository.LoteProcessamentoRepository;
import com.example.desafio.lote.processamento.service.LoteProcessamentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoteProcessamentoServiceImpl implements LoteProcessamentoService {

    private final LoteProcessamentoRepository repository;

    /**
     * Salva o registro numa transação própria (REQUIRES_NEW) para garantir
     * que o log do chunk seja persistido independentemente do resultado do lote pai.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LoteProcessamento registrarChunk(
            Lote lote,
            int chunkIndex,
            int totalLinhas,
            int linhasSucesso,
            int linhasDuplicadas,
            int linhasErro,
            long tempoProcessamentoMs
    ) {
        LocalDateTime agora = LocalDateTime.now();

        LoteProcessamento registro = LoteProcessamento.builder()
                .lote(lote)
                .chunkIndex(chunkIndex)
                .totalLinhas(totalLinhas)
                .linhasSucesso(linhasSucesso)
                .linhasDuplicadas(linhasDuplicadas)
                .linhasErro(linhasErro)
                .tempoProcessamentoMs(tempoProcessamentoMs)
                .iniciadoEm(agora.minusNanos(tempoProcessamentoMs * 1_000_000L))
                .finalizadoEm(agora)
                .build();

        LoteProcessamento salvo = repository.save(registro);

        log.debug("Chunk {} do lote {} registrado: {} sucesso, {} duplicados, {} erros, {}ms",
                chunkIndex, lote.getId(), linhasSucesso, linhasDuplicadas, linhasErro, tempoProcessamentoMs);

        return salvo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteProcessamentoDTO> findByLoteId(UUID loteId) {
        return repository.findByLoteIdOrderByChunkIndex(loteId)
                .stream()
                .map(lp -> new LoteProcessamentoDTO(
                        lp.getId(),
                        lp.getChunkIndex(),
                        lp.getTotalLinhas(),
                        lp.getLinhasSucesso(),
                        lp.getLinhasDuplicadas(),
                        lp.getLinhasErro(),
                        lp.getTempoProcessamentoMs(),
                        lp.getIniciadoEm(),
                        lp.getFinalizadoEm()
                ))
                .toList();
    }
}
