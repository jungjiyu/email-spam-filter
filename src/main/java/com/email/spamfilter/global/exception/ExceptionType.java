package com.email.spamfilter.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

/**
 * 정의 형식 : ENUM명( HTTPStatus, 문자열타입의custom에러코드, 문자열타입의custom에러메시지)
 * 활용 형식 : ExceptionType.ENUM명
 */
@Getter
@AllArgsConstructor
public enum ExceptionType {

    // common
    UNEXPECTED_SERVER_ERROR(INTERNAL_SERVER_ERROR, "C001", "예상치못한 서버에러 발생"),
    BINDING_ERROR(BAD_REQUEST, "C002", "바인딩시 에러 발생"),
    ESSENTIAL_FIELD_MISSING_ERROR(NO_CONTENT , "C003","필수적인 필드 부재"),
    INVALID_VALUE_ERROR(NOT_ACCEPTABLE , "C004","값이 유효하지 않음"),
    DUPLICATE_VALUE_ERROR(NOT_ACCEPTABLE , "C005","값이 중복됨"),


    // security
    ACCESSTOKEN_EXTRACT_FAIL_ERROR(NOT_ACCEPTABLE , "s001","access token 추출 실패"),
    EMAIL_EXTRACT_FAIL_ERROR(NOT_ACCEPTABLE , "s002","email 추출 실패"),


    // user
    USER_NOT_FOUND(NOT_FOUND, "U001", "존재하지 않는 사용자"),
    DUPLICATED_PROFILE_ID(CONFLICT, "U002", "중복 프로필 아이디"),
    DUPLICATED_EMAIL(CONFLICT, "U003", "중복 이메일"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "U004", "권한이 없음"),
    UN_AUTHENTICATION(UNAUTHORIZED, "U005", "인증이 필요함"),
    ALREADY_FOLLOW(BAD_REQUEST, "U006", "이미 팔로우한 사용자입니다."),
    SELF_FOLLOW(BAD_REQUEST, "U007", "자기 자신은 팔로우할 수 없습니다."),
    FOLLOW_NOT_FOUND(BAD_REQUEST, "U007", "팔로우를 하지 않은 사용자입니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;

}
