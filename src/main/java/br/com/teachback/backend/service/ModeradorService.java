package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.ModeradorRequest;
import br.com.teachback.backend.dto.response.ModeradorResponse;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.mapper.ModeradorMapper;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repositories.FaculdadeDominioRepository;
import br.com.teachback.backend.repositories.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ModeradorService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final FaculdadeDominioRepository faculdadeDominioRepository;

    public ModeradorService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, FaculdadeDominioRepository faculdadeDominioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.faculdadeDominioRepository = faculdadeDominioRepository;
    }

    @Transactional
    public void cadastrar(ModeradorRequest request) {
        if(usuarioRepository.findByEmail(request.email()).isPresent()){
            throw new RegraDeNegocioException("Dados inválidos");
        }

        var usuarioLogado = (Usuario) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Faculdade faculdade = usuarioLogado.getFaculdade();
        String dominioCadastro = request.email().substring(request.email().indexOf('@') + 1);
        Optional<FaculdadeDominio> dominio = faculdadeDominioRepository.findByDominioAndFaculdade(dominioCadastro, faculdade);
        if(dominio.isEmpty()){
            throw new RegraDeNegocioException("Dominio não encontrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setRole(Role.MODERADOR);
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.setFaculdade(faculdade);
        usuarioRepository.save(usuario);
    }

    public List<ModeradorResponse> listar(){
        var usuarioLogado = (Usuario) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        List<Usuario> moderadores = usuarioRepository.findByFaculdadeAndRole(usuarioLogado.getFaculdade(), Role.MODERADOR);
        return moderadores.stream().map(ModeradorMapper::toDTO).toList();
    }
}
