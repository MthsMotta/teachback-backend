package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.CadastroRequest;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repositories.FaculdadeDominioRepository;
import br.com.teachback.backend.repositories.FaculdadeRepository;
import br.com.teachback.backend.repositories.TokenConfirmacaoRepository;
import br.com.teachback.backend.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final FaculdadeRepository faculdadeRepository;
    private final AuthorizationService authorizationService;
    private final PasswordEncoder passwordEncoder;
    private final FaculdadeDominioRepository faculdadeDominioRepository;
    private final TokenConfirmacaoRepository tokenConfirmacaoRepository;
    private final EmailService emailService;

    public AuthService(UsuarioRepository usuarioRepository, FaculdadeRepository faculdadeRepository, AuthorizationService authorizationService, PasswordEncoder passwordEncoder, FaculdadeDominioRepository faculdadeDominioRepository, TokenConfirmacaoRepository tokenConfirmacaoRepository, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.faculdadeRepository = faculdadeRepository;
        this.authorizationService = authorizationService;
        this.passwordEncoder = passwordEncoder;
        this.faculdadeDominioRepository = faculdadeDominioRepository;
        this.tokenConfirmacaoRepository = tokenConfirmacaoRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void cadastrar(CadastroRequest cadastro){
        if(usuarioRepository.findByEmail(cadastro.email()).isPresent()){
            throw new RegraDeNegocioException("Dados inválidos");
        }
        Faculdade faculdade = faculdadeRepository.findById(cadastro.faculdadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Faculdade não encontrada"));

        String dominioCadastro = cadastro.email().substring(cadastro.email().indexOf("@")+1);
        Optional<FaculdadeDominio> dominio = faculdadeDominioRepository.findByDominioAndFaculdade(dominioCadastro, faculdade);
        if(dominio.isEmpty()){
            throw new RegraDeNegocioException("Dominio não encontrado");
        }

        Role role = Role.valueOf(cadastro.tipoUsuario().name());
        Usuario usuario = new Usuario();
        usuario.setNome(cadastro.nome());
        usuario.setEmail(cadastro.email());
        usuario.setSenha(passwordEncoder.encode(cadastro.senha()));
        usuario.setRole(role);
        usuario.setStatus(StatusUsuario.PENDENTE_CONFIRMACAO);
        usuario.setFaculdade(faculdade);
        usuarioRepository.save(usuario);

        TokenConfirmacao tokenConfirmacao = new TokenConfirmacao();
        tokenConfirmacao.setToken(UUID.randomUUID().toString());
        tokenConfirmacao.setExpiraEm(LocalDateTime.now().plusHours(24));
        tokenConfirmacao.setTipoToken(TipoToken.CONFIRMACAO_EMAIL);
        tokenConfirmacao.setUsuario(usuario);
        tokenConfirmacaoRepository.save(tokenConfirmacao);

        emailService.enviarEmailToken(usuario.getEmail(),
                "Novo usuários cadastrado",
                 "Você está recebendo um email de cadastro");
    }
}
