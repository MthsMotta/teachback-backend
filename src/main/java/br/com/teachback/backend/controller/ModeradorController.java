package br.com.teachback.backend.controller;

import br.com.teachback.backend.docs.RotaProtegida;
import br.com.teachback.backend.dto.request.ModeradorRequest;
import br.com.teachback.backend.dto.response.MensagemResponse;
import br.com.teachback.backend.dto.response.ModeradorResponse;
import br.com.teachback.backend.dto.response.ProfessorResponse;
import br.com.teachback.backend.service.ModeradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moderadores")
@Tag(name = "Moderadores", description = "Gestão de moderadores e de professores da faculdade do moderador logado.")
@RotaProtegida
public class ModeradorController {

    private final ModeradorService moderadorService;

    public ModeradorController(ModeradorService moderadorService) {
        this.moderadorService = moderadorService;
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE')")
    @PostMapping("/cadastro")
    @Operation(summary = "Cadastra um moderador", description = "Somente MODERADOR_CHEFE. O moderador é criado na faculdade do chefe logado, e o domínio do e-mail precisa estar cadastrado nela.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Moderador criado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, ou domínio do e-mail não pertence à faculdade")
    })
    public ResponseEntity<MensagemResponse> cadastro(@Valid @RequestBody  ModeradorRequest request){
        moderadorService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensagemResponse("Moderador criado com sucesso"));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE')")
    @GetMapping
    @Operation(summary = "Lista os moderadores da faculdade", description = "Somente MODERADOR_CHEFE. Traz os moderadores comuns da faculdade do chefe logado.")
    @ApiResponse(responseCode = "200", description = "Lista de moderadores",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ModeradorResponse.class))))
    public ResponseEntity<List<ModeradorResponse>> listar(){
        return ResponseEntity.ok(moderadorService.listar());
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE') or hasRole('MODERADOR')")
    @GetMapping("/professores")
    @Operation(summary = "Lista os professores da faculdade", description = "MODERADOR ou MODERADOR_CHEFE. Paginado, 20 por página.")
    @ApiResponse(responseCode = "200", description = "Página de professores")
    public ResponseEntity<Page<ProfessorResponse>> listarProfessores(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(moderadorService.listarProfessores(pageable));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE') or hasRole('MODERADOR')")
    @GetMapping("/professores/pendentes")
    @Operation(summary = "Lista os professores aguardando aprovação", description = "MODERADOR ou MODERADOR_CHEFE. Paginado, 20 por página.")
    @ApiResponse(responseCode = "200", description = "Página de professores pendentes")
    public ResponseEntity<Page<ProfessorResponse>> listarProfessoresPendentes(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(moderadorService.listarProfessoresPendentes(pageable));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE')")
    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativa um moderador", description = "Somente MODERADOR_CHEFE. Só vale para moderador da mesma faculdade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Moderador desativado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Moderador de outra faculdade, ou usuário não é moderador"),
            @ApiResponse(responseCode = "404", description = "Moderador não encontrado")
    })
    public ResponseEntity<MensagemResponse> desativarModerador(@PathVariable Long id){
        moderadorService.desativarModerador(id);
        return ResponseEntity.ok(new MensagemResponse("Moderador desativado com sucesso"));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE')")
    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativa um moderador", description = "Somente MODERADOR_CHEFE. Só vale para moderador da mesma faculdade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Moderador ativado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Moderador de outra faculdade, ou usuário não é moderador"),
            @ApiResponse(responseCode = "404", description = "Moderador não encontrado")
    })
    public ResponseEntity<MensagemResponse> ativarModerador(@PathVariable Long id){
        moderadorService.ativarModerador(id);
        return ResponseEntity.ok(new MensagemResponse("Moderador ativado com sucesso"));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE') or hasRole('MODERADOR')")
    @PatchMapping("/{id}/rejeitar-professor")
    @Operation(summary = "Rejeita um professor pendente", description = "MODERADOR ou MODERADOR_CHEFE. A conta fica inativa. Só vale para professor da mesma faculdade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor rejeitado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Professor de outra faculdade, usuário não é professor, ou não está pendente de aprovação"),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    })
    public ResponseEntity<MensagemResponse> rejeitarProfessor(@PathVariable Long id){
        moderadorService.rejeitarProfessor(id);
        return ResponseEntity.ok(new MensagemResponse("Professor rejeitado com sucesso"));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE') or hasRole('MODERADOR')")
    @PatchMapping("/{id}/aprovar-professor")
    @Operation(summary = "Aprova um professor pendente", description = "MODERADOR ou MODERADOR_CHEFE. O professor passa a ficar ativo. Só vale para professor da mesma faculdade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor aprovado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Professor de outra faculdade, usuário não é professor, ou não está pendente de aprovação"),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    })
    public ResponseEntity<MensagemResponse> aprovarProfessor(@PathVariable Long id){
        moderadorService.aprovarProfessor(id);
        return ResponseEntity.ok(new MensagemResponse("Professor aprovado com sucesso"));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE') or hasRole('MODERADOR')")
    @PatchMapping("/{id}/desativar-professor")
    @Operation(summary = "Desativa um professor", description = "MODERADOR ou MODERADOR_CHEFE. Inativa uma conta ativa. Só vale para professor da mesma faculdade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor desativado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Professor de outra faculdade, usuário não é professor, ou ainda pendente de aprovação"),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    })
    public ResponseEntity<MensagemResponse> desativarProfessor(@PathVariable Long id){
        moderadorService.desativarProfessor(id);
        return ResponseEntity.ok(new MensagemResponse("Professor desativado com sucesso"));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE') or hasRole('MODERADOR')")
    @PatchMapping("/{id}/ativar-professor")
    @Operation(summary = "Ativa um professor", description = "MODERADOR ou MODERADOR_CHEFE. Reativa uma conta inativa. Só vale para professor da mesma faculdade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor ativado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Professor de outra faculdade, usuário não é professor, ou ainda pendente de aprovação"),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    })
    public ResponseEntity<MensagemResponse> ativarProfessor(@PathVariable Long id){
        moderadorService.ativarProfessor(id);
        return ResponseEntity.ok(new MensagemResponse("Professor ativado com sucesso"));
    }
}
