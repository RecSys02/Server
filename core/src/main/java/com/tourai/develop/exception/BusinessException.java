package com.tourai.develop.exception;

import com.tourai.develop.exception.enumType.ErrorCode;
import lombok.Getter;

public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

}
