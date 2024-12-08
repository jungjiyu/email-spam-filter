package com.email.spamfilter.global.base.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

@Getter
@JsonTypeName("false")
public final class FailedResponseBody extends AbstractResponseBody<Void> {

    private final String msg;

    public FailedResponseBody(String code, String msg) {
        this.setCode(code);
        this.msg = msg;
    }
}
