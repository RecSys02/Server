package com.tourai.develop.exception.enumType;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용중인 이메일입니다."),
    DUPLICATE_USERNAME(HttpStatus.BAD_REQUEST, "이미 사용중인 닉네임입니다."),
    TAG_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 태그입니다."),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."),
    PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이어야 합니다."),
    PASSWORD_MUST_CONTAIN_ALPHA_NUMERIC(HttpStatus.BAD_REQUEST, "비밀번호는 영문과 숫자를 모두 포함해야 합니다."),


    REFRESH_TOKEN_NULL(HttpStatus.UNAUTHORIZED, "Refresh Token이 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었습니다."),
    REFRESH_TOKEN_TYPE_INVALID(HttpStatus.UNAUTHORIZED, "Refresh Token 타입이 옳바르지 않습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "Refresh Token이 일치하지 않습니다."),

    AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_INVALID_REQUEST(HttpStatus.UNAUTHORIZED, "로그인 요청 형식이 올바르지 않습니다."),
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    AUTH_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다."),
    AUTH_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 로그인 정보입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }


}
