package com.tourai.develop.config;

import com.tourai.develop.service.PlaceDataSyncService;
import com.tourai.develop.service.TagDataSyncService;
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
    private final TagDataSyncService tagDataSyncService;

    @Value("${data.sync.place.filepath}")
    private String placeDataPath;

    @Value("${data.sync.tag.filepath}")
    private String tagDataPath;

    @Bean
    @Profile("!test") // 테스트 프로필이 아닐 때만 실행
    public CommandLineRunner initData() {
        return args -> {
            log.info("Starting initial data synchronization...");
            placeDataSyncService.syncFromJson(placeDataPath);
            tagDataSyncService.syncTagsFromJson(tagDataPath);
            log.info("Initial data synchronization completed.");
        };
    }
}
