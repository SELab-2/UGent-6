package com.ugent.pidgeon.json;

public class UserReferenceJson {
    private String name;
    private String email;
    private Long userId;
    private String studentNumber;

    public UserReferenceJson(String name, String email, Long userId, String studentNumber) {
        this.name = name;
        this.email = email;
        this.userId = userId;
        this.studentNumber = studentNumber;
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


    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
}
