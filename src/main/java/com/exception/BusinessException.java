package com.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String message) {
        this("BUSINESS", message);
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}