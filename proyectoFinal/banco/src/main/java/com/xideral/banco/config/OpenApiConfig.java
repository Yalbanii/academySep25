package com.xideral.banco.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger para la documentación de la API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema Bancario Digital API")
                        .version("1.0.0")
                        .description("API REST para el Sistema Bancario Digital")
                        .contact(new Contact()
                                .name("Xideral")
                                .email("support@xideral.com")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("com.xideral.banco.account.controller", "com.xideral.banco.customer.controller")
                .build();
    }
}
