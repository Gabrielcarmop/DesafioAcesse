package com.example.desafio.lote.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lotes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LoteStatus status;

    @Builder.Default
    @Column(name = "total_linhas")
    private Integer totalLinhas = 0;

    @Builder.Default
    @Column(name = "linhas_processadas")
    private Integer linhasProcessadas = 0;

    @Builder.Default
    @Column(name = "linhas_sucesso")
    private Integer linhasSucesso = 0;

    @Builder.Default
    @Column(name = "linhas_erro")
    private Integer linhasErro = 0;

    @Column(name = "tempo_total_ms")
    private Long tempoTotalMs;

    @Column(name = "iniciado_em")
    private LocalDateTime iniciadoEm;

    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void iniciarProcessamento() {
        this.status = LoteStatus.PROCESSING;
        this.iniciadoEm = LocalDateTime.now();
    }

    public void finalizarProcessamento() {
        this.status = LoteStatus.COMPLETED;
        this.finalizadoEm = LocalDateTime.now();
    }

    public void marcarErro() {
        this.status = LoteStatus.FAILED;
        this.finalizadoEm = LocalDateTime.now();
    }

    public double calcularProgresso() {
        if (totalLinhas == null || totalLinhas == 0) return 0.0;
        return Math.round((linhasProcessadas * 100.0 / totalLinhas) * 100.0) / 100.0;
    }
}
