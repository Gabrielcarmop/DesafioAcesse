package com.example.desafio.lote.processamento.domain;

import com.example.desafio.lote.domain.Lote;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra o resultado de cada chunk processado dentro de um lote.
 *
 * Cada linha desta tabela representa a execução de um fragmento (chunk) do CSV,
 * permitindo auditoria granular: quantas linhas foram tentadas, quantas salvas com
 * sucesso, quantas rejeitadas por erro ou duplicidade, e quanto tempo levou.
 *
 * Esta entidade é exigida pelo desafio como terceira entidade do modelo de domínio,
 * complementando Lote (cabeçalho do lote) e Lead (dado individual importado).
 */
@Entity
@Table(
    name = "lote_processamento",
    indexes = {
        @Index(name = "idx_lote_processamento_lote_id", columnList = "lote_id"),
        @Index(name = "idx_lote_processamento_chunk", columnList = "lote_id, chunk_index")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteProcessamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Lote ao qual este registro de chunk pertence. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    /** Índice sequencial do chunk (0-based) dentro do lote. */
    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    /** Total de linhas contidas neste chunk antes de qualquer filtragem. */
    @Column(name = "total_linhas", nullable = false)
    private Integer totalLinhas;

    /** Linhas persistidas com sucesso no banco. */
    @Column(name = "linhas_sucesso", nullable = false)
    private Integer linhasSucesso;

    /** Linhas rejeitadas por duplicidade (unique constraint). */
    @Column(name = "linhas_duplicadas", nullable = false)
    private Integer linhasDuplicadas;

    /** Linhas que falharam por qualquer outro motivo (validação, erro de banco etc.). */
    @Column(name = "linhas_erro", nullable = false)
    private Integer linhasErro;

    /** Tempo de processamento deste chunk em milissegundos. */
    @Column(name = "tempo_processamento_ms")
    private Long tempoProcessamentoMs;

    /** Momento em que o chunk começou a ser processado. */
    @Column(name = "iniciado_em")
    private LocalDateTime iniciadoEm;

    /** Momento em que o processamento deste chunk foi concluído. */
    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    /** Registro automático da criação da linha. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
