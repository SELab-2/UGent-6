package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.repository.GroupRepository;

public class UserReferenceJson {
    private String name;
    private String email;
    private Long id;

    public UserReferenceJson(String name, String email, Long id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
