package br.com.teachback.backend.controller;

import br.com.teachback.backend.docs.RotaProtegida;
import br.com.teachback.backend.dto.request.ModeradorChefeRequest;
import br.com.teachback.backend.dto.response.MensagemResponse;
import br.com.teachback.backend.service.ModeradorChefeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Moderadores-chefe", description = "Cadastro de moderadores-chefe. Somente ADMIN.")
@RotaProtegida
@RestController
@RequestMapping("/moderadores-chefe")
public class ModeradorChefeController {

    private final ModeradorChefeService moderadorChefeService;

    public ModeradorChefeController(ModeradorChefeService moderadorChefeService) {
        this.moderadorChefeService = moderadorChefeService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastro")
    @Operation(summary = "Cadastra um moderador-chefe",
            description = "Somente ADMIN. O e-mail precisa ter um domínio cadastrado na faculdade informada. A conta já nasce ativa, sem e-mail de confirmação.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Moderador-chefe cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, domínio do e-mail não pertence à faculdade, ou e-mail já cadastrado"),
            @ApiResponse(responseCode = "404", description = "Faculdade não encontrada")
    })
    public ResponseEntity<MensagemResponse> cadastro(@Valid @RequestBody ModeradorChefeRequest request) {
        moderadorChefeService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensagemResponse("Moderador chefe criado com sucesso"));
    }
}
