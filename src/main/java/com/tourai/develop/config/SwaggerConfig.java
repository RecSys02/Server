package com.tourai.develop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo());
        // 추후 JWT 도입 시 이곳에 .components()와 .addSecurity()를 체이닝하면 됩니다.
    }

    private Info apiInfo() {
        return new Info()
                .title("Tourai API Documentation")
                .description("여행 계획 및 추천 서비스 Tourai의 개발용 API 명세서입니다.")
                .version("0.0.1");  // 나중에 환경변수로 버전관리
    }
}