package com.tourai.develop.oauth2;

import com.tourai.develop.domain.entity.User;
import com.tourai.develop.dto.CustomOAuth2User;
import com.tourai.develop.repository.UserRepository;
import com.tourai.develop.service.AuthService;
import com.tourai.develop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthService authService;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("CustomOAuth2UserService.loadUser 들어왔음!");

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        oAuth2Response = setOAuth2Response(oAuth2User, registrationId);

        User user = authService.findOrCreateAndGetUserForOAuth2(oAuth2Response.getProvider(), oAuth2Response.getProviderId(), oAuth2Response.getEmail());
        return new CustomOAuth2User(user);

    }

    private OAuth2Response setOAuth2Response(OAuth2User oAuth2User, String registrationId) {
        OAuth2Response oAuth2Response;
        if (registrationId.equals("naver")) {
            return new NaverOAuth2Response(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            return new GoogleOAuth2Response(oAuth2User.getAttributes());
        }
        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                "Unsupported registrationId: " + registrationId);

    }
}
