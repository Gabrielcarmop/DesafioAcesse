package com.example.desafio.lead.mapper;

import com.example.desafio.lead.domain.Lead;
import com.example.desafio.lead.dto.LeadResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class LeadMapper {

    public LeadResponseDTO toDto(Lead lead) {
        return LeadResponseDTO.builder()
                .id(lead.getId())
                .nome(lead.getNome())
                .email(lead.getEmail())
                .telefone(lead.getTelefone())
                .origem(lead.getOrigem())
                .dataCadastro(lead.getDataCadastro() != null ?
                        lead.getDataCadastro().toString() : null)
                .build();
    }
}