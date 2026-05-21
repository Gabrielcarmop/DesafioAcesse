package com.example.desafio.lote.service.impl;

import com.example.desafio.core.exception.BusinessException;
import com.example.desafio.infra.csv.CsvParser;
import com.example.desafio.infra.csv.CsvValidator;
import com.example.desafio.infra.csv.service.CsvProcessorService;
import com.example.desafio.infra.kafka.event.LoteIniciadoEvent;
import com.example.desafio.infra.kafka.producer.LoteEventProducer;
import com.example.desafio.lote.domain.Lote;
import com.example.desafio.lote.domain.LoteStatus;
import com.example.desafio.lote.dto.LoteResponseDTO;
import com.example.desafio.lote.dto.LoteStatusResponseDTO;
import com.example.desafio.lote.dto.LoteSummaryDTO;
import com.example.desafio.lote.mapper.LoteMapper;
import com.example.desafio.lote.processamento.dto.LoteProcessamentoDTO;
import com.example.desafio.lote.processamento.service.LoteProcessamentoService;
import com.example.desafio.lote.repository.LoteRepository;
import com.example.desafio.lote.service.LoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoteServiceImpl implements LoteService {

    private final LoteRepository loteRepository;
    private final CsvValidator csvValidator;
    private final LoteMapper loteMapper;
    private final CsvParser csvParser;
    private final CsvProcessorService csvProcessorService;
    private final LoteEventProducer eventProducer;
    private final LoteProcessamentoService loteProcessamentoService;

    @Value("${app.upload.diretorio-temporario:/tmp/leads-uploads}")
    private String uploadDir;

    @Override
    public Page<LoteSummaryDTO> findAllLotes(Pageable pageable) {
        return loteRepository.findAll(pageable).map(loteMapper::toSummaryDTO);
    }

    @Override
    @Transactional
    public LoteResponseDTO upload(MultipartFile file) {
        csvValidator.validate(file);

        int totalLinhas = csvParser.contarLinhasDados(file);

        Lote lote = loteRepository.save(
                Lote.builder()
                        .nomeArquivo(file.getOriginalFilename())
                        .status(LoteStatus.PENDING)
                        .totalLinhas(totalLinhas)
                        .build()
        );

        loteRepository.flush();
        log.info("Lote {} salvo com sucesso", lote.getId());

        Path arquivoSalvo = salvarArquivoPermanentemente(file, lote.getId());
        log.info("Arquivo salvo em: {}", arquivoSalvo);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Transação commitada, iniciando processamento assíncrono do lote {}", lote.getId());

                eventProducer.publicarLoteIniciado(new LoteIniciadoEvent(
                        lote.getId(),
                        lote.getNomeArquivo(),
                        totalLinhas,
                        LocalDateTime.now()
                ));

                csvProcessorService.processar(lote.getId(), arquivoSalvo);
            }
        });

        return new LoteResponseDTO(
                lote.getId(),
                lote.getNomeArquivo(),
                lote.getStatus(),
                lote.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoteStatusResponseDTO getStatus(UUID id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Lote não encontrado: " + id));

        List<LoteProcessamentoDTO> chunks = loteProcessamentoService.findByLoteId(id);

        return new LoteStatusResponseDTO(
                lote.getId(),
                lote.getNomeArquivo(),
                lote.getStatus(),
                lote.getStatus().getDescricao(),
                lote.getTotalLinhas(),
                lote.getLinhasProcessadas(),
                lote.getLinhasSucesso(),
                lote.getLinhasErro(),
                lote.calcularProgresso(),
                lote.getTempoTotalMs(),
                lote.getIniciadoEm(),
                lote.getFinalizadoEm(),
                lote.getCreatedAt(),
                chunks
        );
    }

    private Path salvarArquivoPermanentemente(MultipartFile file, UUID loteId) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String nomeArquivo = loteId.toString() + "_" + file.getOriginalFilename();
            Path arquivoPath = uploadPath.resolve(nomeArquivo);
            Files.copy(file.getInputStream(), arquivoPath);
            return arquivoPath;
        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar arquivo permanentemente: " + e.getMessage(), e);
        }
    }
}
