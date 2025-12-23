package com.tourai.develop.config;

import com.tourai.develop.service.PlaceDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PlaceDataSyncService placeDataSyncService;
    @Value("${data.sync.place.filepath}")
    private String placeDataPath;

    @Bean
    @Profile("!test") // 테스트 프로필이 아닐 때만 실행
    public CommandLineRunner initData() {
        return args -> {
            log.info("Starting initial data synchronization...");
            placeDataSyncService.syncFromJson(placeDataPath);
            log.info("Initial data synchronization completed.");
        };
    }
}
