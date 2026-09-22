package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.ModeradorRequest;
import br.com.teachback.backend.dto.response.ModeradorResponse;
import br.com.teachback.backend.dto.response.ProfessorResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.mapper.ModeradorMapper;
import br.com.teachback.backend.mapper.ProfessorMapper;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repositories.FaculdadeDominioRepository;
import br.com.teachback.backend.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<ProfessorResponse> listarProfessores(Pageable pageable){
        var usuarioLogado = (Usuario) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Page<Usuario> professores = usuarioRepository.findByFaculdadeAndRole(usuarioLogado.getFaculdade(), Role.PROFESSOR, pageable);
        return professores.map(ProfessorMapper::toDTO);
    }

    public Page<ProfessorResponse> listarProfessoresPendentes(Pageable pageable){
        var usuarioLogado = (Usuario) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Page<Usuario> professores = usuarioRepository.findByFaculdadeAndRoleAndStatus(usuarioLogado.getFaculdade(), Role.PROFESSOR, StatusUsuario.PENDENTE_APROVACAO, pageable);
        return professores.map(ProfessorMapper::toDTO);
    }

    private Usuario buscarModeradorDaFaculdade(Long id){
        var usuarioLogado = (Usuario) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Usuario moderador = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Moderador não encontrado"));
        if(!moderador.getFaculdade().getId().equals(usuarioLogado.getFaculdade().getId())){
            throw new RegraDeNegocioException("Você só pode gerenciar moderadores da sua própria faculdade");
        }
        if(moderador.getRole() != Role.MODERADOR){
            throw new RegraDeNegocioException("Este usuário não é um moderador");
        }
        return moderador;
    }

    @Transactional
    public void desativarModerador(Long id){
        buscarModeradorDaFaculdade(id).setStatus(StatusUsuario.INATIVO);
    }

    @Transactional
    public void ativarModerador(Long id){
        buscarModeradorDaFaculdade(id).setStatus(StatusUsuario.ATIVO);
    }

    private Usuario buscarProfessorDaFaculdade(Long id){
        var usuarioLogado = (Usuario) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Usuario professor = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado"));
        if(!usuarioLogado.getFaculdade().getId().equals(professor.getFaculdade().getId())){
            throw new RegraDeNegocioException("Você só pode agir sobre professores da sua própria faculdade");
        }
        if(professor.getRole() != Role.PROFESSOR){
            throw new RegraDeNegocioException("Você só pode agir sobre contas de professor");
        }
        if(professor.getStatus() != StatusUsuario.PENDENTE_APROVACAO){
            throw new RegraDeNegocioException("Você só pode agir sobre professores pendentes de aprovação");
        }
        return professor;
    }

    private Usuario buscarProfessorAtivoDaFaculdade(Long id){
        var usuarioLogado = (Usuario) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Usuario professor = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado"));
        if(!usuarioLogado.getFaculdade().getId().equals(professor.getFaculdade().getId())){
            throw new RegraDeNegocioException("Você só pode agir sobre professores da sua própria faculdade");
        }
        if(professor.getRole() != Role.PROFESSOR){
            throw new RegraDeNegocioException("Você só pode agir sobre contas de professor");
        }
        if(professor.getStatus() != StatusUsuario.ATIVO && professor.getStatus() != StatusUsuario.INATIVO){
            throw new RegraDeNegocioException("Você só pode agir sobre professores ativos ou inativos");
        }
        return professor;
    }

    @Transactional
    public void rejeitarProfessor(Long id){
        buscarProfessorDaFaculdade(id).setStatus(StatusUsuario.INATIVO);
    }

    @Transactional
    public void aprovarProfessor(Long id){
        buscarProfessorDaFaculdade(id).setStatus(StatusUsuario.ATIVO);
    }

    @Transactional
    public void desativarProfessor(Long id){
        buscarProfessorAtivoDaFaculdade(id).setStatus(StatusUsuario.INATIVO);
    }

    @Transactional
    public void ativarProfessor(Long id){
        buscarProfessorAtivoDaFaculdade(id).setStatus(StatusUsuario.ATIVO);
    }


}
