package com.example.desafio.lead.repository;

import com.example.desafio.lead.domain.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Page<Lead> findByLoteId(UUID loteId, Pageable pageable);

    @Query("""
    SELECT l FROM Lead l
    WHERE (:nome IS NULL OR l.nome ILIKE %:nome%)
      AND (:email IS NULL OR l.email ILIKE %:email%)
      AND (:origem IS NULL OR l.origem ILIKE %:origem%)
        """)
    Page<Lead> searchLeads(@Param("nome") String nome,
                           @Param("email") String email,
                           @Param("origem") String origem,
                           Pageable pageable);

    @Query("SELECT DISTINCT l.origem FROM Lead l WHERE l.origem IS NOT NULL ORDER BY l.origem")
    List<String> findDistinctOrigens();
}
