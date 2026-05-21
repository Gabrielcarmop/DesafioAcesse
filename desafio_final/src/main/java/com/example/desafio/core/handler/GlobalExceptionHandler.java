package com.example.desafio.core.handler;

import com.example.desafio.core.exception.BusinessException;
import com.example.desafio.core.exception.CsvParsingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(
            BusinessException ex, HttpServletRequest req) {

        String msg = ex.getMessage();
        HttpStatus status = msg != null && msg.toLowerCase().contains("não encontrado")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.UNPROCESSABLE_ENTITY;

        return erro(status, msg, req.getRequestURI());
    }

    @ExceptionHandler(CsvParsingException.class)
    public ResponseEntity<Map<String, Object>> handleCsvParsing(
            CsvParsingException ex, HttpServletRequest req) {
        return erro(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileTooLarge(
            MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return erro(HttpStatus.PAYLOAD_TOO_LARGE,
                "Arquivo excede o tamanho máximo permitido (50MB)", req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest req) {
        log.error("Erro não tratado em {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente.", req.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String mensagem, String path) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "erro", status.getReasonPhrase(),
                "mensagem", mensagem != null ? mensagem : "Erro desconhecido",
                "path", path,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
