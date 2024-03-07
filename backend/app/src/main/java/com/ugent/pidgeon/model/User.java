package com.ugent.pidgeon.model;

import java.util.List;

public class User {

    public String name;
    public String firstName;
    public String lastName;
    public String email;
    public String oid;

    public User (String name, String firstName, String lastName, String email, String oid) {
        this.name = name;
        this.email = email;
        this.oid = oid;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}