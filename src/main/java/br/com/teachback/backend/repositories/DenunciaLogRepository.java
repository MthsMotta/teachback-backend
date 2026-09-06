package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.DenunciaLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenunciaLogRepository extends JpaRepository<DenunciaLog, Long> {
}
