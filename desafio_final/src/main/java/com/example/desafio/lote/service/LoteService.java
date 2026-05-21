package com.example.desafio.lote.service;

import com.example.desafio.lote.dto.LoteResponseDTO;
import com.example.desafio.lote.dto.LoteStatusResponseDTO;
import com.example.desafio.lote.dto.LoteSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface LoteService {

    Page<LoteSummaryDTO> findAllLotes(Pageable pageable);

    LoteResponseDTO upload(MultipartFile file);

    LoteStatusResponseDTO getStatus(UUID id);
}