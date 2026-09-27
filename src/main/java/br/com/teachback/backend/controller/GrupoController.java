package br.com.teachback.backend.controller;

import br.com.teachback.backend.dto.request.CodigoConviteRequest;
import br.com.teachback.backend.dto.request.GrupoRequest;
import br.com.teachback.backend.dto.response.GrupoProfessorResponse;
import br.com.teachback.backend.dto.response.GrupoResponse;
import br.com.teachback.backend.service.GrupoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @PreAuthorize("hasRole('PROFESSOR')")
    @PostMapping
    public ResponseEntity<GrupoProfessorResponse> criar(@Valid @RequestBody GrupoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.criar(request));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PutMapping("/{id}")
    public ResponseEntity<GrupoProfessorResponse> atualizar(@PathVariable Long id, @Valid @RequestBody GrupoRequest request) {
        return ResponseEntity.ok(grupoService.atualizar(id, request));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<Void> encerrar(@PathVariable Long id) {
        grupoService.encerrar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ALUNO')")
    @PostMapping("/entrar-aluno")
    public ResponseEntity<GrupoResponse> entrarComoAluno(@Valid @RequestBody CodigoConviteRequest request) {
        return ResponseEntity.ok(grupoService.entrarComoAluno(request));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PostMapping("/entrar-professor")
    public ResponseEntity<GrupoProfessorResponse> entrarComoProfessor(@Valid @RequestBody CodigoConviteRequest request) {
        return ResponseEntity.ok(grupoService.entrarComoProfessor(request));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @GetMapping("/professor")
    public ResponseEntity<Page<GrupoProfessorResponse>> listarGruposDoProfessor(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(grupoService.listarGruposDoProfessor(pageable));
    }

    @PreAuthorize("hasRole('ALUNO')")
    @GetMapping("/aluno")
    public ResponseEntity<Page<GrupoResponse>> listarGruposDoAluno(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(grupoService.listarGruposDoAluno(pageable));
    }
}