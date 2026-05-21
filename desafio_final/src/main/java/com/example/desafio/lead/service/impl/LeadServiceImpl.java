package com.example.desafio.lead.service.impl;

import com.example.desafio.lead.domain.Lead;
import com.example.desafio.lead.dto.LeadResponseDTO;
import com.example.desafio.lead.mapper.LeadMapper;
import com.example.desafio.lead.repository.LeadRepository;
import com.example.desafio.lead.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;

    @Override
    public Page<LeadResponseDTO> searchLeads(String nome, String email, String origem, Pageable pageable) {
        Page<Lead> leads = leadRepository.searchLeads(nome, email, origem, pageable);
        return leads.map(leadMapper::toDto);
    }

    @Override
    public List<String> findAllOrigens() {
        return leadRepository.findDistinctOrigens();
    }
}
