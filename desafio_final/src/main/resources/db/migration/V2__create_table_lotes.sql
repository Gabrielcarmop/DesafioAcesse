CREATE TABLE lotes (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       nome_arquivo VARCHAR(255) NOT NULL,
                       status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
                       total_linhas INTEGER DEFAULT 0,
                       linhas_processadas INTEGER DEFAULT 0,
                       linhas_sucesso INTEGER DEFAULT 0,
                       linhas_erro INTEGER DEFAULT 0,
                       tempo_total_ms BIGINT,
                       iniciado_em TIMESTAMP,
                       finalizado_em TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMP
);

CREATE INDEX idx_lotes_status ON lotes(status);