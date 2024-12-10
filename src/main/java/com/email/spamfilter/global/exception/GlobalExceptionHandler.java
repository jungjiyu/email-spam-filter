package com.email.spamfilter.global.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.email.spamfilter.global.base.dto.response.AbstractResponseBody;
import com.email.spamfilter.global.base.dto.response.CustomResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 전역적으로 발생하는 에러에 대한 처리
 * 위 -> 아래 방향으로 순차적 검문
 */
@RequiredArgsConstructor
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<AbstractResponseBody<Void>> businessException(BusinessException e) {
        ExceptionType exceptionType = e.getExceptionType();
        return ResponseEntity.status(exceptionType.getStatus())
                .body(CustomResponseUtil.createFailureResponse(exceptionType));
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<AbstractResponseBody<Void>> handleJWTVerificationException(JWTVerificationException ex) {
        log.error("JWTVerificationException 발생 (유효하지 않은 토큰입니다) : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CustomResponseUtil.createFailureResponse(ExceptionType.ACCESSTOKEN_INVALID));
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AbstractResponseBody<Void>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        String customMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(ExceptionType.BINDING_ERROR.getStatus())
                .body(CustomResponseUtil.createFailureResponse(ExceptionType.BINDING_ERROR, customMessage));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<AbstractResponseBody<Void>> dataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(CustomResponseUtil.createFailureResponse(ExceptionType.DUPLICATE_VALUE_ERROR));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<AbstractResponseBody<Void>> exception(Exception e) {
        log.error("Exception Message : {} ", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomResponseUtil.createFailureResponse(ExceptionType.UNEXPECTED_SERVER_ERROR));
    }
}
