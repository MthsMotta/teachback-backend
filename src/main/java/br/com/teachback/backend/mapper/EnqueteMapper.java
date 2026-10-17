package br.com.teachback.backend.mapper;

import br.com.teachback.backend.dto.response.EnqueteAtivaResponse;
import br.com.teachback.backend.dto.response.EnqueteResumoResponse;
import br.com.teachback.backend.dto.response.PerguntaResponse;
import br.com.teachback.backend.model.Enquete;
import br.com.teachback.backend.model.EnquetePergunta;

import java.util.List;

public final class EnqueteMapper {

    private EnqueteMapper() {}

    public static EnqueteResumoResponse toResumoDTO(Enquete enquete) {
        return new EnqueteResumoResponse(enquete.getId(), enquete.getDataInicio(),
                enquete.getDataFim(), enquete.getStatus());
    }

    public static EnqueteAtivaResponse toAtivaDTO(Enquete enquete, List<EnquetePergunta> snapshot, boolean jaRespondeu) {
        List<PerguntaResponse> perguntas = snapshot.stream()
                .map(ep -> new PerguntaResponse(ep.getPergunta().getId(),
                        ep.getPergunta().getTexto(), ep.getPergunta().getOrdem()))
                .toList();
        return new EnqueteAtivaResponse(enquete.getId(), enquete.getDataInicio(),
                enquete.getDataFim(), jaRespondeu, perguntas);
    }
}