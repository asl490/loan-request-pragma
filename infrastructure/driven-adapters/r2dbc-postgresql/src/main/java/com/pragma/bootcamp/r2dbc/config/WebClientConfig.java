package com.pragma.bootcamp.r2dbc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient clientWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080") // O la URL del servicio real
                .build();
    }
}