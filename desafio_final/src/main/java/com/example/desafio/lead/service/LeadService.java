package com.example.desafio.lead.service;

import com.example.desafio.lead.dto.LeadResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeadService {

    Page<LeadResponseDTO> searchLeads(String nome, String email, String origem, Pageable pageable);

    List<String> findAllOrigens();
}
