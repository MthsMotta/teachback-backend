package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Faculdade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FaculdadeRepository extends JpaRepository<Faculdade, Long> {
    Optional<Faculdade> findByNome(String nome);
}
