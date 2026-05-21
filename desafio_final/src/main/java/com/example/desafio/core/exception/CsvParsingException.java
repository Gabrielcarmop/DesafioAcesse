package com.example.desafio.core.exception;

public class CsvParsingException extends RuntimeException {

    public CsvParsingException(String mensagem) {
        super(mensagem);
    }

    public CsvParsingException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
