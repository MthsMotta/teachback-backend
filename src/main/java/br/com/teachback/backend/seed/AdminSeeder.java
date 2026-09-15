package br.com.teachback.backend.seed;

import br.com.teachback.backend.model.Role;
import br.com.teachback.backend.model.StatusUsuario;
import br.com.teachback.backend.model.Usuario;
import br.com.teachback.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${admin.email}")
    private String email;

    @Value("${admin.password}")
    private String senha;


    @Override
    public void run(String... args) throws Exception {

        if(!usuarioRepository.existsByRole(Role.ADMIN)){
            Usuario usuario = new Usuario();
            usuario.setNome("Admin");
            usuario.setEmail(email);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setRole(Role.ADMIN);
            usuario.setStatus(StatusUsuario.ATIVO);
            usuario.setFaculdade(null);
            usuarioRepository.save(usuario);
        }
    }
}
