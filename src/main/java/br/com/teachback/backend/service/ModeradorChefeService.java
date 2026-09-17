package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.ModeradorChefeRequest;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repositories.FaculdadeDominioRepository;
import br.com.teachback.backend.repositories.FaculdadeRepository;
import br.com.teachback.backend.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ModeradorChefeService {

    private final UsuarioRepository usuarioRepository;
    private final FaculdadeRepository faculdadeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FaculdadeDominioRepository faculdadeDominioRepository;

    public ModeradorChefeService(UsuarioRepository usuarioRepository, FaculdadeRepository faculdadeRepository, PasswordEncoder passwordEncoder, FaculdadeDominioRepository faculdadeDominioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.faculdadeRepository = faculdadeRepository;
        this.passwordEncoder = passwordEncoder;
        this.faculdadeDominioRepository = faculdadeDominioRepository;
    }

    @Transactional
    public void cadastrar(ModeradorChefeRequest request) {
        if(usuarioRepository.findByEmail(request.email()).isPresent()){
            throw new RegraDeNegocioException("Dados inválidos");
        }
        Faculdade faculdade = faculdadeRepository.findById(request.faculdadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Faculdade não encontrada"));
        String dominioCadastro = request.email().substring(request.email().indexOf('@') + 1);
        Optional<FaculdadeDominio> dominio = faculdadeDominioRepository.findByDominioAndFaculdade(dominioCadastro, faculdade);
        if(dominio.isEmpty()){
            throw new RegraDeNegocioException("Dominio não encontrado");
        }

        Usuario usuario = new  Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setRole(Role.MODERADOR_CHEFE);
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.setFaculdade(faculdade);
        usuarioRepository.save(usuario);
    }
}
