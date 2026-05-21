package com.example.desafio.lead.domain;

import com.example.desafio.lote.domain.Lote;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 50)
    private String telefone;

    @Column(nullable = false, length = 100)
    private String origem;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
