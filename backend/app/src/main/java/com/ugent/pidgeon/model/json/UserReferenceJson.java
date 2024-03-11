package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.repository.GroupRepository;

public class UserReferenceJson {
    private String name;
    private String url;

    public UserReferenceJson() {
    }

    public UserReferenceJson(String name, String url) {
        this.name = name;
        this.url = url;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
