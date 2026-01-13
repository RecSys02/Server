package com.tourai.develop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class PromptConfig {

    @Value("classpath:prompts/plan_instructor.txt")
    private Resource planInstructorResource;

    @Bean
    public String planInstructor() {
        try {
            return StreamUtils.copyToString(planInstructorResource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load plan instructor prompt", e);
        }
    }
}
