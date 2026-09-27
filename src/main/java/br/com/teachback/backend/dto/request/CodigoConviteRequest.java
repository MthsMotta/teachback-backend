package br.com.teachback.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodigoConviteRequest(@NotBlank @Size(min = 8, max = 8) String codigo) {
}
