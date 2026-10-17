package br.com.teachback.backend.service;

import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repository.EnquetePerguntaRepository;
import br.com.teachback.backend.repository.EnqueteRepository;
import br.com.teachback.backend.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnqueteGrupoProcessorTest {

    @Mock
    private EnqueteRepository enqueteRepository;

    @Mock
    private EnquetePerguntaRepository enquetePerguntaRepository;

    @InjectMocks
    private EnqueteGrupoProcessor processor;

    private final Grupo grupo = TestDataFactory.criarGrupo(TestDataFactory.criarFaculdade(), StatusGrupo.ATIVO);
    private final List<Pergunta> perguntas = List.of(
            TestDataFactory.criarPergunta(1L, (short) 10),
            TestDataFactory.criarPergunta(2L, (short) 20));

    @Test
    @DisplayName("Renovar: grupo sem enquete ativa recebe enquete nova com snapshot das perguntas")
    void renovarGrupoSemEnqueteAtivaTest() {
        LocalDate hoje = LocalDate.of(2026, 10, 28);
        when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.empty());
        when(enqueteRepository.existsByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(false);
        when(enqueteRepository.save(any(Enquete.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean criou = processor.renovarEnquete(grupo, hoje, perguntas);

        assertThat(criou).isTrue();
        ArgumentCaptor<Enquete> captor = ArgumentCaptor.forClass(Enquete.class);
        verify(enqueteRepository).save(captor.capture());
        Enquete nova = captor.getValue();
        assertThat(nova.getGrupo()).isSameAs(grupo);
        assertThat(nova.getStatus()).isEqualTo(StatusEnquete.ATIVA);
        assertThat(nova.getDataInicio()).isEqualTo(hoje);
        assertThat(nova.getDataFim()).isEqualTo(LocalDate.of(2026, 11, 21));
        verify(enquetePerguntaRepository, times(perguntas.size())).save(any(EnquetePergunta.class));
    }

    @Test
    @DisplayName("Renovar: em dezembro, a dataFim vira dia 21 de janeiro do ano seguinte")
    void renovarVirandoOAnoTest() {
        LocalDate hoje = LocalDate.of(2026, 12, 28);
        when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.empty());
        when(enqueteRepository.existsByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(false);
        when(enqueteRepository.save(any(Enquete.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.renovarEnquete(grupo, hoje, perguntas);

        ArgumentCaptor<Enquete> captor = ArgumentCaptor.forClass(Enquete.class);
        verify(enqueteRepository).save(captor.capture());
        assertThat(captor.getValue().getDataFim()).isEqualTo(LocalDate.of(2027, 1, 21));
    }

    @Test
    @DisplayName("Renovar: grupo com enquete ativa que ainda não venceu é pulado")
    void renovarGrupoComEnqueteAindaValidaTest() {
        LocalDate hoje = LocalDate.of(2026, 10, 28);
        Enquete ativa = TestDataFactory.criarEnquete(grupo);
        ativa.setDataFim(LocalDate.of(2026, 11, 21));
        when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.of(ativa));
        when(enqueteRepository.existsByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(true);

        boolean criou = processor.renovarEnquete(grupo, hoje, perguntas);

        assertThat(criou).isFalse();
        assertThat(ativa.getStatus()).isEqualTo(StatusEnquete.ATIVA);
        verify(enqueteRepository, never()).save(any(Enquete.class));
        verify(enquetePerguntaRepository, never()).save(any(EnquetePergunta.class));
    }

    @Test
    @DisplayName("Renovar: enquete vencida que sobrou (job do dia 21 falhou) é fechada e a nova é criada")
    void renovarFechaVencidaECriaNovaTest() {
        LocalDate hoje = LocalDate.of(2026, 10, 28);
        Enquete vencida = TestDataFactory.criarEnquete(grupo);
        vencida.setDataFim(LocalDate.of(2026, 10, 21));
        when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.of(vencida));
        when(enqueteRepository.existsByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(false);
        when(enqueteRepository.save(any(Enquete.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean criou = processor.renovarEnquete(grupo, hoje, perguntas);

        assertThat(criou).isTrue();
        assertThat(vencida.getStatus()).isEqualTo(StatusEnquete.ENCERRADA);
        verify(enqueteRepository, times(2)).save(any(Enquete.class));
    }

    @Test
    @DisplayName("Renovar: enquete com dataFim igual a hoje já conta como vencida")
    void renovarDataFimIgualAHojeTest() {
        LocalDate hoje = LocalDate.of(2026, 10, 28);
        Enquete noLimite = TestDataFactory.criarEnquete(grupo);
        noLimite.setDataFim(hoje);
        when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.of(noLimite));
        when(enqueteRepository.existsByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(false);
        when(enqueteRepository.save(any(Enquete.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.renovarEnquete(grupo, hoje, perguntas);

        assertThat(noLimite.getStatus()).isEqualTo(StatusEnquete.ENCERRADA);
    }

    @Test
    @DisplayName("Renovar: cada pergunta ativa vira uma linha de snapshot ligada à enquete nova")
    void renovarSnapshotLigaPerguntasNaEnqueteNovaTest() {
        LocalDate hoje = LocalDate.of(2026, 10, 28);
        when(enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(Optional.empty());
        when(enqueteRepository.existsByGrupoAndStatus(grupo, StatusEnquete.ATIVA)).thenReturn(false);
        when(enqueteRepository.save(any(Enquete.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.renovarEnquete(grupo, hoje, perguntas);

        ArgumentCaptor<Enquete> enqueteCaptor = ArgumentCaptor.forClass(Enquete.class);
        verify(enqueteRepository).save(enqueteCaptor.capture());
        Enquete nova = enqueteCaptor.getValue();

        ArgumentCaptor<EnquetePergunta> snapshotCaptor = ArgumentCaptor.forClass(EnquetePergunta.class);
        verify(enquetePerguntaRepository, times(perguntas.size())).save(snapshotCaptor.capture());
        assertThat(snapshotCaptor.getAllValues()).allSatisfy(ep -> assertThat(ep.getEnquete()).isSameAs(nova));
        assertThat(snapshotCaptor.getAllValues())
                .extracting(EnquetePergunta::getPergunta)
                .containsExactlyInAnyOrderElementsOf(perguntas);
    }
}