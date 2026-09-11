package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.TokenConfirmacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenConfirmacaoRepository extends JpaRepository<TokenConfirmacao,Long> {
    Optional<TokenConfirmacao> findByToken(String token);
}
