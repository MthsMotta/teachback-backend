package br.com.teachback.backend.repositories;

import br.com.teachback.backend.model.Faculdade;
import br.com.teachback.backend.model.Role;
import br.com.teachback.backend.model.StatusUsuario;
import br.com.teachback.backend.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByRole(Role role);
    List<Usuario> findByFaculdadeAndRole(Faculdade faculdade, Role role);
    Page<Usuario> findByFaculdadeAndRole(Faculdade faculdade, Role role, Pageable pageable);
    Page<Usuario> findByFaculdadeAndRoleAndStatus(Faculdade faculdade, Role role, StatusUsuario status, Pageable pageable);
}
