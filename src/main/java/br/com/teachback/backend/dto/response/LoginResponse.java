package br.com.teachback.backend.dto.response;

import br.com.teachback.backend.model.Role;

public record LoginResponse(String token, String nome, Role role) {
}
