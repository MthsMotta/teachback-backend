package br.com.teachback.backend.controller;

import br.com.teachback.backend.docs.RotaProtegida;
import br.com.teachback.backend.dto.request.CodigoConviteRequest;
import br.com.teachback.backend.dto.request.GrupoRequest;
import br.com.teachback.backend.dto.response.GrupoProfessorResponse;
import br.com.teachback.backend.dto.response.GrupoResponse;
import br.com.teachback.backend.service.GrupoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Grupos", description = "Criação de grupos, entrada por código de convite e listagem. Rotas de professor e de aluno.")
@RotaProtegida
public class GrupoController {

    private final GrupoService grupoService;

    @PreAuthorize("hasRole('PROFESSOR')")
    @PostMapping
    @Operation(summary = "Cria um grupo", description = "Somente PROFESSOR. Devolve os dois códigos de convite: o do aluno e o de co-docente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Grupo criado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GrupoProfessorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<GrupoProfessorResponse> criar(@Valid @RequestBody GrupoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.criar(request));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PutMapping("/{id}")
    @Operation(summary = "Altera o nome do grupo", description = "Qualquer professor ativo do grupo, não só o criador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo atualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GrupoProfessorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, ou grupo já encerrado"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado, ou o professor não faz parte dele")
    })
    public ResponseEntity<GrupoProfessorResponse> atualizar(@PathVariable Long id, @Valid @RequestBody GrupoRequest request) {
        return ResponseEntity.ok(grupoService.atualizar(id, request));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PatchMapping("/{id}/encerrar")
    @Operation(summary = "Encerra o grupo", description = "Somente o professor criador. Também encerra a enquete aberta do grupo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Grupo encerrado"),
            @ApiResponse(responseCode = "400", description = "Grupo já encerrado"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado, ou o professor não faz parte dele")
    })
    public ResponseEntity<Void> encerrar(@PathVariable Long id) {
        grupoService.encerrar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ALUNO')")
    @PostMapping("/entrar-aluno")
    @Operation(summary = "Aluno entra no grupo pelo código de convite", description = "Somente ALUNO. Usa o código público do grupo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno entrou no grupo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GrupoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Código com formato inválido, ou aluno já faz parte do grupo"),
            @ApiResponse(responseCode = "404", description = "Código inválido, ou grupo encerrado")
    })
    public ResponseEntity<GrupoResponse> entrarComoAluno(@Valid @RequestBody CodigoConviteRequest request) {
        return ResponseEntity.ok(grupoService.entrarComoAluno(request));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PostMapping("/entrar-professor")
    @Operation(summary = "Professor entra no grupo como co-docente", description = "Somente PROFESSOR. Usa o código privado, que o criador repassa manualmente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor entrou como CO_DOCENTE",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GrupoProfessorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Código com formato inválido, ou professor já faz parte do grupo"),
            @ApiResponse(responseCode = "404", description = "Código inválido, ou grupo encerrado")
    })
    public ResponseEntity<GrupoProfessorResponse> entrarComoProfessor(@Valid @RequestBody CodigoConviteRequest request) {
        return ResponseEntity.ok(grupoService.entrarComoProfessor(request));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @GetMapping("/professor")
    @Operation(summary = "Lista os grupos ativos do professor", description = "Inclui grupos como criador e como co-docente. Paginado, 20 por página.")
    @ApiResponse(responseCode = "200", description = "Página de grupos")
    public ResponseEntity<Page<GrupoProfessorResponse>> listarGruposDoProfessor(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(grupoService.listarGruposDoProfessor(pageable));
    }

    @PreAuthorize("hasRole('ALUNO')")
    @GetMapping("/aluno")
    @Operation(summary = "Lista os grupos ativos do aluno", description = "Só grupos ativos em que o aluno é membro ativo. Paginado, 20 por página.")
    @ApiResponse(responseCode = "200", description = "Página de grupos")
    public ResponseEntity<Page<GrupoResponse>> listarGruposDoAluno(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(grupoService.listarGruposDoAluno(pageable));
    }
}