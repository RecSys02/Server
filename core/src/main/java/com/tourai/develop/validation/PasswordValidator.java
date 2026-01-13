package com.tourai.develop.validation;

import com.tourai.develop.exception.BusinessException;
import com.tourai.develop.exception.enumType.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

    public void validatePassword(String password) {

        if (password == null || password.length() < 8) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        boolean isHasLetter = password.chars().anyMatch(Character::isLetter);
        boolean isHasDigit = password.chars().anyMatch(Character::isDigit);

        if (!isHasLetter || !isHasDigit) {
            throw new BusinessException(ErrorCode.PASSWORD_MUST_CONTAIN_ALPHA_NUMERIC);
        }

    }
}
