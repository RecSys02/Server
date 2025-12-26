package com.tourai.develop.oauth2;

import com.tourai.develop.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService {

    public User login(User user) {
        return user;
    }


}
