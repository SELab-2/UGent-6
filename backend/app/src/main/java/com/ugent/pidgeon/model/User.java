package com.ugent.pidgeon.model;

import java.util.List;

public class User {

    public String name;
    public String firstName;
    public String lastName;
    public String email;
    public List<String> groups;
    public String oid;

    public User (String name, String firstName, String lastName, String email, List<String> groups, String oid) {
        this.name = name;
        this.email = email;
        this.groups = groups;
        this.oid = oid;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}

