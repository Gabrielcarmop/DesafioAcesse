package com.example.desafio.infra.csv;

import com.example.desafio.core.exception.CsvParsingException;
import com.example.desafio.lead.dto.LeadRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class CsvParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    );

    @Value("${app.processamento.chunk-size:1000}")
    private int chunkSize;


    public ParseResult processarEmChunks(Path filePath, Consumer<List<LeadRequestDTO>> chunkConsumer) {
        int totalLidas = 0;
        int totalValidas = 0;
        int totalIgnoradas = 0;

        try (BufferedReader reader = abrirReader(filePath.toFile())) {
            String cabecalho = reader.readLine();
            int[] indices = resolverIndicesColunas(cabecalho);

            List<LeadRequestDTO> chunkAtual = new ArrayList<>(chunkSize);
            String linha;
            int numeroLinha = 1;

            while ((linha = reader.readLine()) != null) {
                numeroLinha++;
                if (linha.isBlank()) continue;

                totalLidas++;

                ParsedLine resultado = parsearLinha(linha, indices, numeroLinha);
                if (resultado.valido()) {
                    chunkAtual.add(resultado.lead());
                    totalValidas++;
                } else {
                    totalIgnoradas++;
                    log.warn("Linha {} ignorada — {}", numeroLinha, resultado.motivo());
                }

                if (chunkAtual.size() >= chunkSize) {
                    chunkConsumer.accept(List.copyOf(chunkAtual));
                    chunkAtual.clear();
                }
            }

            if (!chunkAtual.isEmpty()) {
                chunkConsumer.accept(List.copyOf(chunkAtual));
            }

        } catch (CsvParsingException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new CsvParsingException("Erro ao ler o arquivo CSV: " + ex.getMessage(), ex);
        }

        log.info("CSV processado: {} lidas, {} válidas, {} ignoradas",
                totalLidas, totalValidas, totalIgnoradas);

        return new ParseResult(totalLidas, totalValidas, totalIgnoradas);
    }


    public int contarLinhasDados(MultipartFile file) {
        int count = 0;
        try (BufferedReader reader = abrirReader(file)) {
            reader.readLine();
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (!linha.isBlank()) count++;
            }
        } catch (IOException ex) {
            throw new CsvParsingException("Erro ao contar linhas do CSV: " + ex.getMessage(), ex);
        }
        return count;
    }

    private int[] resolverIndicesColunas(String cabecalho) {
        List<String> colunas = splitCsv(cabecalho).stream()
                .map(String::trim)
                .map(s -> s.replace("\"", ""))
                .map(String::toLowerCase)
                .toList();

        return new int[]{
                colunas.indexOf("nome"),
                colunas.indexOf("email"),
                colunas.indexOf("telefone"),
                colunas.indexOf("origem"),
                colunas.indexOf("data_cadastro")
        };
    }

    private ParsedLine parsearLinha(String linha, int[] indices, int numeroLinha) {
        List<String> campos = splitCsv(linha);

        int maiorIndice = 0;
        for (int i : indices) maiorIndice = Math.max(maiorIndice, i);

        if (campos.size() <= maiorIndice) {
            return ParsedLine.invalida("colunas insuficientes (" + campos.size() + " encontradas)");
        }

        String nome  = obterCampo(campos, indices[0]);
        String email = obterCampo(campos, indices[1]);

        if (nome == null || nome.isBlank()) {
            return ParsedLine.invalida("campo 'nome' vazio");
        }
        if (email == null || email.isBlank()) {
            return ParsedLine.invalida("campo 'email' vazio");
        }

        LeadRequestDTO lead = new LeadRequestDTO();
        lead.setNome(nome);
        lead.setEmail(email);
        lead.setTelefone(obterCampo(campos, indices[2]));
        lead.setOrigem(obterCampo(campos, indices[3]));
        lead.setDataCadastro(parsearDataHora(obterCampo(campos, indices[4]), numeroLinha));

        return ParsedLine.valida(lead);
    }


    private List<String> splitCsv(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean dentroDeAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);

            if (c == '"') {
                if (dentroDeAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    campo.append('"');
                    i++; // pula a segunda aspa escapada
                } else {
                    dentroDeAspas = !dentroDeAspas;
                }
            } else if ((c == ',' || c == ';') && !dentroDeAspas) {
                campos.add(campo.toString().trim());
                campo.setLength(0);
            } else {
                campo.append(c);
            }
        }

        campos.add(campo.toString().trim());
        return campos;
    }

    private String obterCampo(List<String> campos, int indice) {
        if (indice < 0 || indice >= campos.size()) return null;
        String valor = campos.get(indice);
        return (valor == null || valor.isBlank()) ? null : valor;
    }


    private LocalDateTime parsearDataHora(String valor, int numeroLinha) {
        if (valor == null) return null;

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(valor, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        log.warn("Linha {} — data_cadastro '{}' não reconhecida em nenhum formato, campo será nulo.",
                numeroLinha, valor);
        return null;
    }


    private BufferedReader abrirReader(File file) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    }

    private BufferedReader abrirReader(MultipartFile file) throws IOException {
        return new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
    }

    public record ParseResult(
            int totalLidas,
            int totalValidas,
            int totalIgnoradas
    ) {
        public int calcularTotalChunks(int chunkSize) {
            return (int) Math.ceil((double) totalValidas / chunkSize);
        }
    }

    private record ParsedLine(boolean valido, LeadRequestDTO lead, String motivo) {

        static ParsedLine valida(LeadRequestDTO lead) {
            return new ParsedLine(true, lead, null);
        }

        static ParsedLine invalida(String motivo) {
            return new ParsedLine(false, null, motivo);
        }
    }
}