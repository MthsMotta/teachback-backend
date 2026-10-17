package br.com.teachback.backend.repository;

import br.com.teachback.backend.model.Enquete;
import br.com.teachback.backend.model.EnquetePergunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnquetePerguntaRepository extends JpaRepository<EnquetePergunta, Long> {
    List<EnquetePergunta> findByEnqueteOrderByPergunta_OrdemAsc(Enquete enquete);

}
