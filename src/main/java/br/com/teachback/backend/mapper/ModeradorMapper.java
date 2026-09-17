package br.com.teachback.backend.mapper;

import br.com.teachback.backend.dto.response.ModeradorResponse;
import br.com.teachback.backend.model.Usuario;

public final class ModeradorMapper {

    private ModeradorMapper(){}

    public static ModeradorResponse toDTO(Usuario moderador){
        return new ModeradorResponse(moderador.getId(),
                moderador.getNome(),
                moderador.getEmail(),
                moderador.getStatus());
    }
}
