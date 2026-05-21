package com.example.desafio.lote.domain;

public enum LoteStatus {
    PENDING("Aguardando processamento"),
    PROCESSING("Processando"),
    COMPLETED("Concluído"),
    FAILED("Falhou");

    private final String descricao;

    LoteStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
