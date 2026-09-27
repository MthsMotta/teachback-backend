package br.com.teachback.backend.repository;

import br.com.teachback.backend.model.Grupo;
import br.com.teachback.backend.model.StatusGrupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    boolean existsByCodigoConvite(String codigoConvite);

    boolean existsByCodigoConviteProfessor(String codigoConviteProfessor);

    Optional<Grupo> findByCodigoConviteAndStatus(String codigoConvite, StatusGrupo status);

    Optional<Grupo> findByCodigoConviteProfessorAndStatus(String codigoConviteProfessor, StatusGrupo status);
}