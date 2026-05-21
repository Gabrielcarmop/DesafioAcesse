package com.example.desafio.lead.service.impl;

import com.example.desafio.lead.domain.Lead;
import com.example.desafio.lead.dto.LeadRequestDTO;
import com.example.desafio.lead.repository.LeadRepository;
import com.example.desafio.lead.service.LeadBatchService;
import com.example.desafio.lote.domain.Lote;
import com.example.desafio.lote.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadBatchServiceImpl implements LeadBatchService {

    private final LeadRepository leadRepository;
    private final LoteRepository loteRepository;

    @Override
    public int salvarChunk(UUID loteId, List<LeadRequestDTO> chunk) {
        return salvarChunkDetalhado(loteId, chunk).salvos();
    }

    @Override
    public BatchResult salvarChunkDetalhado(UUID loteId, List<LeadRequestDTO> chunk) {
        Lote lote = loteRepository.findById(loteId).orElseThrow(
                () -> new IllegalArgumentException("Lote não encontrado: " + loteId)
        );

        int salvos     = 0;
        int duplicados = 0;
        int erros      = 0;

        for (LeadRequestDTO dto : chunk) {
            SalvarResultado resultado = tentarSalvarLead(dto, lote);
            switch (resultado) {
                case SALVO      -> salvos++;
                case DUPLICADO  -> duplicados++;
                case ERRO       -> erros++;
            }
        }

        return new BatchResult(salvos, duplicados, erros);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SalvarResultado tentarSalvarLead(LeadRequestDTO dto, Lote lote) {
        try {
            Lead lead = Lead.builder()
                    .nome(dto.getNome())
                    .email(dto.getEmail().toLowerCase().trim())
                    .telefone(dto.getTelefone())
                    .origem(dto.getOrigem())
                    .dataCadastro(dto.getDataCadastro())
                    .lote(lote)
                    .build();

            leadRepository.save(lead);
            return SalvarResultado.SALVO;

        } catch (DataIntegrityViolationException e) {
            log.debug("Lead duplicado ignorado: email={} origem={}", dto.getEmail(), dto.getOrigem());
            return SalvarResultado.DUPLICADO;
        } catch (Exception e) {
            log.warn("Erro inesperado ao salvar lead email={}: {}", dto.getEmail(), e.getMessage());
            return SalvarResultado.ERRO;
        }
    }

    private enum SalvarResultado { SALVO, DUPLICADO, ERRO }
}
