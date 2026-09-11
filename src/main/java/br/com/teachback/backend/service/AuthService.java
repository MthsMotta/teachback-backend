package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.CadastroRequest;
import br.com.teachback.backend.dto.request.LoginRequest;
import br.com.teachback.backend.dto.response.LoginResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.exception.TokenExpiradoException;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repositories.FaculdadeDominioRepository;
import br.com.teachback.backend.repositories.FaculdadeRepository;
import br.com.teachback.backend.repositories.TokenConfirmacaoRepository;
import br.com.teachback.backend.repositories.UsuarioRepository;
import br.com.teachback.backend.security.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final PasswordEncoder passwordEncoder;
    private final FaculdadeDominioRepository faculdadeDominioRepository;
    private final TokenConfirmacaoRepository tokenConfirmacaoRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository, FaculdadeRepository faculdadeRepository, PasswordEncoder passwordEncoder, FaculdadeDominioRepository faculdadeDominioRepository, TokenConfirmacaoRepository tokenConfirmacaoRepository, EmailService emailService, AuthenticationManager authenticationManager, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.faculdadeRepository = faculdadeRepository;
        this.passwordEncoder = passwordEncoder;
        this.faculdadeDominioRepository = faculdadeDominioRepository;
        this.tokenConfirmacaoRepository = tokenConfirmacaoRepository;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
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

        TokenConfirmacao token = new TokenConfirmacao();
        token.setToken(UUID.randomUUID().toString());
        token.setTipoToken(TipoToken.CONFIRMACAO_EMAIL);
        token.setExpiraEm(LocalDateTime.now().plusMinutes(15));
        token.setUsuario(usuario);
        tokenConfirmacaoRepository.save(token);

        emailService.enviarEmailToken(usuario.getEmail(),
                "Confirmação de cadastro - TeachBack",
                "Confirme seu cadastro clicando no link: http://localhost:8080/auth/confirmar?token=" + token.getToken());
    }

    public LoginResponse login(LoginRequest loginRequest){
        var usuarioSenha = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.senha());
        var auth = authenticationManager.authenticate(usuarioSenha);
        Usuario usuario =  (Usuario) auth.getPrincipal();
        var token = tokenService.generateToken(usuario);
        return new LoginResponse(token, usuario.getNome(), usuario.getRole());
    }

    @Transactional
    public void confirmarEmail(String token){
        TokenConfirmacao tokenConfirmacao = tokenConfirmacaoRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Token inválido"));
        if(LocalDateTime.now().isAfter(tokenConfirmacao.getExpiraEm())){
            throw new TokenExpiradoException("Token expirado");
        }
        Usuario usuario = tokenConfirmacao.getUsuario();
        if(usuario.getRole() == Role.ALUNO){
            usuario.setStatus(StatusUsuario.ATIVO);
        }
        else if(usuario.getRole() == Role.PROFESSOR){
            usuario.setStatus(StatusUsuario.PENDENTE_APROVACAO);
        }
        usuarioRepository.save(usuario);
        tokenConfirmacaoRepository.delete(tokenConfirmacao);
    }
}
