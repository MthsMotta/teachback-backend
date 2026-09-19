package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.CadastroRequest;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.exception.TokenExpiradoException;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repositories.FaculdadeDominioRepository;
import br.com.teachback.backend.repositories.FaculdadeRepository;
import br.com.teachback.backend.repositories.TokenConfirmacaoRepository;
import br.com.teachback.backend.repositories.UsuarioRepository;
import br.com.teachback.backend.security.TokenService;
import br.com.teachback.backend.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FaculdadeRepository faculdadeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FaculdadeDominioRepository faculdadeDominioRepository;

    @Mock
    private TokenConfirmacaoRepository tokenConfirmacaoRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;


    @Test
    @DisplayName("Dominio encontrado")
    void checarDominioTest() {
        Faculdade faculdade = TestDataFactory.criarFaculdade();
        FaculdadeDominio faculdadeDominio = TestDataFactory.criarFaculdadeDominio(faculdade);
        CadastroRequest cadastro = TestDataFactory.criarCadastroRequest(RoleCadastro.ALUNO);

        when(usuarioRepository.findByEmail(cadastro.email())).thenReturn(Optional.empty());
        when(faculdadeRepository.findById(cadastro.faculdadeId())).thenReturn(Optional.of(faculdade));
        when(faculdadeDominioRepository.findByDominioAndFaculdade(eq(faculdadeDominio.getDominio()), eq(faculdade)))
                .thenReturn(Optional.of(faculdadeDominio));
        when(passwordEncoder.encode(anyString())).thenReturn("senhaHasheadaFake");

        authService.cadastrar(cadastro);

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Dominio não encontrado")
    void checarDominioTest2() {
        Faculdade faculdade = TestDataFactory.criarFaculdade();
        CadastroRequest cadastro = TestDataFactory.criarCadastroRequest(RoleCadastro.ALUNO);

        when(usuarioRepository.findByEmail(cadastro.email())).thenReturn(Optional.empty());
        when(faculdadeRepository.findById(cadastro.faculdadeId())).thenReturn(Optional.of(faculdade));
        when(faculdadeDominioRepository.findByDominioAndFaculdade(anyString(), any(Faculdade.class)))
                .thenReturn(Optional.empty());

        assertThrows(RegraDeNegocioException.class, () -> authService.cadastrar(cadastro));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Email já cadastrado")
    void emailJaCadastradoTest(){
        CadastroRequest cadastro = TestDataFactory.criarCadastroRequest(RoleCadastro.ALUNO);
        Usuario usuarioExistente = TestDataFactory.criarUsuarioAluno();

        when(usuarioRepository.findByEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(usuarioExistente));

        assertThrows(RegraDeNegocioException.class, () -> authService.cadastrar(cadastro));
        verify(faculdadeRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Faculdade não encontrada")
    void faculdadeNaoEncontrada(){
        CadastroRequest cadastro = TestDataFactory.criarCadastroRequest(RoleCadastro.ALUNO);

        when(usuarioRepository.findByEmail(cadastro.email())).thenReturn(Optional.empty());
        when(faculdadeRepository.findById(cadastro.faculdadeId())).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> authService.cadastrar(cadastro));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Cadastro de professor com sucesso")
    void cadastroDeProfessor(){
        Faculdade faculdade = TestDataFactory.criarFaculdade();
        FaculdadeDominio faculdadeDominio = TestDataFactory.criarFaculdadeDominio(faculdade);
        CadastroRequest cadastro = TestDataFactory.criarCadastroRequest(RoleCadastro.PROFESSOR);

        when(usuarioRepository.findByEmail(cadastro.email())).thenReturn(Optional.empty());
        when(faculdadeRepository.findById(cadastro.faculdadeId())).thenReturn(Optional.of(faculdade));
        when(faculdadeDominioRepository.findByDominioAndFaculdade(eq(faculdadeDominio.getDominio()), eq(faculdade)))
                .thenReturn(Optional.of(faculdadeDominio));
        when(passwordEncoder.encode(anyString())).thenReturn("senhaHasheadaFake");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        authService.cadastrar(cadastro);

        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioSalvo = captor.getValue();
        assertThat(usuarioSalvo.getRole()).isEqualTo(Role.PROFESSOR);
    }

    @Test
    void login() {
    }

    @Test
    @DisplayName("Confirmação de email com sucesso")
    void confirmarEmailTest() {
        Usuario aluno =  TestDataFactory.criarUsuarioAluno();
        TokenConfirmacao tokenConfirmacao = TestDataFactory.criarTokenValido(aluno);

        when(tokenConfirmacaoRepository.findByToken(tokenConfirmacao.getToken())).thenReturn(Optional.of(tokenConfirmacao));

        authService.confirmarEmail(tokenConfirmacao.getToken());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioAtualizado = captor.getValue();
        assertThat(usuarioAtualizado.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        verify(tokenConfirmacaoRepository).delete(tokenConfirmacao);
    }

    @Test
    @DisplayName("Confirmação de email com sucesso para Professor")
    void confirmarEmailTest2() {
        Usuario professor =  TestDataFactory.criarUsuarioProfessor();
        TokenConfirmacao tokenConfirmacao = TestDataFactory.criarTokenValido(professor);

        when(tokenConfirmacaoRepository.findByToken(tokenConfirmacao.getToken())).thenReturn(Optional.of(tokenConfirmacao));

        authService.confirmarEmail(tokenConfirmacao.getToken());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioAtualizado = captor.getValue();
        assertThat(usuarioAtualizado.getStatus()).isEqualTo(StatusUsuario.PENDENTE_APROVACAO);
        verify(tokenConfirmacaoRepository).delete(tokenConfirmacao);
    }

    @Test
    @DisplayName("Token não encontrado")
    void tokenNaoEncontradoTest(){
        when(tokenConfirmacaoRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> authService.confirmarEmail("token-inexistente"));
    }

    @Test
    @DisplayName("Token expirado")
    void tokenExpiradoTest(){
        Usuario aluno = TestDataFactory.criarUsuarioAluno();
        TokenConfirmacao tokenExpirado = TestDataFactory.criarTokenExpirado(aluno);

        when(tokenConfirmacaoRepository.findByToken(tokenExpirado.getToken())).thenReturn(Optional.of(tokenExpirado));

        assertThrows(TokenExpiradoException.class, () -> authService.confirmarEmail(tokenExpirado.getToken()));
        verify(tokenConfirmacaoRepository, never()).delete(any(TokenConfirmacao.class));
    }

    @Test
    void reenviarConfirmacao() {
    }
}