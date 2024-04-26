package com.ugent.pidgeon;

import java.time.OffsetDateTime;

public record ApiErrorReponse(OffsetDateTime timestamp, int status, String error, String message, String path) {
}
