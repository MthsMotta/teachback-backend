package br.com.teachback.backend.mapper;

import br.com.teachback.backend.dto.request.FaculdadeRequest;
import br.com.teachback.backend.dto.response.FaculdadeResponse;
import br.com.teachback.backend.model.Faculdade;

public final class FaculdadeMapper {

    private FaculdadeMapper() {}

    public static FaculdadeResponse toDTO(Faculdade faculdade) {
        return new FaculdadeResponse(faculdade.getId(),
                faculdade.getNome(),
                faculdade.getSigla(),
                faculdade.getExemploTurma());
    }

    public static Faculdade toEntity(FaculdadeRequest faculdadeRequest) {
        Faculdade faculdade = new Faculdade();
        faculdade.setNome(faculdadeRequest.nome());
        faculdade.setSigla(faculdadeRequest.sigla());
        faculdade.setExemploTurma(faculdadeRequest.exemploTurma());
        return faculdade;
    }

    public static void updateEntityFromDTO(FaculdadeRequest faculdadeRequest, Faculdade faculdade) {
        faculdade.setNome(faculdadeRequest.nome());
        faculdade.setSigla(faculdadeRequest.sigla());
        faculdade.setExemploTurma(faculdadeRequest.exemploTurma());
    }
}
