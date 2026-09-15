package br.com.teachback.backend.controller;

import br.com.teachback.backend.dto.request.FaculdadeRequest;
import br.com.teachback.backend.dto.response.FaculdadeResponse;
import br.com.teachback.backend.service.FaculdadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/faculdades")
public class FaculdadeController {

    private final FaculdadeService faculdadeService;

    public FaculdadeController(FaculdadeService faculdadeService) {
        this.faculdadeService = faculdadeService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FaculdadeResponse> criar(@Valid @RequestBody FaculdadeRequest faculdadeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faculdadeService.criar(faculdadeRequest));
    }

    @GetMapping
    public ResponseEntity<List<FaculdadeResponse>> listar() {
        return ResponseEntity.ok(faculdadeService.listar());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FaculdadeResponse> atualizar(@PathVariable Long id, @Valid @RequestBody FaculdadeRequest faculdadeRequest) {
        return ResponseEntity.ok(faculdadeService.atualizar(id, faculdadeRequest));
    }
}
