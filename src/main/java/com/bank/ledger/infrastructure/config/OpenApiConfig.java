package com.bank.ledger.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig
{
    @Bean
    public OpenAPI customOpenAPI()
    {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Transaction & Ledger API")
                        .version("1.0.0")
                        .description("High-concurrency double-entry accounting ledger microservice powered by Java 21 Virtual Threads.")
                        .contact(new Contact().name("SuyashArya0").email("aryasuyash55@gmail.com")));
    }
}
