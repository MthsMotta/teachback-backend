package br.com.teachback.backend.dto.response;

import java.time.LocalDate;
import java.util.List;

public record EnqueteAtivaResponse(Long id, LocalDate dataInicio, LocalDate dataFim,
                                   boolean jaRespondeu, List<PerguntaResponse> perguntas) {}
