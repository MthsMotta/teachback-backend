package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Grupo;
import br.com.teachback.backend.model.GrupoProfessor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoProfessorRepository extends JpaRepository<GrupoProfessor,Long> {
    List<GrupoProfessor> findByGrupo(Grupo grupo);
}
