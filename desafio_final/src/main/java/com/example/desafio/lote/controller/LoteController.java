package com.example.desafio.lote.controller;

import com.example.desafio.infra.sse.SseProgressService;
import com.example.desafio.lote.dto.LoteResponseDTO;
import com.example.desafio.lote.dto.LoteStatusResponseDTO;
import com.example.desafio.lote.dto.LoteSummaryDTO;
import com.example.desafio.lote.processamento.dto.LoteProcessamentoDTO;
import com.example.desafio.lote.processamento.service.LoteProcessamentoService;
import com.example.desafio.lote.service.LoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@Tag(name = "Lotes", description = "Endpoints para upload e acompanhamento de lotes CSV")
public class LoteController {

    private final LoteService loteService;
    private final SseProgressService sseProgressService;
    private final LoteProcessamentoService loteProcessamentoService;

    /**
     * POST /api/lotes
     * Aceita o arquivo, cria o lote e dispara processamento assíncrono.
     * Retorna 202 Accepted — o processamento continua em background.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de arquivo CSV para importação de leads")
    public ResponseEntity<LoteResponseDTO> upload(
            @RequestPart("file")
            @NotNull(message = "Arquivo é obrigatório")
            MultipartFile file
    ) {
        LoteResponseDTO response = loteService.upload(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * GET /api/lotes/{id}/status
     * Retorna o progresso atual do lote incluindo lista de chunks (lote_processamento).
     */
    @GetMapping("/{id}/status")
    @Operation(summary = "Consultar status, progresso e chunks de um lote")
    public ResponseEntity<LoteStatusResponseDTO> getStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(loteService.getStatus(id));
    }

    /**
     * GET /api/lotes/{id}/chunks
     * Retorna somente o detalhe dos chunks processados (tabela lote_processamento).
     */
    @GetMapping("/{id}/chunks")
    @Operation(summary = "Listar chunks processados de um lote (tabela lote_processamento)")
    public ResponseEntity<List<LoteProcessamentoDTO>> getChunks(@PathVariable UUID id) {
        return ResponseEntity.ok(loteProcessamentoService.findByLoteId(id));
    }

    /**
     * GET /api/lotes/{id}/progresso
     * Endpoint SSE — o frontend se inscreve aqui e recebe atualizações em tempo real.
     */
    @GetMapping(value = "/{id}/progresso", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream de progresso em tempo real via SSE")
    public SseEmitter streamProgresso(@PathVariable UUID id) {
        return sseProgressService.inscrever(id);
    }

    /**
     * GET /api/lotes
     * Lista todos os lotes com paginação.
     */
    @GetMapping
    @Operation(summary = "Listar todos os lotes com paginação")
    public Page<LoteSummaryDTO> getAllLotes(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return loteService.findAllLotes(pageable);
    }
}
