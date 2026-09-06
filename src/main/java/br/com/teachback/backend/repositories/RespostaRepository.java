package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespostaRepository extends JpaRepository<Resposta, Long> {
}
