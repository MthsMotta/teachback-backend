package br.com.teachback.backend.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer semCorpoNosErros() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation ->
                        operation.getResponses().forEach((codigo, resposta) -> {
                            boolean erro = codigo.startsWith("4") || codigo.startsWith("5");
                            boolean paginaHtml = resposta.getContent() != null
                                    && resposta.getContent().containsKey("text/html");
                            if (erro && !paginaHtml) {
                                resposta.setContent(null);
                            }
                        })));
    }
}
