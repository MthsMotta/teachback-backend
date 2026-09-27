package br.com.teachback.backend.repository;

import br.com.teachback.backend.model.Enquete;
import br.com.teachback.backend.model.Grupo;
import br.com.teachback.backend.model.StatusEnquete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnqueteRepository extends JpaRepository<Enquete, Long> {

    Optional<Enquete> findByGrupoAndStatus(Grupo grupo, StatusEnquete status);
}
