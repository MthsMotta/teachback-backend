package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.response.EnqueteAtivaResponse;
import br.com.teachback.backend.dto.response.EnqueteResumoResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.mapper.EnqueteMapper;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnqueteService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final EnqueteRepository enqueteRepository;
    private final GrupoRepository grupoRepository;
    private final PerguntaRepository perguntaRepository;
    private final EnqueteGrupoProcessor enqueteGrupoProcessor;
    private final GrupoProfessorRepository grupoProfessorRepository;
    private final GrupoMembroRepository grupoMembroRepository;
    private final EnquetePerguntaRepository enquetePerguntaRepository;
    private final EnqueteParticipacaoRepository enqueteParticipacaoRepository;

    @Transactional
    public void encerrarEnquetesVencidas() {
        LocalDate hoje = LocalDate.now(FUSO);
        int encerradas = enqueteRepository.encerrarEnquetes(hoje);
        log.info("Fechamento de enquetes em {}: {} encerrada(s)", hoje, encerradas);
    }

    public void gerarEnquetesMensais() {
        LocalDate hoje = LocalDate.now(FUSO);

        List<Pergunta> perguntasAtivas = perguntaRepository.findByAtivaTrueOrderByOrdemAsc();
        if (perguntasAtivas.isEmpty()) {
            log.error("Geração de enquetes cancelada em {}: não há nenhuma pergunta ativa no catálogo", hoje);
            return;
        }

        List<Grupo> grupos = grupoRepository.findByStatus(StatusGrupo.ATIVO);

        int criadas = 0;
        int puladas = 0;
        int falhas = 0;

        for (Grupo grupo : grupos) {
            try {
                if (enqueteGrupoProcessor.renovarEnquete(grupo, hoje, perguntasAtivas)) {
                    criadas++;
                } else {
                    puladas++;
                }
            } catch (Exception e) {
                falhas++;
                log.error("Falha ao renovar a enquete do grupo {}", grupo.getId(), e);
            }
        }

        log.info("Geração de enquetes em {}: {} criada(s), {} pulada(s), {} falha(s)", hoje, criadas, puladas, falhas);
    }

    @Transactional(readOnly = true)
    public EnqueteAtivaResponse buscarEnqueteAtivaDoGrupo(Long grupoId) {
        Usuario aluno = usuarioLogado();

        Grupo grupo = grupoRepository.findById(grupoId)
                .filter(g -> g.getStatus() == StatusGrupo.ATIVO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo não encontrado."));

        if (!grupoMembroRepository.existsByGrupoAndAlunoAndStatus(grupo, aluno, StatusMembro.ATIVO)) {
            throw new RecursoNaoEncontradoException("Grupo não encontrado.");
        }

        LocalDate hoje = LocalDate.now(FUSO);
        Enquete enquete = enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)
                .filter(e -> e.getDataFim().isAfter(hoje))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não há enquete aberta para este grupo."));

        List<EnquetePergunta> snapshot = enquetePerguntaRepository.findByEnqueteOrderByPergunta_OrdemAsc(enquete);
        boolean jaRespondeu = enqueteParticipacaoRepository.existsByEnqueteAndAluno(enquete, aluno);
        return EnqueteMapper.toAtivaDTO(enquete, snapshot, jaRespondeu);
    }

    @Transactional(readOnly = true)
    public List<EnqueteResumoResponse> listarEnquetesDoGrupo(Long grupoId) {
        Usuario professor = usuarioLogado();

        GrupoProfessor vinculo = grupoProfessorRepository.findByGrupo_IdAndProfessor(grupoId, professor)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo não encontrado."));

        return enqueteRepository.findByGrupoOrderByDataInicioAsc(vinculo.getGrupo()).stream()
                .map(EnqueteMapper::toResumoDTO)
                .toList();
    }

    private Usuario usuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}