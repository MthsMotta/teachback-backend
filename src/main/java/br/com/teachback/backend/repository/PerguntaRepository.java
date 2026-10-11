package br.com.teachback.backend.repository;

import br.com.teachback.backend.model.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {

    List<Pergunta> findByAtivaTrueOrderByOrdemAsc();
}
