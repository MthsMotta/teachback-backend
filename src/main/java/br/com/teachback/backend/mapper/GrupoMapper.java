package br.com.teachback.backend.mapper;

import br.com.teachback.backend.dto.request.GrupoRequest;
import br.com.teachback.backend.dto.response.GrupoProfessorResponse;
import br.com.teachback.backend.dto.response.GrupoResponse;
import br.com.teachback.backend.model.Faculdade;
import br.com.teachback.backend.model.Grupo;
import br.com.teachback.backend.model.GrupoProfessor;
import br.com.teachback.backend.model.StatusGrupo;

public final class GrupoMapper {

    private GrupoMapper() {}

    public static GrupoResponse toDTO(Grupo grupo) {
        return new GrupoResponse(grupo.getId(),
                grupo.getNome(),
                grupo.getStatus());
    }

    public static GrupoProfessorResponse toDTO(GrupoProfessor grupoProfessor) {
        Grupo grupo = grupoProfessor.getGrupo();
        return new GrupoProfessorResponse(grupo.getId(),
                grupo.getNome(),
                grupo.getCodigoConvite(),
                grupo.getCodigoConviteProfessor(),
                grupo.getStatus(),
                grupoProfessor.getRole());
    }

    public static Grupo toEntity(GrupoRequest request, Faculdade faculdade) {
        Grupo grupo = new Grupo();
        grupo.setNome(request.nome());
        grupo.setFaculdade(faculdade);
        grupo.setStatus(StatusGrupo.ATIVO);
        return grupo;
    }

    public static void atualizar(Grupo grupo, GrupoRequest request) {
        grupo.setNome(request.nome());
    }
}