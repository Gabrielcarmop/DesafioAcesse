package com.example.desafio.infra.csv;

import com.example.desafio.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Component
public class CsvValidator {

    private static final List<String> COLUNAS_OBRIGATORIAS = List.of(
            "nome", "email", "telefone", "origem", "data_cadastro"
    );

    private static final String DELIMITADORES_REGEX = "[,;]";

    @Value("${app.upload.tamanho-maximo-bytes:104857600}")
    private long tamanhoMaximoBytes;

    public void validate(MultipartFile file) {
        validarNaoVazio(file);
        validarExtensao(file);

        byte[] bytes = lerBytes(file);

        validarEncoding(bytes);
        validarCabecalho(bytes);
    }


    private void validarNaoVazio(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo vazio. Por favor, envie um arquivo CSV válido.");
        }
        if (file.getSize() > tamanhoMaximoBytes) {
            throw new BusinessException(
                    String.format("Arquivo excede o tamanho máximo permitido de %d MB.",
                            tamanhoMaximoBytes / 1024 / 1024)
            );
        }
    }

    private void validarExtensao(MultipartFile file) {
        String nomeArquivo = file.getOriginalFilename();

        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            throw new BusinessException("Arquivo sem extensão. Envie um arquivo .csv");
        }

        String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase();

        if (!"csv".equals(extensao)) {
            throw new BusinessException(
                    "Extensão inválida: '" + extensao + "'. Apenas arquivos .csv são permitidos."
            );
        }
    }

    private void validarEncoding(byte[] bytes) {
        CharsetDecoder decoderEstrito = StandardCharsets.UTF_8.newDecoder();
        decoderEstrito.onMalformedInput(java.nio.charset.CodingErrorAction.REPORT);
        decoderEstrito.onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);

        try {
            decoderEstrito.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException ex) {
            throw new BusinessException(
                    "O arquivo não está codificado em UTF-8. Por favor, salve o CSV com encoding UTF-8."
            );
        }
    }

    private void validarCabecalho(byte[] bytes) {
        String conteudo = new String(bytes, StandardCharsets.UTF_8);

        String primeiraLinha;
        try (BufferedReader reader = new BufferedReader(new StringReader(conteudo))) {
            primeiraLinha = reader.readLine();
        } catch (IOException ex) {
            throw new BusinessException("Erro inesperado ao ler cabeçalho do CSV.");
        }

        if (primeiraLinha == null || primeiraLinha.isBlank()) {
            throw new BusinessException("Arquivo CSV vazio ou sem cabeçalho.");
        }

        List<String> colunasEncontradas = Arrays.stream(primeiraLinha.split(DELIMITADORES_REGEX))
                .map(String::trim)
                .map(campo -> campo.replace("\"", "")) // remove aspas do Excel
                .map(String::toLowerCase)
                .toList();

        List<String> colunasAusentes = COLUNAS_OBRIGATORIAS.stream()
                .filter(obrigatoria -> !colunasEncontradas.contains(obrigatoria))
                .toList();

        if (!colunasAusentes.isEmpty()) {
            throw new BusinessException(
                    String.format("Coluna(s) obrigatória(s) ausente(s): %s. Colunas encontradas: %s",
                            colunasAusentes, colunasEncontradas)
            );
        }

        log.info("Cabeçalho CSV validado. Colunas encontradas: {}", colunasEncontradas);
    }

    private byte[] lerBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível ler o arquivo enviado: " + ex.getMessage());
        }
    }
}