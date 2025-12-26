package com.tourai.develop.exception.handler;

import com.tourai.develop.exception.AuthException;
import com.tourai.develop.exception.BusinessException;
import com.tourai.develop.exception.enumType.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(Map.of(
                                "errorCode", errorCode.name(),
                                "message", errorCode.getMessage(),
                                "type", "BUSINESS"
                        )
                );
    }


    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> handleAuthException(AuthException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(Map.of(
                                "errorCode", errorCode.name(),
                                "message", errorCode.getMessage(),
                                "type", "AUTH"
                        )
                );
    }

}
