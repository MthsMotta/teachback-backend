package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Faculdade;
import br.com.teachback.backend.model.FaculdadeDominio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaculdadeDominioRepository extends JpaRepository<FaculdadeDominio,Long> {
    FaculdadeDominio findByDominio(String dominio);
    List<FaculdadeDominio> findByFaculdade(Faculdade faculdade);
}
