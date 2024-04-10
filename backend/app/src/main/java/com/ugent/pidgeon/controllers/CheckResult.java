package com.ugent.pidgeon.controllers;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import org.springframework.http.HttpStatus;

public class CheckResult {
    private final HttpStatus status;
    private final String message;

    public CheckResult(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
