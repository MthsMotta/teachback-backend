package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Enquete;
import br.com.teachback.backend.model.EnqueteParticipacao;
import br.com.teachback.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnqueteParticipacaoRepository extends JpaRepository<EnqueteParticipacao,Long> {
    boolean existsByEnqueteAndAluno(Enquete enquete, Usuario aluno);
}
