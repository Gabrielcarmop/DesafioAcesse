-- =============================================================================
-- V4 - Tabela lote_processamento
--
-- Registra o resultado de cada chunk processado dentro de um lote.
-- Permite auditoria granular: quantas linhas foram tentadas, quantas salvas,
-- quantas duplicadas, quantas com erro, e quanto tempo cada chunk levou.
-- =============================================================================

CREATE TABLE lote_processamento (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    lote_id                 UUID        NOT NULL,
    chunk_index             INTEGER     NOT NULL,
    total_linhas            INTEGER     NOT NULL DEFAULT 0,
    linhas_sucesso          INTEGER     NOT NULL DEFAULT 0,
    linhas_duplicadas       INTEGER     NOT NULL DEFAULT 0,
    linhas_erro             INTEGER     NOT NULL DEFAULT 0,
    tempo_processamento_ms  BIGINT,
    iniciado_em             TIMESTAMP,
    finalizado_em           TIMESTAMP,
    created_at              TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_lote_processamento_lote
        FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,

    CONSTRAINT uq_lote_processamento_chunk
        UNIQUE (lote_id, chunk_index)
);

CREATE INDEX idx_lote_processamento_lote_id    ON lote_processamento(lote_id);
CREATE INDEX idx_lote_processamento_chunk      ON lote_processamento(lote_id, chunk_index);

COMMENT ON TABLE lote_processamento IS
    'Registra o resultado de cada chunk (partição) processado de um lote CSV. '
    'Cada linha representa uma execução de chunk pelo pool de threads assíncrono.';

COMMENT ON COLUMN lote_processamento.chunk_index IS
    'Índice sequencial do chunk dentro do lote (0-based).';

COMMENT ON COLUMN lote_processamento.linhas_duplicadas IS
    'Linhas ignoradas por violação da unique constraint uk_leads_email_origem (deduplicação).';
