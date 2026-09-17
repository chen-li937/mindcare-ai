package com.example.aispringboot.exception;

import lombok.Getter;

//业务异常类
@Getter
public class BusinessException extends RuntimeException  {

    private final String code;
    private final String message;
    private final String data;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.message = message;
        this.data = null;
    }
}
