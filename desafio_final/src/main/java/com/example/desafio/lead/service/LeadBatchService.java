package com.example.desafio.lead.service;

import java.util.List;
import java.util.UUID;
import com.example.desafio.lead.dto.LeadRequestDTO;

public interface LeadBatchService {

    /**
     * Salva um chunk de leads e retorna o número de registros salvos com sucesso.
     * Mantido por retrocompatibilidade.
     */
    int salvarChunk(UUID loteId, List<LeadRequestDTO> chunk);

    /**
     * Salva um chunk de leads e retorna resultado detalhado: salvos, duplicados e erros.
     * Usado pelo processador para registrar na tabela lote_processamento.
     */
    BatchResult salvarChunkDetalhado(UUID loteId, List<LeadRequestDTO> chunk);

    /**
     * Resultado detalhado do processamento de um chunk.
     *
     * @param salvos     linhas persistidas com sucesso
     * @param duplicados linhas ignoradas por unique constraint (deduplicação)
     * @param erros      linhas que falharam por outros erros
     */
    record BatchResult(int salvos, int duplicados, int erros) {
        public int total() { return salvos + duplicados + erros; }
    }
}
