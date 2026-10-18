package br.com.teachback.backend.controller;

import br.com.teachback.backend.dto.request.FaculdadeDominioRequest;
import br.com.teachback.backend.dto.request.FaculdadeRequest;
import br.com.teachback.backend.dto.response.FaculdadeAutoCompleteResponse;
import br.com.teachback.backend.dto.response.FaculdadeDominioResponse;
import br.com.teachback.backend.dto.response.FaculdadeResponse;
import br.com.teachback.backend.service.FaculdadeDominioService;
import br.com.teachback.backend.service.FaculdadeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.com.teachback.backend.docs.RotaProtegida;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@RestController
@RequestMapping("/faculdades")
@Tag(name = "Faculdades", description = "Cadastro de faculdades e dos domínios de e-mail aceitos em cada uma.")
public class FaculdadeController {

    private final FaculdadeService faculdadeService;
    private final FaculdadeDominioService faculdadeDominioService;

    public FaculdadeController(FaculdadeService faculdadeService, FaculdadeDominioService faculdadeDominioService) {
        this.faculdadeService = faculdadeService;
        this.faculdadeDominioService = faculdadeDominioService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @RotaProtegida
    @Operation(summary = "Cadastra uma faculdade", description = "Somente ADMIN. Nome e sigla juntos não podem repetir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Faculdade cadastrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaculdadeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, ou já existe faculdade com esse nome e sigla")
    })
    public ResponseEntity<FaculdadeResponse> cadastrar(@Valid @RequestBody FaculdadeRequest faculdadeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faculdadeService.cadastrar(faculdadeRequest));
    }

    @GetMapping
    @Operation(summary = "Lista todas as faculdades", description = "Rota pública, usada no cadastro de usuário.")
    @ApiResponse(responseCode = "200", description = "Lista de faculdades",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FaculdadeResponse.class))))
    public ResponseEntity<List<FaculdadeResponse>> listar() {
        return ResponseEntity.ok(faculdadeService.listar());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca faculdades por nome ou sigla", description = "Rota pública, para o autocomplete. Ignora maiúsculas e minúsculas.")
    @ApiResponse(responseCode = "200", description = "Faculdades que contêm o termo (lista vazia se nenhuma)",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FaculdadeAutoCompleteResponse.class))))
    public ResponseEntity<List<FaculdadeAutoCompleteResponse>> buscar(@RequestParam String termo) {
        return ResponseEntity.ok(faculdadeService.listarPorNomeOuSigla(termo));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @RotaProtegida
    @Operation(summary = "Atualiza uma faculdade", description = "Somente ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Faculdade atualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaculdadeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, ou nome e sigla já usados por outra faculdade"),
            @ApiResponse(responseCode = "404", description = "Faculdade não encontrada")
    })
    public ResponseEntity<FaculdadeResponse> atualizar(@PathVariable Long id, @Valid @RequestBody FaculdadeRequest faculdadeRequest) {
        return ResponseEntity.ok(faculdadeService.atualizar(id, faculdadeRequest));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERADOR_CHEFE')")
    @PostMapping("/{id}/dominios")
    @RotaProtegida
    @Operation(summary = "Cadastra um domínio de e-mail na faculdade",
            description = "ADMIN em qualquer faculdade. MODERADOR_CHEFE só na própria.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Domínio cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaculdadeDominioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, domínio já cadastrado, ou moderador-chefe tentando outra faculdade"),
            @ApiResponse(responseCode = "404", description = "Faculdade não encontrada")
    })
    public ResponseEntity<FaculdadeDominioResponse> cadastrarDominio(@PathVariable Long id, @Valid @RequestBody FaculdadeDominioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faculdadeDominioService.cadastrar(id, request));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERADOR_CHEFE')")
    @GetMapping("/{id}/dominios")
    @RotaProtegida
    @Operation(summary = "Lista os domínios de e-mail da faculdade", description = "ADMIN ou MODERADOR_CHEFE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de domínios",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FaculdadeDominioResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Faculdade não encontrada"),
            @ApiResponse(responseCode = "400", description = "Moderador-chefe tentando listar domínios de outra faculdade")
    })
    public ResponseEntity<List<FaculdadeDominioResponse>> listarDominios(@PathVariable Long id) {
        return ResponseEntity.ok(faculdadeDominioService.listar(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERADOR_CHEFE')")
    @DeleteMapping("/dominios/{id}")
    @RotaProtegida
    @Operation(summary = "Exclui um domínio de e-mail", description = "ADMIN em qualquer faculdade. MODERADOR_CHEFE só da própria.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Domínio excluído"),
            @ApiResponse(responseCode = "400", description = "Moderador-chefe tentando excluir domínio de outra faculdade"),
            @ApiResponse(responseCode = "404", description = "Domínio não encontrado")
    })
    public ResponseEntity<Void> excluirDominio(@PathVariable Long id) {
        faculdadeDominioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
