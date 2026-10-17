package br.com.teachback.backend.dto.response;

import br.com.teachback.backend.model.StatusEnquete;

import java.time.LocalDate;

public record EnqueteResumoResponse(Long id, LocalDate dataInicio, LocalDate dataFim, StatusEnquete status) {}

