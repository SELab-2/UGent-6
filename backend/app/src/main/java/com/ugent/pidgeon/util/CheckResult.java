package com.ugent.pidgeon.util;

import org.springframework.http.HttpStatus;

/**
 * Class to return a result of a check
 * @param <T> type of the data that is returned
 */
public class CheckResult<T> {
    private final HttpStatus status;
    private final String message;
    private final T data;

    public CheckResult(HttpStatus status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
