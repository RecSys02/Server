package com.tourai.develop.oauth2;

import com.tourai.develop.domain.entity.User;
import com.tourai.develop.repository.UserRepository;
import com.tourai.develop.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName(); // 보통 username(email)이 들어옴
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isPresent()) {
                authService.onLogoutSuccess(userOptional.get());
            } else {
                log.warn("Logout successful but user not found in DB for logging: {}", email);
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
