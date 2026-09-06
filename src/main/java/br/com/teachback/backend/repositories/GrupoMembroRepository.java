package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Grupo;
import br.com.teachback.backend.model.GrupoMembro;
import br.com.teachback.backend.model.StatusMembro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoMembroRepository extends JpaRepository<GrupoMembro,Long> {
    List<GrupoMembro> findByGrupoAndStatus(Grupo grupo, StatusMembro status);
}
