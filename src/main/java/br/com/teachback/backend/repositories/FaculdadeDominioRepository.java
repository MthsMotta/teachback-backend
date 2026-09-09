package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Faculdade;
import br.com.teachback.backend.model.FaculdadeDominio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FaculdadeDominioRepository extends JpaRepository<FaculdadeDominio,Long> {
    FaculdadeDominio findByDominio(String dominio);
    List<FaculdadeDominio> findByFaculdade(Faculdade faculdade);
    Optional<FaculdadeDominio> findByDominioAndFaculdade(String dominio, Faculdade faculdade);
}
