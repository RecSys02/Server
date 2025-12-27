package com.tourai.develop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourai.develop.domain.entity.Plan;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.entity.UserLog;
import com.tourai.develop.domain.enumType.Action;
import com.tourai.develop.dto.LogPayload;
import com.tourai.develop.dto.PlaceItem;
import com.tourai.develop.repository.UserLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserLogService {

    private final UserLogRepository userLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(User user, Action action, Object payload) {
        Map<String, Object> metadata = new HashMap<>();

        // 1. 공통 필드 (Request 정보 등)
        addCommonMetadata(metadata);

        // 2. Payload 변환 및 병합
        if (payload != null) {
            Map<String, Object> payloadMap = objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});
            metadata.putAll(payloadMap);
        }

        // 3. 저장
        UserLog userLog = UserLog.builder()
                .user(user)
                .action(action)
                .metadata(metadata)
                .build();

        userLogRepository.save(userLog);
    }

    private void addCommonMetadata(Map<String, Object> metadata) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                metadata.put("client_ip", getClientIp(request));
                metadata.put("user_agent", request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // RequestContext가 없는 경우 (비동기 처리 등) 무시하거나 기본값 설정
            metadata.put("client_ip", "unknown");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    // --- 편의 메서드들 ---
    @Async
    public void logSignUp(User user, String provider) {
        LogPayload.SignUp payload = LogPayload.SignUp.builder()
                .provider(provider)
                .email(user.getEmail())
                // TODO: 클라이언트에서 OS, AppVersion 등을 헤더로 받으면 추가
                .build();
        logAction(user, Action.SIGN_UP, payload);
    }

    @Async
    public void logLogin(User user, String provider, boolean isSuccess, String failReason) {
        LogPayload.Login payload = LogPayload.Login.builder()
                .provider(provider)
                .isSuccess(isSuccess)
                .failReason(failReason)
                .build();
        logAction(user, Action.LOGIN, payload);
    }

    @Async
    public void logLogout(User user) {
        // 로그아웃은 특별한 페이로드가 없을 수 있음. 필요시 추가
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "User logged out");
        logAction(user, Action.LOGOUT, payload);
    }

    @Async
    public void logCreatePlan(User user, Plan plan) {
        List<LogPayload.CreatePlan.PlaceInfoLog> placeLogs = new ArrayList<>();
        int placeCount = 0;

        if (plan.getSchedule() != null) {
            for (Map.Entry<String, List<PlaceItem>> entry : plan.getSchedule().entrySet()) {
                int day = Integer.parseInt(entry.getKey());
                List<PlaceItem> items = entry.getValue();
                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        PlaceItem item = items.get(i);
                        placeLogs.add(LogPayload.CreatePlan.PlaceInfoLog.builder()
                                .placeId(item.getPlaceId())
                                .name(item.getPlaceName())
                                .category(null) // PlaceItem에 category가 없음
                                .day(day)
                                .order(i + 1)
                                .build());
                        placeCount++;
                    }
                }
            }
        }

        LogPayload.CreatePlan payload = LogPayload.CreatePlan.builder()
                .planId(plan.getId())
                .title(plan.getName())
                .isPrivate(plan.getIsPrivate())
                .totalDays(plan.getSchedule() != null ? plan.getSchedule().size() : 0)
                .placeCount(placeCount)
                .places(placeLogs)
                .build();

        logAction(user, Action.CREATE_PLAN, payload);
    }

    @Async
    public void logDeletePlan(User user, Plan plan) {
        int placeCount = 0;
        if (plan.getSchedule() != null) {
            for (List<PlaceItem> items : plan.getSchedule().values()) {
                if (items != null) placeCount += items.size();
            }
        }

        LogPayload.DeletePlan payload = LogPayload.DeletePlan.builder()
                .deletedPlanId(plan.getId())
                .deletedPlanTitle(plan.getName())
                .createdAt(plan.getCreatedAt())
                .placeCount(placeCount)
                .build();

        logAction(user, Action.DELETE_PLAN, payload);
    }

    @Async
    public void logLikePlan(User user, Plan targetPlan) {
        LogPayload.LikePlan payload = LogPayload.LikePlan.builder()
                .targetPlanId(targetPlan.getId())
                .targetPlanOwnerId(targetPlan.getUser().getId())
                .targetPlanTitle(targetPlan.getName())
                .build();
        
        logAction(user, Action.LIKE_PLAN, payload);
    }

    @Async
    public void logUnlikePlan(User user, Plan targetPlan) {
        LogPayload.UnlikePlan payload = LogPayload.UnlikePlan.builder()
                .targetPlanId(targetPlan.getId())
                .targetPlanOwnerId(targetPlan.getUser().getId())
                .targetPlanTitle(targetPlan.getName())
                .build();

        logAction(user, Action.UNLIKE_PLAN, payload);
    }
}
