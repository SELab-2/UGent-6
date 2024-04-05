package com.ugent.pidgeon.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Permission {

    private boolean permission;
    private String errorMessage;


    public Permission(boolean permission, String errorMessage) {
        this.permission = permission;
        this.errorMessage = errorMessage;
    }


    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean hasPermission() {
        return permission;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ResponseEntity<String> getResponseEntity() {
        if (permission) {
            return null;
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage);
        }
    }
}
