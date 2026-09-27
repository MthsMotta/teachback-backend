package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.CodigoConviteRequest;
import br.com.teachback.backend.dto.request.GrupoRequest;
import br.com.teachback.backend.dto.response.GrupoProfessorResponse;
import br.com.teachback.backend.dto.response.GrupoResponse;
import br.com.teachback.backend.exception.AcessoNegadoException;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repository.EnqueteRepository;
import br.com.teachback.backend.repository.GrupoMembroRepository;
import br.com.teachback.backend.repository.GrupoProfessorRepository;
import br.com.teachback.backend.repository.GrupoRepository;
import br.com.teachback.backend.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private GrupoProfessorRepository grupoProfessorRepository;

    @Mock
    private GrupoMembroRepository grupoMembroRepository;

    @Mock
    private EnqueteRepository enqueteRepository;

    @InjectMocks
    private GrupoService grupoService;

    private MockedStatic<SecurityContextHolder> mockLoggedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class);
        mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        return mockedStatic;
    }

    // ---------- criar ----------

    @Test
    @DisplayName("Criar grupo com sucesso")
    void criarComSucessoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        GrupoRequest request = new GrupoRequest("Cálculo 1 — Noite");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoRepository.existsByCodigoConvite(anyString())).thenReturn(false);
            when(grupoRepository.existsByCodigoConviteProfessor(anyString())).thenReturn(false);
            when(grupoRepository.save(any(Grupo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(grupoProfessorRepository.save(any(GrupoProfessor.class))).thenAnswer(inv -> inv.getArgument(0));

            GrupoProfessorResponse response = grupoService.criar(request);

            assertThat(response.role()).isEqualTo(RoleGrupo.CRIADOR);
            assertThat(response.codigoConvite()).hasSize(8);
            assertThat(response.codigoConviteProfessor()).hasSize(8);
            verify(grupoRepository).save(any(Grupo.class));
            verify(grupoProfessorRepository).save(any(GrupoProfessor.class));
        }
    }

    @Test
    @DisplayName("Criar grupo: colisão de código de convite força nova tentativa")
    void criarColisaoDeCodigoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        GrupoRequest request = new GrupoRequest("Cálculo 1 — Noite");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoRepository.existsByCodigoConvite(anyString())).thenReturn(true, false);
            when(grupoRepository.existsByCodigoConviteProfessor(anyString())).thenReturn(false);
            when(grupoRepository.save(any(Grupo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(grupoProfessorRepository.save(any(GrupoProfessor.class))).thenAnswer(inv -> inv.getArgument(0));

            grupoService.criar(request);

            verify(grupoRepository, times(2)).existsByCodigoConvite(anyString());
        }
    }

    // ---------- atualizar ----------

    @Test
    @DisplayName("Atualizar grupo com sucesso: qualquer professor ativo pode, não só o criador")
    void atualizarComSucessoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ATIVO);
        GrupoProfessor grupoProfessor = TestDataFactory.criarGrupoProfessor(grupo, professor, RoleGrupo.CO_DOCENTE);
        GrupoRequest request = new GrupoRequest("Novo Nome do Grupo");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(grupo.getId(), professor))
                    .thenReturn(Optional.of(grupoProfessor));

            GrupoProfessorResponse response = grupoService.atualizar(grupo.getId(), request);

            assertThat(response.nome()).isEqualTo("Novo Nome do Grupo");
        }
    }

    @Test
    @DisplayName("Atualizar grupo rejeitado: professor não vinculado a esse grupo")
    void atualizarProfessorNaoVinculadoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        GrupoRequest request = new GrupoRequest("Novo Nome do Grupo");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(1L, professor))
                    .thenReturn(Optional.empty());

            assertThrows(RecursoNaoEncontradoException.class, () -> grupoService.atualizar(1L, request));
        }
    }

    @Test
    @DisplayName("Atualizar grupo rejeitado: grupo já está encerrado")
    void atualizarGrupoEncerradoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ENCERRADO);
        GrupoProfessor grupoProfessor = TestDataFactory.criarGrupoProfessor(grupo, professor, RoleGrupo.CRIADOR);
        GrupoRequest request = new GrupoRequest("Nome atualizado");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(grupo.getId(), professor))
                    .thenReturn(Optional.of(grupoProfessor));

            assertThrows(RegraDeNegocioException.class, () -> grupoService.atualizar(grupo.getId(), request));
            verify(grupoRepository, never()).save(any(Grupo.class));
        }
    }

    // ---------- entrarComoAluno ----------

    @Test
    @DisplayName("Entrar como aluno com sucesso")
    void entrarComoAlunoComSucessoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ATIVO);
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        CodigoConviteRequest request = new CodigoConviteRequest(grupo.getCodigoConvite());

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findByCodigoConviteAndStatus(grupo.getCodigoConvite(), StatusGrupo.ATIVO))
                    .thenReturn(Optional.of(grupo));
            when(grupoMembroRepository.existsByGrupoAndAluno(grupo, aluno)).thenReturn(false);

            grupoService.entrarComoAluno(request);

            verify(grupoMembroRepository).save(any(GrupoMembro.class));
        }
    }

    @Test
    @DisplayName("Entrar como aluno rejeitado: código de convite inválido")
    void entrarComoAlunoCodigoInvalidoTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        CodigoConviteRequest request = new CodigoConviteRequest("XXXXXXXX");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findByCodigoConviteAndStatus("XXXXXXXX", StatusGrupo.ATIVO))
                    .thenReturn(Optional.empty());

            assertThrows(RecursoNaoEncontradoException.class, () -> grupoService.entrarComoAluno(request));
            verify(grupoMembroRepository, never()).save(any(GrupoMembro.class));
        }
    }

    @Test
    @DisplayName("Entrar como aluno rejeitado: aluno já é membro do grupo")
    void entrarComoAlunoJaEhMembroTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(aluno.getFaculdade(), StatusGrupo.ATIVO);
        CodigoConviteRequest request = new CodigoConviteRequest(grupo.getCodigoConvite());

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findByCodigoConviteAndStatus(grupo.getCodigoConvite(), StatusGrupo.ATIVO))
                    .thenReturn(Optional.of(grupo));
            when(grupoMembroRepository.existsByGrupoAndAluno(grupo, aluno)).thenReturn(true);

            assertThrows(RegraDeNegocioException.class, () -> grupoService.entrarComoAluno(request));
            verify(grupoMembroRepository, never()).save(any(GrupoMembro.class));
        }
    }

    // ---------- entrarComoProfessor ----------

    @Test
    @DisplayName("Entrar como professor com sucesso: vira CO_DOCENTE")
    void entrarComoProfessorComSucessoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ATIVO);
        CodigoConviteRequest request = new CodigoConviteRequest(grupo.getCodigoConviteProfessor());

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoRepository.findByCodigoConviteProfessorAndStatus(grupo.getCodigoConviteProfessor(), StatusGrupo.ATIVO))
                    .thenReturn(Optional.of(grupo));
            when(grupoProfessorRepository.existsByGrupoAndProfessor(grupo, professor)).thenReturn(false);
            when(grupoProfessorRepository.save(any(GrupoProfessor.class))).thenAnswer(inv -> inv.getArgument(0));

            GrupoProfessorResponse response = grupoService.entrarComoProfessor(request);

            assertThat(response.role()).isEqualTo(RoleGrupo.CO_DOCENTE);
        }
    }

    @Test
    @DisplayName("Entrar como professor rejeitado: código de convite inválido")
    void entrarComoProfessorCodigoInvalidoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        CodigoConviteRequest request = new CodigoConviteRequest("XXXXXXXX");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoRepository.findByCodigoConviteProfessorAndStatus("XXXXXXXX", StatusGrupo.ATIVO))
                    .thenReturn(Optional.empty());

            assertThrows(RecursoNaoEncontradoException.class, () -> grupoService.entrarComoProfessor(request));
        }
    }

    @Test
    @DisplayName("Entrar como professor rejeitado: professor já faz parte do grupo")
    void entrarComoProfessorJaEhMembroTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Usuario professorCriador = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(professorCriador.getFaculdade(), StatusGrupo.ATIVO);
        CodigoConviteRequest request = new CodigoConviteRequest(grupo.getCodigoConviteProfessor());

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoRepository.findByCodigoConviteProfessorAndStatus(grupo.getCodigoConviteProfessor(), StatusGrupo.ATIVO))
                    .thenReturn(Optional.of(grupo));
            when(grupoProfessorRepository.existsByGrupoAndProfessor(grupo, professor)).thenReturn(true);

            assertThrows(RegraDeNegocioException.class, () -> grupoService.entrarComoProfessor(request));
            verify(grupoProfessorRepository, never()).save(any(GrupoProfessor.class));
        }
    }

    // ---------- encerrar ----------

    @Test
    @DisplayName("Encerrar grupo com sucesso: também encerra a enquete ativa")
    void encerrarComSucessoComEnqueteAtivaTest() {
        Usuario criador = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(criador.getFaculdade(), StatusGrupo.ATIVO);
        GrupoProfessor grupoProfessor = TestDataFactory.criarGrupoProfessor(grupo, criador, RoleGrupo.CRIADOR);
        Enquete enquete = TestDataFactory.criarEnquete(grupo);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(criador)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(grupo.getId(), criador))
                    .thenReturn(Optional.of(grupoProfessor));
            when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA))
                    .thenReturn(Optional.of(enquete));

            grupoService.encerrar(grupo.getId());

            assertThat(grupo.getStatus()).isEqualTo(StatusGrupo.ENCERRADO);
            assertThat(enquete.getStatus()).isEqualTo(StatusEnquete.ENCERRADA);
            verify(grupoRepository).save(grupo);
            verify(enqueteRepository).save(enquete);
        }
    }

    @Test
    @DisplayName("Encerrar grupo com sucesso: sem enquete ativa, não quebra")
    void encerrarComSucessoSemEnqueteAtivaTest() {
        Usuario criador = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(criador.getFaculdade(), StatusGrupo.ATIVO);
        GrupoProfessor grupoProfessor = TestDataFactory.criarGrupoProfessor(grupo, criador, RoleGrupo.CRIADOR);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(criador)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(grupo.getId(), criador))
                    .thenReturn(Optional.of(grupoProfessor));
            when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA))
                    .thenReturn(Optional.empty());

            grupoService.encerrar(grupo.getId());

            assertThat(grupo.getStatus()).isEqualTo(StatusGrupo.ENCERRADO);
            verify(grupoRepository).save(grupo);
            verify(enqueteRepository, never()).save(any(Enquete.class));
        }
    }

    @Test
    @DisplayName("Encerrar grupo rejeitado: CO_DOCENTE não pode encerrar, só o CRIADOR")
    void encerrarCoDocenteRejeitadoTest() {
        Usuario coDocente = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(coDocente.getFaculdade(), StatusGrupo.ATIVO);
        GrupoProfessor grupoProfessor = TestDataFactory.criarGrupoProfessor(grupo, coDocente, RoleGrupo.CO_DOCENTE);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(coDocente)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(grupo.getId(), coDocente))
                    .thenReturn(Optional.of(grupoProfessor));

            assertThrows(AcessoNegadoException.class, () -> grupoService.encerrar(grupo.getId()));
        }
    }

    @Test
    @DisplayName("Encerrar grupo rejeitado: professor não vinculado a esse grupo")
    void encerrarProfessorNaoVinculadoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(1L, professor))
                    .thenReturn(Optional.empty());

            assertThrows(RecursoNaoEncontradoException.class, () -> grupoService.encerrar(1L));
        }
    }

    // ---------- listagens ----------

    @Test
    @DisplayName("Listar grupos do professor: traz grupos como CRIADOR e como CO_DOCENTE juntos")
    void listarGruposDoProfessorTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupoCriado = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ATIVO);
        Grupo grupoCoDocente = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ATIVO);
        grupoCoDocente.setId(2L);

        GrupoProfessor comoCriador = TestDataFactory.criarGrupoProfessor(grupoCriado, professor, RoleGrupo.CRIADOR);
        GrupoProfessor comoCoDocente = TestDataFactory.criarGrupoProfessor(grupoCoDocente, professor, RoleGrupo.CO_DOCENTE);

        Pageable pageable = PageRequest.of(0, 20);
        Page<GrupoProfessor> page = new PageImpl<>(List.of(comoCriador, comoCoDocente), pageable, 2);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoProfessorRepository.findByProfessorAndGrupo_Status(professor, StatusGrupo.ATIVO, pageable))
                    .thenReturn(page);

            Page<GrupoProfessorResponse> response = grupoService.listarGruposDoProfessor(pageable);

            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent())
                    .extracting(GrupoProfessorResponse::role)
                    .containsExactlyInAnyOrder(RoleGrupo.CRIADOR, RoleGrupo.CO_DOCENTE);
        }
    }

    @Test
    @DisplayName("Listar grupos do aluno: não traz grupos onde o vínculo está REMOVIDO")
    void listarGruposDoAlunoTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);

        Grupo grupoUm = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ATIVO);
        Grupo grupoDois = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ATIVO);

        GrupoMembro membroPrimeiroGrupo = TestDataFactory.criarGrupoMembro(grupoUm, aluno, StatusMembro.ATIVO);
        GrupoMembro membroSegundoGrupo = TestDataFactory.criarGrupoMembro(grupoDois, aluno, StatusMembro.REMOVIDO);

        Pageable pageable = PageRequest.of(0, 20);
        Page<GrupoMembro> page = new PageImpl<>(List.of(membroPrimeiroGrupo), pageable, 1);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoMembroRepository.findByAlunoAndStatusAndGrupo_Status(aluno, StatusMembro.ATIVO, StatusGrupo.ATIVO, pageable))
                    .thenReturn(page);

            Page<GrupoResponse> response = grupoService.listarGruposDoAluno(pageable);

            assertThat(response.getContent()).hasSize(1);
        }
    }
}