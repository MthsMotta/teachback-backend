package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.TokenConfirmacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenConfirmacaoRepository extends JpaRepository<TokenConfirmacao,Long> {
}
