package com.example.desafio.lote.repository;

import com.example.desafio.lote.domain.Lote;
import com.example.desafio.lote.domain.LoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoteRepository extends JpaRepository<Lote, UUID> {

    Page<Lote> findByStatus(LoteStatus status, Pageable pageable);

    long countByStatus(LoteStatus status);
}