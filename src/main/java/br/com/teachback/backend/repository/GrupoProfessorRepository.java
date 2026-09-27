package br.com.teachback.backend.repository;

import br.com.teachback.backend.model.Grupo;
import br.com.teachback.backend.model.GrupoProfessor;
import br.com.teachback.backend.model.Usuario;
import br.com.teachback.backend.model.StatusGrupo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoProfessorRepository extends JpaRepository<GrupoProfessor, Long> {

    boolean existsByGrupoAndProfessor(Grupo grupo, Usuario professor);

    Optional<GrupoProfessor> findByGrupo_IdAndProfessor(Long grupoId, Usuario professor);

    Page<GrupoProfessor> findByProfessorAndGrupo_Status(Usuario professor, StatusGrupo status, Pageable pageable);
}
