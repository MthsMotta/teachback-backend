package br.com.teachback.backend.repository;

import br.com.teachback.backend.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoMembroRepository extends JpaRepository<GrupoMembro, Long> {

    boolean existsByGrupoAndAluno(Grupo grupo, Usuario aluno);
    Page<GrupoMembro> findByAlunoAndStatusAndGrupo_Status(Usuario aluno, StatusMembro statusMembro, StatusGrupo statusGrupo, Pageable pageable);
}
