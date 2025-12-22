package com.tourai.develop.config;

import com.tourai.develop.jwt.*;
import com.tourai.develop.oauth2.CustomOAuth2UserService;
import com.tourai.develop.oauth2.CustomSuccessHandler;
import com.tourai.develop.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final RedisLogoutHandler redisLogoutHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           AuthenticationManager authenticationManager,
                                           CustomUserDetailsService customUserDetailsService,
                                           CustomSuccessHandler customSuccessHandler) throws Exception {

        httpSecurity
                .csrf((auth) -> auth.disable());

        httpSecurity
                .formLogin((auth) -> auth.disable());

        httpSecurity
                .httpBasic((auth) -> auth.disable());

        httpSecurity
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/", "/join",
                                "/auth/reissue", "/oauth2/**",
                                "/login/oauth2/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated());


        LoginFilter loginFilter = new LoginFilter(authenticationManager, jwtUtil, refreshTokenService);
        loginFilter.setPostOnly(true);
        loginFilter.setFilterProcessesUrl("/login");

        httpSecurity
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);


        httpSecurity
                .addFilterBefore(new JwtFilter(jwtUtil, customUserDetailsService), LoginFilter.class);


        //oauth2
        httpSecurity
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService)))
                        .successHandler(customSuccessHandler));


        httpSecurity
                .sessionManagement((session)
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity.logout((logout) -> logout
                .logoutUrl("/logout")
                .addLogoutHandler(redisLogoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                })
        );

        return httpSecurity.build();
    }


}
