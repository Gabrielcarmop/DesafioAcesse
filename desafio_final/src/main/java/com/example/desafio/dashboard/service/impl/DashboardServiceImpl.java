package com.example.desafio.dashboard.service.impl;

import com.example.desafio.dashboard.dto.DashboardStatsDTO;
import com.example.desafio.dashboard.service.DashboardService;
import com.example.desafio.lead.repository.LeadRepository;
import com.example.desafio.lote.domain.Lote;
import com.example.desafio.lote.domain.LoteStatus;
import com.example.desafio.lote.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final LeadRepository leadRepository;
    private final LoteRepository loteRepository;

    @Override
    public DashboardStatsDTO getStats() {
        long totalLeads = leadRepository.count();
        long totalLotes = loteRepository.count();
        long lotesProcessados = loteRepository.countByStatus(LoteStatus.COMPLETED);

        double taxaErro = calcularTaxaErro();

        return DashboardStatsDTO.builder()
                .totalLeads(totalLeads)
                .totalLotes(totalLotes)
                .lotesProcessados(lotesProcessados)
                .taxaErro(taxaErro)
                .build();
    }

    private double calcularTaxaErro() {
        var lotes = loteRepository.findAll();
        long totalLinhas = lotes.stream().mapToLong(Lote::getTotalLinhas).sum();
        long totalErros = lotes.stream().mapToLong(Lote::getLinhasErro).sum();
        return totalLinhas > 0 ? (totalErros * 100.0) / totalLinhas : 0.0;
    }
}
