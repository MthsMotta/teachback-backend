package br.com.teachback.backend.controller;

import br.com.teachback.backend.dto.response.EnqueteAtivaResponse;
import br.com.teachback.backend.dto.response.EnqueteResumoResponse;
import br.com.teachback.backend.service.EnqueteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/grupos/{grupoId}/enquetes")
@RequiredArgsConstructor
public class EnqueteController {

    private final EnqueteService enqueteService;

    @PreAuthorize("hasRole('ALUNO')")
    @GetMapping("/ativa")
    public ResponseEntity<EnqueteAtivaResponse> buscarAtiva(@PathVariable Long grupoId) {
        return ResponseEntity.ok(enqueteService.buscarEnqueteAtivaDoGrupo(grupoId));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @GetMapping
    public ResponseEntity<List<EnqueteResumoResponse>> listar(@PathVariable Long grupoId) {
        return ResponseEntity.ok(enqueteService.listarEnquetesDoGrupo(grupoId));
    }
}