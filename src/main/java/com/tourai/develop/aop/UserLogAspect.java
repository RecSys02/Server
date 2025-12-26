package com.tourai.develop.aop;

import com.tourai.develop.aop.annotation.UserActionLog;
import com.tourai.develop.domain.entity.Plan;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.enumType.Action;
import com.tourai.develop.repository.UserRepository;
import com.tourai.develop.service.UserLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UserLogAspect {

    private final UserLogService userLogService;
    private final UserRepository userRepository;

    @AfterReturning(pointcut = "@annotation(userActionLog)", returning = "result")
    public void logUserAction(JoinPoint joinPoint, UserActionLog userActionLog, Object result) {
        try {
            Action action = userActionLog.action();
            User user = null;

            // SIGN_UP, LOGIN은 SecurityContext에 아직 인증 정보가 없거나(로그인 전),
            // 막 생성된 User 객체를 다뤄야 하므로 예외 처리
            if (action == Action.SIGN_UP || action == Action.LOGIN) {
                // 이 경우엔 리턴값이나 파라미터에서 User를 찾아야 함 (기존 방식 유지 필요할 수도 있음)
                // 하지만 요청하신 대로 SecurityContext 위주로 가되, 이 두 케이스만 예외적으로 처리
                if (result instanceof User) {
                    user = (User) result;
                } else {
                    // 로그인/회원가입 시에는 보통 User 객체나 관련 정보를 리턴함.
                    // 만약 아니라면 파라미터에서 찾아야 할 수도 있음.
                    // 일단 여기서는 SecurityContext 조회를 시도하지 않음 (로그인 전일 수 있으므로)
                }
            } else {
                // 그 외(CREATE_PLAN 등)는 이미 로그인된 상태이므로 SecurityContext에서 조회
                user = getCurrentUser();
            }

            if (user == null) {
                log.warn("UserLogAspect: User not found. Action: {}", action);
                return;
            }

            // 2. Action에 따라 적절한 로깅 메서드 호출
            switch (action) {
                case CREATE_PLAN:
                    if (result instanceof Plan) {
                        userLogService.logCreatePlan(user, (Plan) result);
                    }
                    break;
                
                case DELETE_PLAN:
                    // DELETE는 삭제된 Plan 객체를 리턴받아야 로그를 남길 수 있음
                    if (result instanceof Plan) {
                        userLogService.logDeletePlan(user, (Plan) result);
                    } else {
                        log.warn("UserLogAspect: DELETE_PLAN requires returning the deleted Plan object.");
                    }
                    break;

                case LIKE_PLAN:
                    if (result instanceof Plan) {
                        userLogService.logLikePlan(user, (Plan) result);
                    }
                    break;

                case UNLIKE_PLAN:
                    if (result instanceof Plan) {
                        userLogService.logUnlikePlan(user, (Plan) result);
                    }
                    break;

                case SIGN_UP:
                    // 회원가입 시 provider 정보는 파라미터 등에서 가져와야 함
                    String provider = findArg(joinPoint.getArgs(), String.class);
                    userLogService.logSignUp(user, provider != null ? provider : "UNKNOWN");
                    break;
                
                case LOGIN:
                    String loginProvider = findArg(joinPoint.getArgs(), String.class);
                    userLogService.logLogin(user, loginProvider != null ? loginProvider : "UNKNOWN", true, null);
                    break;

                default:
                    log.warn("UserLogAspect: Unsupported action type: {}", action);
            }

        } catch (Exception e) {
            log.error("UserLogAspect: Failed to log user action", e);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        
        // Principal이 String(email)인지, UserDetails인지에 따라 처리
        String email = null;
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        }
        
        if (email != null) {
            return userRepository.findByEmail(email).orElse(null);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T findArg(Object[] args, Class<T> clazz) {
        for (Object arg : args) {
            if (clazz.isInstance(arg)) {
                return (T) arg;
            }
        }
        return null;
    }
}
