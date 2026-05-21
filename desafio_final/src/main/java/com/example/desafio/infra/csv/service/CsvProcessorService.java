package com.example.desafio.infra.csv.service;

import java.nio.file.Path;
import java.util.UUID;

public interface CsvProcessorService {
    void processar(UUID loteId, Path filePath);
}
