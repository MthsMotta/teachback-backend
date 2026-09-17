package br.com.teachback.backend.controller;

import br.com.teachback.backend.dto.request.ModeradorRequest;
import br.com.teachback.backend.dto.response.MensagemResponse;
import br.com.teachback.backend.dto.response.ModeradorResponse;
import br.com.teachback.backend.service.ModeradorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moderadores")
public class ModeradorController {

    private final ModeradorService moderadorService;

    public ModeradorController(ModeradorService moderadorService) {
        this.moderadorService = moderadorService;
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE')")
    @PostMapping("/cadastro")
    public ResponseEntity<MensagemResponse> cadastro(@Valid @RequestBody  ModeradorRequest request){
        moderadorService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensagemResponse("Moderador criado com sucesso"));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE')")
    @GetMapping
    public ResponseEntity<List<ModeradorResponse>> listar(){
        return ResponseEntity.ok(moderadorService.listar());
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE')")
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<MensagemResponse> desativarModerador(@PathVariable Long id){
        moderadorService.desativarModerador(id);
        return ResponseEntity.ok(new MensagemResponse("Moderador desativado com sucesso"));
    }

    @PreAuthorize("hasRole('MODERADOR_CHEFE')")
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<MensagemResponse> ativarModerador(@PathVariable Long id){
        moderadorService.ativarModerador(id);
        return ResponseEntity.ok(new MensagemResponse("Moderador ativado com sucesso"));
    }
}
