package com.ugent.selab2.model;

import java.util.List;

public class User {

    public String name;
    public String email;
    public List<String> groups;
    public String oid;

    public User (String name, String email, List<String> groups, String oid) {
        this.name = name;
        this.email = email;
        this.groups = groups;
        this.oid = oid;
    }
}
