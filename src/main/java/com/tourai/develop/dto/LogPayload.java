package com.tourai.develop.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class LogPayload {

    @Builder
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SignUp {
        private String provider;
        private String email;
        private String osType;
        private String appVersion;
    }

    @Builder
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Login {
        private String provider;
        private String clientIp;
        private String deviceModel;
        private String osVersion;
        private boolean isSuccess;
        private String failReason;
    }

    @Builder
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Logout {
        private LocalDateTime loginAt;
        private Long durationSec;
        private String trigger;
    }

    @Builder
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CreatePlan {
        private Long planId;
        private String title;
        private String region;
        private boolean isPrivate;
        private int totalDays;
        private int placeCount;
        private List<PlaceInfoLog> places;

        @Builder
        @Getter
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PlaceInfoLog {
            private Long placeId;
            private String name;
            private String category;
            private int day;
            private int order;
        }
    }

    @Builder
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class LikePlan {
        private Long targetPlanId;
        private Long targetPlanOwnerId;
        private String targetPlanTitle;
    }

    @Builder
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UnlikePlan {
        private Long targetPlanId;
        private Long targetPlanOwnerId;
        private String targetPlanTitle;
    }

    @Builder
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DeletePlan {
        private Long deletedPlanId;
        private String deletedPlanTitle;
        private LocalDateTime createdAt;
        private int placeCount;
    }
}
