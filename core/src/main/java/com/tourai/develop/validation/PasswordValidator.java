package com.tourai.develop.validation;

import com.tourai.develop.exception.BusinessException;
import com.tourai.develop.exception.enumType.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    // 프론트: /^(?=.*[a-z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$");

    public void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException(ErrorCode.PASSWORD_INVALID_FORMAT);
        }
    }
}
