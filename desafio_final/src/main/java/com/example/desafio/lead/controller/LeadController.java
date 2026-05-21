package com.example.desafio.lead.controller;

import com.example.desafio.lead.dto.LeadResponseDTO;
import com.example.desafio.lead.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Tag(name = "Leads", description = "Endpoints para consulta de leads")
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    @Operation(summary = "Listar leads com filtros e paginação")
    public Page<LeadResponseDTO> searchLeads(
            @RequestParam(required = false)
            String nome,
            @RequestParam(required = false)
            String email,
            @RequestParam(required = false)
            String origem,
            @ParameterObject Pageable pageable
    ) {

        return leadService.searchLeads(
                nome,
                email,
                origem,
                pageable
        );
    }

    @GetMapping("/origens")
    @Operation(summary = "Listar todas as origens de leads cadastradas")
    public List<String> getOrigens() {
        return leadService.findAllOrigens();
    }
}