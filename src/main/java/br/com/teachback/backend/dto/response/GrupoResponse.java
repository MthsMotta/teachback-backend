package br.com.teachback.backend.dto.response;

import br.com.teachback.backend.model.StatusGrupo;

public record GrupoResponse(Long id,
                            String nome,
                            StatusGrupo status) {
}