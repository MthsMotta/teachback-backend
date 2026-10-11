package br.com.teachback.backend.repository;

import br.com.teachback.backend.model.EnquetePergunta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnquetePerguntaRepository extends JpaRepository<EnquetePergunta, Long> {
}
