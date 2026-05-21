package com.example.desafio.lote.mapper;

import com.example.desafio.lote.domain.Lote;
import com.example.desafio.lote.dto.LoteSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class LoteMapper {

    public LoteSummaryDTO toSummaryDTO(Lote lote) {
        return LoteSummaryDTO.builder()
                .id(lote.getId())
                .nomeArquivo(lote.getNomeArquivo())
                .status(lote.getStatus())
                .totalLinhas(lote.getTotalLinhas())
                .linhasSucesso(lote.getLinhasSucesso())
                .linhasErro(lote.getLinhasErro())
                .createdAt(lote.getCreatedAt())
                .updatedAt(lote.getUpdatedAt())
                .build();
    }
}