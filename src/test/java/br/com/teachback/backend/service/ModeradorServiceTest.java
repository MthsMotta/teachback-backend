package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.ModeradorRequest;
import br.com.teachback.backend.dto.response.ModeradorResponse;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repositories.FaculdadeDominioRepository;
import br.com.teachback.backend.repositories.UsuarioRepository;
import br.com.teachback.backend.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModeradorServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FaculdadeDominioRepository faculdadeDominioRepository;

    @InjectMocks
    private ModeradorService moderadorService;

    private MockedStatic<SecurityContextHolder> mockLoggedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class);
        mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        return mockedStatic;
    }

    @Test
    @DisplayName("Cadastro de moderador com sucesso")
    void cadastrarComSucessoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        ModeradorRequest request = new ModeradorRequest("Moderador Teste", "moderador@teste.edu.br", "senha12345");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            when(faculdadeDominioRepository.findByDominioAndFaculdade("teste.edu.br", chefe.getFaculdade()))
                    .thenReturn(Optional.of(TestDataFactory.criarFaculdadeDominio(chefe.getFaculdade())));
            when(passwordEncoder.encode(anyString())).thenReturn("senhaHasheadaFake");

            moderadorService.cadastrar(request);

            verify(usuarioRepository).save(any(Usuario.class));
        }
    }

    @Test
    @DisplayName("Cadastro rejeitado: e-mail já cadastrado")
    void cadastrarEmailDuplicadoTest() {
        Usuario existente = TestDataFactory.criarUsuarioAluno();
        ModeradorRequest request = new ModeradorRequest("Moderador Teste", existente.getEmail(), "senha12345");

        when(usuarioRepository.findByEmail(request.email())).thenReturn(Optional.of(existente));

        assertThrows(RegraDeNegocioException.class, () -> moderadorService.cadastrar(request));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Cadastro rejeitado: domínio não pertence à faculdade do chefe")
    void cadastrarDominioNaoEncontradoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        ModeradorRequest request = new ModeradorRequest("Moderador Teste", "moderador@outrodominio.com", "senha12345");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            when(faculdadeDominioRepository.findByDominioAndFaculdade("outrodominio.com", chefe.getFaculdade()))
                    .thenReturn(Optional.empty());

            assertThrows(RegraDeNegocioException.class, () -> moderadorService.cadastrar(request));
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    @Test
    @DisplayName("Listagem retorna moderadores da faculdade do chefe logado")
    void listarTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario moderador1 = TestDataFactory.criarUsuario(Role.MODERADOR, StatusUsuario.ATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findByFaculdadeAndRole(chefe.getFaculdade(), Role.MODERADOR))
                    .thenReturn(List.of(moderador1));

            List<ModeradorResponse> response = moderadorService.listar();

            assertThat(response).hasSize(1);
        }
    }

    @Test
    @DisplayName("Ativar moderador com sucesso")
    void ativarModeradorComSucessoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario moderador = TestDataFactory.criarUsuario(Role.MODERADOR, StatusUsuario.INATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findById(moderador.getId())).thenReturn(Optional.of(moderador));

            moderadorService.ativarModerador(moderador.getId());

            assertThat(moderador.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        }
    }

    @Test
    @DisplayName("Ativar moderador rejeitado: faculdade diferente")
    void ativarModeradorFaculdadeDiferenteTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario moderador = TestDataFactory.criarUsuario(Role.MODERADOR, StatusUsuario.INATIVO);
        Faculdade outraFaculdade = TestDataFactory.criarFaculdade();
        outraFaculdade.setId(999L);
        moderador.setFaculdade(outraFaculdade);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findById(moderador.getId())).thenReturn(Optional.of(moderador));

            assertThrows(RegraDeNegocioException.class, () -> moderadorService.ativarModerador(moderador.getId()));
        }
    }

    @Test
    @DisplayName("Desativar moderador com sucesso")
    void desativarModeradorComSucessoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario moderador = TestDataFactory.criarUsuario(Role.MODERADOR, StatusUsuario.ATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findById(moderador.getId())).thenReturn(Optional.of(moderador));

            moderadorService.desativarModerador(moderador.getId());

            assertThat(moderador.getStatus()).isEqualTo(StatusUsuario.INATIVO);
        }
    }

    @Test
    @DisplayName("Aprovar professor com sucesso")
    void aprovarProfessorComSucessoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.PENDENTE_APROVACAO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));

            moderadorService.aprovarProfessor(professor.getId());

            assertThat(professor.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        }
    }

    @Test
    @DisplayName("Aprovar professor rejeitado: não está pendente de aprovação")
    void aprovarProfessorStatusInvalidoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));

            assertThrows(RegraDeNegocioException.class, () -> moderadorService.aprovarProfessor(professor.getId()));
        }
    }

    @Test
    @DisplayName("Rejeitar professor com sucesso")
    void rejeitarProfessorComSucessoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.PENDENTE_APROVACAO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));

            moderadorService.rejeitarProfessor(professor.getId());

            assertThat(professor.getStatus()).isEqualTo(StatusUsuario.INATIVO);
        }
    }


    @Test
    @DisplayName("Ativar professor (já inativo) com sucesso")
    void ativarProfessorComSucessoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.INATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));

            moderadorService.ativarProfessor(professor.getId());

            assertThat(professor.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        }
    }

    @Test
    @DisplayName("Ativar professor rejeitado: ainda pendente de aprovação")
    void ativarProfessorAindaPendenteTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.PENDENTE_APROVACAO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));

            assertThrows(RegraDeNegocioException.class, () -> moderadorService.ativarProfessor(professor.getId()));
        }
    }

    @Test
    @DisplayName("Desativar professor com sucesso")
    void desativarProfessorComSucessoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));

            moderadorService.desativarProfessor(professor.getId());

            assertThat(professor.getStatus()).isEqualTo(StatusUsuario.INATIVO);
        }
    }
}