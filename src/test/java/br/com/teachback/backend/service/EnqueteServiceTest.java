package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.response.EnqueteAtivaResponse;
import br.com.teachback.backend.dto.response.EnqueteResumoResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repository.*;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnqueteServiceTest {

    @Mock
    private EnqueteRepository enqueteRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private PerguntaRepository perguntaRepository;

    @Mock
    private EnqueteGrupoProcessor enqueteGrupoProcessor;

    @Mock
    private GrupoProfessorRepository grupoProfessorRepository;

    @Mock
    private GrupoMembroRepository grupoMembroRepository;

    @Mock
    private EnquetePerguntaRepository enquetePerguntaRepository;

    @Mock
    private EnqueteParticipacaoRepository enqueteParticipacaoRepository;

    @InjectMocks
    private EnqueteService enqueteService;

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private MockedStatic<SecurityContextHolder> mockLoggedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class);
        mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        return mockedStatic;
    }

    private EnquetePergunta criarSnapshot(Enquete enquete, Pergunta pergunta) {
        EnquetePergunta ep = new EnquetePergunta();
        ep.setEnquete(enquete);
        ep.setPergunta(pergunta);
        return ep;
    }

    @Test
    @DisplayName("Encerrar vencidas: delega o fechamento em massa ao repository")
    void encerrarEnquetesVencidasTest() {
        when(enqueteRepository.encerrarEnquetes(any(LocalDate.class))).thenReturn(3);

        enqueteService.encerrarEnquetesVencidas();

        verify(enqueteRepository).encerrarEnquetes(any(LocalDate.class));
    }

    @Test
    @DisplayName("Gerar enquetes: catálogo sem pergunta ativa cancela a geração")
    void gerarCatalogoVazioTest() {
        when(perguntaRepository.findByAtivaTrueOrderByOrdemAsc()).thenReturn(List.of());

        enqueteService.gerarEnquetesMensais();

        verify(grupoRepository, never()).findByStatus(any(StatusGrupo.class));
        verify(enqueteGrupoProcessor, never()).renovarEnquete(any(Grupo.class), any(LocalDate.class), anyList());
    }

    @Test
    @DisplayName("Gerar enquetes: processa todos os grupos ativos com as mesmas perguntas")
    void gerarProcessaTodosOsGruposTest() {
        Grupo grupo1 = TestDataFactory.criarGrupo(TestDataFactory.criarFaculdade(), StatusGrupo.ATIVO);
        Grupo grupo2 = TestDataFactory.criarGrupo(TestDataFactory.criarFaculdade(), StatusGrupo.ATIVO);
        grupo2.setId(2L);
        List<Pergunta> perguntas = List.of(TestDataFactory.criarPergunta(1L, (short) 10));

        when(perguntaRepository.findByAtivaTrueOrderByOrdemAsc()).thenReturn(perguntas);
        when(grupoRepository.findByStatus(StatusGrupo.ATIVO)).thenReturn(List.of(grupo1, grupo2));
        when(enqueteGrupoProcessor.renovarEnquete(eq(grupo1), any(LocalDate.class), eq(perguntas))).thenReturn(true);
        when(enqueteGrupoProcessor.renovarEnquete(eq(grupo2), any(LocalDate.class), eq(perguntas))).thenReturn(false);

        enqueteService.gerarEnquetesMensais();

        verify(enqueteGrupoProcessor).renovarEnquete(eq(grupo1), any(LocalDate.class), eq(perguntas));
        verify(enqueteGrupoProcessor).renovarEnquete(eq(grupo2), any(LocalDate.class), eq(perguntas));
    }

    @Test
    @DisplayName("Gerar enquetes: falha em um grupo não impede o processamento dos demais")
    void gerarFalhaIsoladaTest() {
        Grupo grupo1 = TestDataFactory.criarGrupo(TestDataFactory.criarFaculdade(), StatusGrupo.ATIVO);
        Grupo grupo2 = TestDataFactory.criarGrupo(TestDataFactory.criarFaculdade(), StatusGrupo.ATIVO);
        grupo2.setId(2L);
        List<Pergunta> perguntas = List.of(TestDataFactory.criarPergunta(1L, (short) 10));

        when(perguntaRepository.findByAtivaTrueOrderByOrdemAsc()).thenReturn(perguntas);
        when(grupoRepository.findByStatus(StatusGrupo.ATIVO)).thenReturn(List.of(grupo1, grupo2));
        when(enqueteGrupoProcessor.renovarEnquete(eq(grupo1), any(LocalDate.class), eq(perguntas)))
                .thenThrow(new RuntimeException("falha simulada"));
        when(enqueteGrupoProcessor.renovarEnquete(eq(grupo2), any(LocalDate.class), eq(perguntas))).thenReturn(true);

        assertDoesNotThrow(() -> enqueteService.gerarEnquetesMensais());

        verify(enqueteGrupoProcessor).renovarEnquete(eq(grupo2), any(LocalDate.class), eq(perguntas));
    }

    @Test
    @DisplayName("Enquete ativa: aluno membro recebe a enquete com as perguntas do snapshot")
    void buscarEnqueteAtivaComSucessoTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(aluno.getFaculdade(), StatusGrupo.ATIVO);
        Enquete enquete = TestDataFactory.criarEnquete(grupo);
        enquete.setDataFim(LocalDate.now(FUSO).plusDays(10));
        List<EnquetePergunta> snapshot = List.of(
                criarSnapshot(enquete, TestDataFactory.criarPergunta(1L, (short) 10)),
                criarSnapshot(enquete, TestDataFactory.criarPergunta(2L, (short) 20)));

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
            when(grupoMembroRepository.existsByGrupoAndAlunoAndStatus(grupo, aluno, StatusMembro.ATIVO)).thenReturn(true);
            when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.of(enquete));
            when(enquetePerguntaRepository.findByEnqueteOrderByPergunta_OrdemAsc(enquete)).thenReturn(snapshot);
            when(enqueteParticipacaoRepository.existsByEnqueteAndAluno(enquete, aluno)).thenReturn(false);

            EnqueteAtivaResponse response = enqueteService.buscarEnqueteAtivaDoGrupo(grupo.getId());

            assertThat(response.id()).isEqualTo(enquete.getId());
            assertThat(response.jaRespondeu()).isFalse();
            assertThat(response.perguntas()).hasSize(2);
            assertThat(response.perguntas().get(0).ordem()).isEqualTo((short) 10);
        }
    }

    @Test
    @DisplayName("Enquete ativa: jaRespondeu vem verdadeiro quando o aluno já tem participação")
    void buscarEnqueteAtivaJaRespondeuTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(aluno.getFaculdade(), StatusGrupo.ATIVO);
        Enquete enquete = TestDataFactory.criarEnquete(grupo);
        enquete.setDataFim(LocalDate.now(FUSO).plusDays(10));

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
            when(grupoMembroRepository.existsByGrupoAndAlunoAndStatus(grupo, aluno, StatusMembro.ATIVO)).thenReturn(true);
            when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.of(enquete));
            when(enquetePerguntaRepository.findByEnqueteOrderByPergunta_OrdemAsc(enquete)).thenReturn(List.of());
            when(enqueteParticipacaoRepository.existsByEnqueteAndAluno(enquete, aluno)).thenReturn(true);

            EnqueteAtivaResponse response = enqueteService.buscarEnqueteAtivaDoGrupo(grupo.getId());

            assertThat(response.jaRespondeu()).isTrue();
        }
    }

    @Test
    @DisplayName("Enquete ativa rejeitada: grupo não existe")
    void buscarEnqueteAtivaGrupoInexistenteTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RecursoNaoEncontradoException.class, () -> enqueteService.buscarEnqueteAtivaDoGrupo(999L));
        }
    }

    @Test
    @DisplayName("Enquete ativa rejeitada: grupo encerrado")
    void buscarEnqueteAtivaGrupoEncerradoTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(aluno.getFaculdade(), StatusGrupo.ENCERRADO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));

            assertThrows(RecursoNaoEncontradoException.class, () -> enqueteService.buscarEnqueteAtivaDoGrupo(grupo.getId()));
            verify(grupoMembroRepository, never()).existsByGrupoAndAlunoAndStatus(any(), any(), any());
        }
    }

    @Test
    @DisplayName("Enquete ativa rejeitada: aluno não é membro ativo do grupo")
    void buscarEnqueteAtivaAlunoNaoMembroTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(aluno.getFaculdade(), StatusGrupo.ATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
            when(grupoMembroRepository.existsByGrupoAndAlunoAndStatus(grupo, aluno, StatusMembro.ATIVO)).thenReturn(false);

            assertThrows(RecursoNaoEncontradoException.class, () -> enqueteService.buscarEnqueteAtivaDoGrupo(grupo.getId()));
            verify(enqueteRepository, never()).findByGrupoAndStatus(any(), any());
        }
    }

    @Test
    @DisplayName("Enquete ativa rejeitada: grupo não tem enquete ATIVA")
    void buscarEnqueteAtivaSemEnqueteTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(aluno.getFaculdade(), StatusGrupo.ATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
            when(grupoMembroRepository.existsByGrupoAndAlunoAndStatus(grupo, aluno, StatusMembro.ATIVO)).thenReturn(true);
            when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.empty());

            assertThrows(RecursoNaoEncontradoException.class, () -> enqueteService.buscarEnqueteAtivaDoGrupo(grupo.getId()));
        }
    }

    @Test
    @DisplayName("Enquete ativa rejeitada: dataFim igual a hoje conta como fechada, mesmo com status ATIVA")
    void buscarEnqueteAtivaDataFimHojeTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(aluno.getFaculdade(), StatusGrupo.ATIVO);
        Enquete enquete = TestDataFactory.criarEnquete(grupo);
        enquete.setDataFim(LocalDate.now(FUSO));

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
            when(grupoMembroRepository.existsByGrupoAndAlunoAndStatus(grupo, aluno, StatusMembro.ATIVO)).thenReturn(true);
            when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.of(enquete));

            assertThrows(RecursoNaoEncontradoException.class, () -> enqueteService.buscarEnqueteAtivaDoGrupo(grupo.getId()));
            verify(enquetePerguntaRepository, never()).findByEnqueteOrderByPergunta_OrdemAsc(any());
        }
    }

    @Test
    @DisplayName("Enquete ativa: dataFim amanhã ainda está aberta, hoje é o último dia para responder")
    void buscarEnqueteAtivaDataFimAmanhaTest() {
        Usuario aluno = TestDataFactory.criarUsuario(Role.ALUNO, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(aluno.getFaculdade(), StatusGrupo.ATIVO);
        Enquete enquete = TestDataFactory.criarEnquete(grupo);
        enquete.setDataFim(LocalDate.now(FUSO).plusDays(1));

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(aluno)) {
            when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
            when(grupoMembroRepository.existsByGrupoAndAlunoAndStatus(grupo, aluno, StatusMembro.ATIVO)).thenReturn(true);
            when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.of(enquete));
            when(enquetePerguntaRepository.findByEnqueteOrderByPergunta_OrdemAsc(enquete)).thenReturn(List.of());
            when(enqueteParticipacaoRepository.existsByEnqueteAndAluno(enquete, aluno)).thenReturn(false);

            EnqueteAtivaResponse response = enqueteService.buscarEnqueteAtivaDoGrupo(grupo.getId());

            assertThat(response.id()).isEqualTo(enquete.getId());
        }
    }

    @Test
    @DisplayName("Listar enquetes: professor vinculado recebe o histórico do grupo")
    void listarEnquetesDoGrupoComSucessoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ATIVO);
        GrupoProfessor vinculo = TestDataFactory.criarGrupoProfessor(grupo, professor, RoleGrupo.CO_DOCENTE);
        Enquete antiga = TestDataFactory.criarEnquete(grupo);
        antiga.setStatus(StatusEnquete.ENCERRADA);
        Enquete atual = TestDataFactory.criarEnquete(grupo);
        atual.setId(2L);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(grupo.getId(), professor))
                    .thenReturn(Optional.of(vinculo));
            when(enqueteRepository.findByGrupoOrderByDataInicioAsc(grupo)).thenReturn(List.of(antiga, atual));

            List<EnqueteResumoResponse> response = enqueteService.listarEnquetesDoGrupo(grupo.getId());

            assertThat(response).hasSize(2);
            assertThat(response.get(0).status()).isEqualTo(StatusEnquete.ENCERRADA);
            assertThat(response.get(1).status()).isEqualTo(StatusEnquete.ATIVA);
        }
    }

    @Test
    @DisplayName("Listar enquetes rejeitada: professor não vinculado ao grupo")
    void listarEnquetesDoGrupoProfessorNaoVinculadoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(1L, professor)).thenReturn(Optional.empty());

            assertThrows(RecursoNaoEncontradoException.class, () -> enqueteService.listarEnquetesDoGrupo(1L));
            verify(enqueteRepository, never()).findByGrupoOrderByDataInicioAsc(any());
        }
    }

    @Test
    @DisplayName("Listar enquetes: professor ainda vê o histórico de um grupo encerrado")
    void listarEnquetesGrupoEncerradoTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ENCERRADO);
        GrupoProfessor vinculo = TestDataFactory.criarGrupoProfessor(grupo, professor, RoleGrupo.CRIADOR);
        Enquete enquete = TestDataFactory.criarEnquete(grupo);
        enquete.setStatus(StatusEnquete.ENCERRADA);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(grupo.getId(), professor)).thenReturn(Optional.of(vinculo));
            when(enqueteRepository.findByGrupoOrderByDataInicioAsc(grupo)).thenReturn(List.of(enquete));

            assertThat(enqueteService.listarEnquetesDoGrupo(grupo.getId())).hasSize(1);
        }
    }

    @Test
    @DisplayName("Listar enquetes: grupo ainda sem enquete devolve lista vazia, não erro")
    void listarEnquetesGrupoSemEnqueteTest() {
        Usuario professor = TestDataFactory.criarUsuario(Role.PROFESSOR, StatusUsuario.ATIVO);
        Grupo grupo = TestDataFactory.criarGrupo(professor.getFaculdade(), StatusGrupo.ATIVO);
        GrupoProfessor vinculo = TestDataFactory.criarGrupoProfessor(grupo, professor, RoleGrupo.CRIADOR);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(professor)) {
            when(grupoProfessorRepository.findByGrupo_IdAndProfessor(grupo.getId(), professor)).thenReturn(Optional.of(vinculo));
            when(enqueteRepository.findByGrupoOrderByDataInicioAsc(grupo)).thenReturn(List.of());

            assertThat(enqueteService.listarEnquetesDoGrupo(grupo.getId())).isEmpty();
        }
    }
}