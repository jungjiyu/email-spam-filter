package com.email.spamfilter.global.base.dto.response;


import com.email.spamfilter.global.exception.ExceptionType;

public class CustomResponseUtil {

    public static AbstractResponseBody<Void> createSuccessResponse() {
        return new SuccessResponseBody<>();
    }

    public static <T> AbstractResponseBody<T> createSuccessResponse(T data) {
        return new SuccessResponseBody<>(data);
    }

    public static AbstractResponseBody<Void> createFailureResponse(ExceptionType exceptionType) {
        return new FailedResponseBody(
                exceptionType.getCode(),
                exceptionType.getMessage()
        );
    }

    public static AbstractResponseBody<Void> createFailureResponse(ExceptionType exceptionType, String customMessage) {
        return new FailedResponseBody(
                exceptionType.getCode(),
                customMessage
        );
    }
}
