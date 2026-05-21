package com.example.desafio.lead.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeadRequestDTO {
    @NotBlank
    private String nome;

    @NotBlank
    @Email
    private String email;

    private String telefone;

    @NotBlank
    private String origem;

    private LocalDateTime dataCadastro;
}
