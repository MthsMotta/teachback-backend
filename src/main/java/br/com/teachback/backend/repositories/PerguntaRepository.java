package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {
}
