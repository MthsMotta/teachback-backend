package br.com.teachback.backend.mapper;

import br.com.teachback.backend.dto.request.FaculdadeDominioRequest;
import br.com.teachback.backend.dto.response.FaculdadeDominioResponse;
import br.com.teachback.backend.model.Faculdade;
import br.com.teachback.backend.model.FaculdadeDominio;

public final class FaculdadeDominioMapper {

    private FaculdadeDominioMapper(){}

    public static FaculdadeDominioResponse toDto(FaculdadeDominio faculdadeDominio) {
        return new FaculdadeDominioResponse(faculdadeDominio.getId(),
                faculdadeDominio.getFaculdade().getId(),
                faculdadeDominio.getDominio());
    }

    public static FaculdadeDominio toEntity(FaculdadeDominioRequest request, Faculdade faculdade){
        FaculdadeDominio faculdadeDominio = new FaculdadeDominio();
        faculdadeDominio.setFaculdade(faculdade);
        faculdadeDominio.setDominio(request.dominio());
        return faculdadeDominio;
    }
}
