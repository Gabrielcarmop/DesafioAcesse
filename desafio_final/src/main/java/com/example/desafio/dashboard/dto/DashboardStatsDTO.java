package com.example.desafio.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    private Long totalLeads;
    private Long totalLotes;
    private Long lotesProcessados;
    private Double taxaErro;
}
