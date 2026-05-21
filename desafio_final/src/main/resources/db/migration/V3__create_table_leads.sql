CREATE TABLE leads (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       nome VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       telefone VARCHAR(50),
                       origem VARCHAR(100) NOT NULL,
                       data_cadastro TIMESTAMP,
                       lote_id UUID NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       CONSTRAINT fk_leads_lote FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,
                       CONSTRAINT uk_leads_email_origem UNIQUE(email, origem)
);

CREATE INDEX idx_leads_nome ON leads(nome);
CREATE INDEX idx_leads_email ON leads(email);
CREATE INDEX idx_leads_origem ON leads(origem);
CREATE INDEX idx_leads_lote_id ON leads(lote_id);