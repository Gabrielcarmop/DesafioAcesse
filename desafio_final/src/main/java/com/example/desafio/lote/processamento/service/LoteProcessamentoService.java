package com.example.desafio.lote.processamento.service;

import com.example.desafio.lote.domain.Lote;
import com.example.desafio.lote.processamento.domain.LoteProcessamento;
import com.example.desafio.lote.processamento.dto.LoteProcessamentoDTO;

import java.util.List;
import java.util.UUID;

public interface LoteProcessamentoService {

    /**
     * Persiste o registro de um chunk concluído.
     *
     * @param lote                 entidade do lote pai
     * @param chunkIndex           índice sequencial do chunk (0-based)
     * @param totalLinhas          total de linhas tentadas no chunk
     * @param linhasSucesso        linhas persistidas com sucesso
     * @param linhasDuplicadas     linhas ignoradas por duplicidade
     * @param linhasErro           linhas que falharam por outros erros
     * @param tempoProcessamentoMs tempo de execução do chunk em ms
     * @return entidade salva
     */
    LoteProcessamento registrarChunk(
            Lote lote,
            int chunkIndex,
            int totalLinhas,
            int linhasSucesso,
            int linhasDuplicadas,
            int linhasErro,
            long tempoProcessamentoMs
    );

    /** Retorna todos os chunks de um lote ordenados por índice. */
    List<LoteProcessamentoDTO> findByLoteId(UUID loteId);
}
