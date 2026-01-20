package com.example.chatserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.beans.JavaBean;

@Configuration
public class WebClientConfig {
    // Core 서버 호출용

    @Bean
    public WebClient coreWebClient(@Value("${core.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
