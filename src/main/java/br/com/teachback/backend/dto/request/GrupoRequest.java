package br.com.teachback.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GrupoRequest(@NotBlank @Size(max = 255) String nome) {
}
