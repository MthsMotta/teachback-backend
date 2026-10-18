package br.com.teachback.backend.controller;

import br.com.teachback.backend.dto.request.CadastroRequest;
import br.com.teachback.backend.dto.request.EmailRequest;
import br.com.teachback.backend.dto.request.LoginRequest;
import br.com.teachback.backend.dto.response.LoginResponse;
import br.com.teachback.backend.dto.response.MensagemResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.TokenExpiradoException;
import br.com.teachback.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Cadastro, confirmação de e-mail e login. Todas as rotas são públicas.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastra aluno ou professor",
            description = "Cria a conta com status pendente e envia um e-mail com o link de confirmação (válido por 15 minutos).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cadastro realizado, e-mail de confirmação enviado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Faculdade não encontrada")
    })
    public ResponseEntity<MensagemResponse> cadastro(@RequestBody @Valid CadastroRequest cadastroRequest) {
        authService.cadastrar(cadastroRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensagemResponse("Cadastro realizado, verifique seu e-mail"));
    }

    @PostMapping("/login")
    @Operation(summary = "Realiza o login",
            description = "Devolve o token JWT (validade de 4 horas), o nome e o papel do usuário. "
                    + "Após 5 tentativas com senha errada para o mesmo e-mail, o login fica bloqueado por 20 minutos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos"),
            @ApiResponse(responseCode = "403", description = "Conta ainda não liberada para acesso, ou suspensa"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de login")
    })
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reenviar-confirmacao")
    @Operation(summary = "Reenvia o e-mail de confirmação",
            description = "A resposta é sempre a mesma, exista ou não uma conta pendente com esse e-mail, para não revelar quais e-mails estão cadastrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitação recebida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "E-mail inválido")
    })
    public ResponseEntity<MensagemResponse> reenviarConfirmacao(@Valid @RequestBody EmailRequest request) {
        authService.reenviarConfirmacao(request.email());
        return ResponseEntity.ok(new MensagemResponse("Se o e-mail informado estiver pendente de confirmação, um novo link foi enviado."));
    }

    @GetMapping("/confirmar")
    @Operation(summary = "Confirma o e-mail",
            description = "Rota aberta pelo link do e-mail. Devolve uma página HTML, não JSON. "
                    + "Aluno passa a ficar ativo, professor fica aguardando aprovação de um moderador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "E-mail confirmado (página HTML)",
                    content = @Content(mediaType = "text/html", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Link inválido (página HTML)",
                    content = @Content(mediaType = "text/html", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "410", description = "Link expirado (página HTML)",
                    content = @Content(mediaType = "text/html", schema = @Schema(type = "string")))
    })
    public ResponseEntity<String> confirmarEmail(@Parameter(description = "Token recebido no link do e-mail de confirmação") @RequestParam String token){

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
