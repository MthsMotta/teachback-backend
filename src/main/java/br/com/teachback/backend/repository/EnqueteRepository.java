package br.com.teachback.backend.repository;

import br.com.teachback.backend.model.Enquete;
import br.com.teachback.backend.model.Grupo;
import br.com.teachback.backend.model.StatusEnquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnqueteRepository extends JpaRepository<Enquete, Long> {
    Optional<Enquete> findByGrupoAndStatus(Grupo grupo, StatusEnquete status);

    @Modifying
    @Query("UPDATE Enquete e SET e.status = br.com.teachback.backend.model.StatusEnquete.ENCERRADA " +
            "WHERE e.dataFim <= :dataLimite AND e.status = br.com.teachback.backend.model.StatusEnquete.ATIVA")
    int encerrarEnquetes(@Param("dataLimite") LocalDate dataLimite);

    boolean existsByGrupoAndStatus(Grupo grupo, StatusEnquete status);
    List<Enquete> findByGrupoOrderByDataInicioAsc(Grupo grupo);

}
