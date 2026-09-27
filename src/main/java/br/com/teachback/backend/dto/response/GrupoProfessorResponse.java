package br.com.teachback.backend.dto.response;

import br.com.teachback.backend.model.RoleGrupo;
import br.com.teachback.backend.model.StatusGrupo;

public record GrupoProfessorResponse(Long id,
                                     String nome,
                                     String codigoConvite,
                                     String codigoConviteProfessor,
                                     StatusGrupo status,
                                     RoleGrupo role) {
}
