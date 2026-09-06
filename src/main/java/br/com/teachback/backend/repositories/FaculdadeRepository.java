package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Faculdade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaculdadeRepository extends JpaRepository<Faculdade, Long> {
}
