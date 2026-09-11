package br.com.teachback.backend.controller;

import br.com.teachback.backend.dto.request.CadastroRequest;
import br.com.teachback.backend.dto.request.LoginRequest;
import br.com.teachback.backend.dto.response.LoginResponse;
import br.com.teachback.backend.dto.response.MensagemResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.TokenExpiradoException;
import br.com.teachback.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<MensagemResponse> cadastro(@RequestBody @Valid CadastroRequest cadastroRequest) {
        authService.cadastrar(cadastroRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensagemResponse("Cadastro realizado, verifique seu e-mail"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/confirmar")
    public ResponseEntity<String> confirmarEmail(@RequestParam String token){

        try {
            authService.confirmarEmail(token);

            String htmlSucesso = "<html><body><h1>E-mail confirmado com sucesso!</h1>"
                    + "<p>Você já pode fazer login no TeachBack.</p>"
                    + "</body></html>";

            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlSucesso);
        } catch (RecursoNaoEncontradoException e) {
            String htmlErro = "<html><body>"
                    + "<h1>Link inválido</h1>"
                    + "<p>" + e.getMessage() + "</p>"
                    + "</body></html>";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_HTML).body(htmlErro);

        } catch (TokenExpiradoException e) {
            String htmlErro = "<html><body>"
                    + "<h1>Link expirado</h1>"
                    + "<p>" + e.getMessage() + "</p>"
                    + "</body></html>";
            return ResponseEntity.status(HttpStatus.GONE).contentType(MediaType.TEXT_HTML).body(htmlErro);
        }
    }
}
