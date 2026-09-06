package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Enquete;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnqueteRepository extends JpaRepository<Enquete, Long> {
}
