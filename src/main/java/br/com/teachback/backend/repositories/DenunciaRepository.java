package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenunciaRepository extends JpaRepository<Denuncia,Long> {
}
