package com.tourai.develop.oauth2;

import com.tourai.develop.aop.annotation.UserActionLog;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.enumType.Action;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService {

    @UserActionLog(action = Action.LOGIN)
    public User login(User user) {
        return user;
    }


}
