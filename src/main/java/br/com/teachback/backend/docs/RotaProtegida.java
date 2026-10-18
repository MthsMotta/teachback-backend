package br.com.teachback.backend.docs;

import br.com.teachback.backend.config.WebSecurityConfig;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@SecurityRequirement(name = WebSecurityConfig.SECURITY)
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
        @ApiResponse(responseCode = "403", description = "Papel sem permissão para esta rota")
})
public @interface RotaProtegida {
}
