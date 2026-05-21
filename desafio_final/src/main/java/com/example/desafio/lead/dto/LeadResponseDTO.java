package com.example.desafio.lead.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class LeadResponseDTO {
    private UUID id;
    private String nome;
    private String email;
    private String telefone;
    private String origem;
    private String dataCadastro;
}
