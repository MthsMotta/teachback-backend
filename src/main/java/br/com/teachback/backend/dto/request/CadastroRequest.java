package br.com.teachback.backend.dto.request;

import br.com.teachback.backend.model.RoleCadastro;
import jakarta.validation.constraints.*;

public record CadastroRequest(@NotBlank String nome,
                              @NotBlank @Email String email,
                              @NotBlank @Size(min = 8) String senha,
                              @NotNull Long faculdadeId,
                              @NotNull RoleCadastro tipoUsuario,
                              @AssertTrue Boolean aceiteTermos) {
}
