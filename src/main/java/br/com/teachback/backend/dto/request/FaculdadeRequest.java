package br.com.teachback.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FaculdadeRequest(@NotBlank String nome,
                               @NotBlank @Size(max = 20) String sigla,
                               @Size(max = 20) String exemploTurma) {
}
