package br.com.teachback.backend.dto.response;

import br.com.teachback.backend.model.StatusUsuario;

public record ModeradorResponse(Long id, String nome, String email, StatusUsuario status) {
}
