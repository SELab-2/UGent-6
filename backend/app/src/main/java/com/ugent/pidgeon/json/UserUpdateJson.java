package com.ugent.pidgeon.json;

import com.ugent.pidgeon.postgre.models.types.UserRole;

public class UserUpdateJson {
    private String name;
    private String surname;
    private String email;
    private String role;




    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserUpdateJson() {
    }

    public UserUpdateJson(String name, String surname, String password, String role) {
        this.name = name;
        this.surname = surname;
        this.email = password;
        this.role = role;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UserRole getRoleAsEnum() {
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
