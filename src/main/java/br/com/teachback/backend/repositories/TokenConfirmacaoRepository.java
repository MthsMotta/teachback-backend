package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.TokenConfirmacao;
import br.com.teachback.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenConfirmacaoRepository extends JpaRepository<TokenConfirmacao,Long> {
    Optional<TokenConfirmacao> findByToken(String token);
    Optional<TokenConfirmacao> findByUsuario(Usuario usuario);
}
