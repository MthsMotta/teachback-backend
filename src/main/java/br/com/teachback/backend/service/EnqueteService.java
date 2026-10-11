package br.com.teachback.backend.service;

import br.com.teachback.backend.model.Grupo;
import br.com.teachback.backend.model.Pergunta;
import br.com.teachback.backend.repository.EnqueteRepository;
import br.com.teachback.backend.repository.GrupoRepository;
import br.com.teachback.backend.repository.PerguntaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        List<Grupo> grupos = grupoRepository.findAllByStatusATIVO();

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
}