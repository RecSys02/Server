package com.tourai.develop.oauth2;

import com.tourai.develop.domain.entity.User;
import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2AccountService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public User findOrCreateAndGetUserForOAuth2(String provider, String providerId, String email) {

        Optional<User> optionalFindUser = findByProvider(provider, providerId);

        if (optionalFindUser.isPresent()) {
            //기존 OAuth2 회원일 경우
            return optionalFindUser.get();
        }
        //새롭게 OAuth2 가입하는 경우
        return createOAuth2User(provider, providerId, email);

    }

    private Optional<User> findByProvider(String provider, String providerId) {
        String username = buildUsername(provider, providerId);
        return userRepository.findByUserName(username);
    }

    private User createOAuth2User(String provider, String providerId, String email) {
        String username = buildUsername(provider, providerId);

        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = bCryptPasswordEncoder.encode(randomPassword);

        User user = User.builder()
                .userName(username)
                .email(email)
                .password(encodedPassword)
                .build();

        return userRepository.save(user);
    }

    private String buildUsername(String provider, String providerId) {
        return provider + "_" + providerId;
    }
}
