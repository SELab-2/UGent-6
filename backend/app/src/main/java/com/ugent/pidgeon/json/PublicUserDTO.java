package com.ugent.pidgeon.json;

public class PublicUserDTO {
    private String firstName;
    private String surName;
    private String email;

    public PublicUserDTO(String firstName, String surName, String email) {
        this.firstName = firstName;
        this.surName = surName;
        this.email = email;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}