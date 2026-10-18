package br.com.teachback.backend.controller;

import br.com.teachback.backend.docs.RotaProtegida;
import br.com.teachback.backend.dto.response.EnqueteAtivaResponse;
import br.com.teachback.backend.dto.response.EnqueteResumoResponse;
import br.com.teachback.backend.service.EnqueteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Enquetes", description = "Leitura da enquete aberta (aluno) e do histórico do grupo (professor).")
@RotaProtegida
public class EnqueteController {

    private final EnqueteService enqueteService;

    @PreAuthorize("hasRole('ALUNO')")
    @GetMapping("/ativa")
    @Operation(summary = "Busca a enquete aberta do grupo",
            description = "Somente ALUNO membro. Traz as perguntas em ordem e se o aluno já respondeu. Uma enquete com dataFim igual a hoje já conta como fechada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enquete aberta",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnqueteAtivaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado, aluno não é membro, ou não há enquete aberta")
    })
    public ResponseEntity<EnqueteAtivaResponse> buscarAtiva(@PathVariable Long grupoId) {
        return ResponseEntity.ok(enqueteService.buscarEnqueteAtivaDoGrupo(grupoId));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @GetMapping
    @Operation(summary = "Lista as enquetes do grupo",
            description = "Qualquer professor do grupo. Em ordem cronológica, inclusive as encerradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico de enquetes (lista vazia se ainda não houve nenhuma)",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EnqueteResumoResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado, ou o professor não faz parte dele")
    })
    public ResponseEntity<List<EnqueteResumoResponse>> listar(@PathVariable Long grupoId) {
        return ResponseEntity.ok(enqueteService.listarEnquetesDoGrupo(grupoId));
    }
}