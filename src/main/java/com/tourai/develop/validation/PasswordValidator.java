package com.tourai.develop.validation;

public class PasswordValidator {

    public void validatePassword(String password) {

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어 합니다!");
        }

        boolean isHasLetter = password.chars().anyMatch(Character::isLetter);
        boolean isHasDigit = password.chars().anyMatch(Character::isDigit);

        if (!isHasLetter || !isHasDigit) {
            throw new IllegalArgumentException("비밀번호는 영문과 숫자를 모두 포함해야 합니다!");
        }
    }
}
