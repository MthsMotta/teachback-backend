package br.com.teachback.backend.controller;

import br.com.teachback.backend.dto.request.ModeradorChefeRequest;
import br.com.teachback.backend.dto.response.MensagemResponse;
import br.com.teachback.backend.service.ModeradorChefeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/moderadores-chefe")
public class ModeradorChefeController {

    private final ModeradorChefeService moderadorChefeService;

    public ModeradorChefeController(ModeradorChefeService moderadorChefeService) {
        this.moderadorChefeService = moderadorChefeService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastro")
    public ResponseEntity<MensagemResponse> cadastro(@Valid @RequestBody ModeradorChefeRequest request) {
        moderadorChefeService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensagemResponse("Moderador chefe criado com sucesso"));
    }
}
