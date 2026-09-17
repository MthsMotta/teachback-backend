package br.com.teachback.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModeradorChefeRequest(@NotBlank String nome,
                                    @NotBlank @Email String email,
                                    @NotBlank @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres") String senha,
                                    @NotNull Long faculdadeId) {
}
