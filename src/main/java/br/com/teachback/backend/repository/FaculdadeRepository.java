package br.com.teachback.backend.repository;

import br.com.teachback.backend.model.Faculdade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FaculdadeRepository extends JpaRepository<Faculdade, Long> {
    Optional<Faculdade> findByNomeAndSigla(String nome, String sigla);
    List<Faculdade> findByNomeContainingIgnoreCaseOrSiglaContainingIgnoreCase(String nome, String sigla);
}
