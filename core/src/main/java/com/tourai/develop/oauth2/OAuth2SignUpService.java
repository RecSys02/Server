package com.tourai.develop.oauth2;

import com.tourai.develop.aop.annotation.UserActionLog;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.enumType.Action;
import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2SignUpService {


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    @UserActionLog(action = Action.SIGN_UP)
    public User signUp(String provider, String providerId, String email) {
        String username = provider + "_" + providerId;

        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = bCryptPasswordEncoder.encode(randomPassword);

        User user = User.builder()
                .userName(username)
                .email(email)
                .password(encodedPassword)
                .build();
        return userRepository.save(user);
    }

}
