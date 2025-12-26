package com.tourai.develop.exception;

import com.tourai.develop.exception.enumType.ErrorCode;

public class AuthException extends BaseException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
