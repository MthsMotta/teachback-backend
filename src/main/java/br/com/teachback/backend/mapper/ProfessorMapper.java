package br.com.teachback.backend.mapper;

import br.com.teachback.backend.dto.response.ProfessorResponse;
import br.com.teachback.backend.model.Usuario;

public final class ProfessorMapper {

    private ProfessorMapper(){}

    public static ProfessorResponse toDTO(Usuario professor) {
        return new ProfessorResponse(professor.getId(),
                professor.getNome(),
                professor.getEmail(),
                professor.getStatus());
    }
}
