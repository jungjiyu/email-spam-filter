package com.email.spamfilter.global.exception;

import lombok.Getter;

/**
 * 쉬운 custom Exception throw 를 위한 클래스
 * thorw 형식 : throw new BusinessException( ExceptionType.ENUM명 )
 */
@Getter
public class BusinessException extends RuntimeException{

    private final ExceptionType exceptionType;

    public BusinessException(ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }
}
