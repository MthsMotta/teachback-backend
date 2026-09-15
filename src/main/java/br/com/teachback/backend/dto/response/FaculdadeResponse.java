package br.com.teachback.backend.dto.response;

public record FaculdadeResponse(Long id,
                                String nome,
                                String sigla,
                                String exemploTurma) {
}
