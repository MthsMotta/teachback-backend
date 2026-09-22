package br.com.teachback.backend.controller;

import br.com.teachback.backend.dto.request.FaculdadeDominioRequest;
import br.com.teachback.backend.dto.request.FaculdadeRequest;
import br.com.teachback.backend.dto.response.FaculdadeAutoCompleteResponse;
import br.com.teachback.backend.dto.response.FaculdadeDominioResponse;
import br.com.teachback.backend.dto.response.FaculdadeResponse;
import br.com.teachback.backend.service.FaculdadeDominioService;
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
    private final FaculdadeDominioService faculdadeDominioService;

    public FaculdadeController(FaculdadeService faculdadeService, FaculdadeDominioService faculdadeDominioService) {
        this.faculdadeService = faculdadeService;
        this.faculdadeDominioService = faculdadeDominioService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FaculdadeResponse> cadastrar(@Valid @RequestBody FaculdadeRequest faculdadeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faculdadeService.cadastrar(faculdadeRequest));
    }

    @GetMapping
    public ResponseEntity<List<FaculdadeResponse>> listar() {
        return ResponseEntity.ok(faculdadeService.listar());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<FaculdadeAutoCompleteResponse>> buscar(@RequestParam String termo) {
        return ResponseEntity.ok(faculdadeService.listarPorNomeOuSigla(termo));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FaculdadeResponse> atualizar(@PathVariable Long id, @Valid @RequestBody FaculdadeRequest faculdadeRequest) {
        return ResponseEntity.ok(faculdadeService.atualizar(id, faculdadeRequest));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERADOR_CHEFE')")
    @PostMapping("/{id}/dominios")
    public ResponseEntity<FaculdadeDominioResponse> cadastrarDominio(@PathVariable Long id, @Valid @RequestBody FaculdadeDominioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faculdadeDominioService.cadastrar(id, request));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERADOR_CHEFE')")
    @GetMapping("/{id}/dominios")
    public ResponseEntity<List<FaculdadeDominioResponse>> listarDominios(@PathVariable Long id) {
        return ResponseEntity.ok(faculdadeDominioService.listar(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERADOR_CHEFE')")
    @DeleteMapping("/dominios/{id}")
    public ResponseEntity<Void> excluirDominio(@PathVariable Long id) {
        faculdadeDominioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
