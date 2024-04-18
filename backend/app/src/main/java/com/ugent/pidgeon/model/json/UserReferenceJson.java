package com.ugent.pidgeon.model.json;

public class UserReferenceJson {
    private String name;
    private String email;
    private Long userId;

    public UserReferenceJson(String name, String email, Long userId) {
        this.name = name;
        this.email = email;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserReferenceJson() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
