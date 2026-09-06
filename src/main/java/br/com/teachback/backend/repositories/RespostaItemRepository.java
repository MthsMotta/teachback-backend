package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.RespostaItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespostaItemRepository extends JpaRepository<RespostaItem, Long> {
}
