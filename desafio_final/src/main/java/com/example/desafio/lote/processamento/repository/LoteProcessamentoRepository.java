package com.example.desafio.lote.processamento.repository;

import com.example.desafio.lote.processamento.domain.LoteProcessamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoteProcessamentoRepository extends JpaRepository<LoteProcessamento, UUID> {

    /** Retorna todos os chunks de um lote ordenados por índice. */
    List<LoteProcessamento> findByLoteIdOrderByChunkIndex(UUID loteId);

    /** Total de chunks registrados para um lote. */
    long countByLoteId(UUID loteId);

    /** Soma as linhas salvas com sucesso em todos os chunks de um lote. */
    @Query("SELECT COALESCE(SUM(lp.linhasSucesso), 0) FROM LoteProcessamento lp WHERE lp.lote.id = :loteId")
    long sumLinhasSucessoByLoteId(@Param("loteId") UUID loteId);

    /** Soma as linhas com erro em todos os chunks de um lote. */
    @Query("SELECT COALESCE(SUM(lp.linhasErro), 0) FROM LoteProcessamento lp WHERE lp.lote.id = :loteId")
    long sumLinhasErroByLoteId(@Param("loteId") UUID loteId);

    /** Soma o tempo total de processamento de todos os chunks de um lote (ms). */
    @Query("SELECT COALESCE(SUM(lp.tempoProcessamentoMs), 0) FROM LoteProcessamento lp WHERE lp.lote.id = :loteId")
    long sumTempoProcessamentoByLoteId(@Param("loteId") UUID loteId);
}
