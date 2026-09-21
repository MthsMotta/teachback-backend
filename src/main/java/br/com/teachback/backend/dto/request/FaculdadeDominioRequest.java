package br.com.teachback.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FaculdadeDominioRequest(@NotBlank @Size(max=100) String dominio) {
}
