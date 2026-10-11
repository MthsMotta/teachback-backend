package br.com.teachback.backend.service;

import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repository.EnquetePerguntaRepository;
import br.com.teachback.backend.repository.EnqueteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnqueteGrupoProcessor {

    private final EnqueteRepository enqueteRepository;
    private final EnquetePerguntaRepository enquetePerguntaRepository;

    @Transactional
    public boolean renovarEnquete(Grupo grupo, LocalDate dataProcessamento, List<Pergunta> perguntasAtivas) {
        Optional<Enquete> enqueteAtiva = enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA);

        if (enqueteAtiva.isPresent() && !enqueteAtiva.get().getDataFim().isAfter(dataProcessamento)) {
            Enquete vencida = enqueteAtiva.get();
            vencida.setStatus(StatusEnquete.ENCERRADA);
            enqueteRepository.save(vencida);
        }

        if (enqueteRepository.existsByGrupoAndStatusATIVA(grupo)) {
            return false;
        }

        Enquete novaEnquete = new Enquete();
        novaEnquete.setGrupo(grupo);
        novaEnquete.setStatus(StatusEnquete.ATIVA);
        novaEnquete.setDataInicio(dataProcessamento);
        novaEnquete.setDataFim(dataProcessamento.plusMonths(1).withDayOfMonth(21));
        novaEnquete = enqueteRepository.save(novaEnquete);

        for (Pergunta pergunta : perguntasAtivas) {
            EnquetePergunta enquetePergunta = new EnquetePergunta();
            enquetePergunta.setEnquete(novaEnquete);
            enquetePergunta.setPergunta(pergunta);
            enquetePerguntaRepository.save(enquetePergunta);
        }

        return true;
    }
}