package com.email.spamfilter.global.base.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 프론트단으로의 정형화된 response 를 위한 클래스
 * 활용 형식 : 컨트롤러단에서 아래와 같이 return 타입 설정 및 return
 *     @XXXXMapping("/어쩌구")
 *     public ResponseEntity<ResponseBody<특정객체타입>> 메서드명( ... ) {
 *         ... 특정 객체 구하는 로직 ...
 *         return ResponseEntity.status(HttpStatus.상태코드).body(ResponseUtil.createSuccessResponse(특정객체));
 *     }
 *
 **/
@Setter
@Getter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "success")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SuccessResponseBody.class, name = "true"),
        @JsonSubTypes.Type(value = FailedResponseBody.class, name = "false")
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public sealed abstract class AbstractResponseBody<T> permits SuccessResponseBody, FailedResponseBody {
    private String code;
}
