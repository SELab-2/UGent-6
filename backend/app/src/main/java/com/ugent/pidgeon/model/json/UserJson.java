package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.controllers.ApiRoutes;

import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserJson {

    private long id;
    private String name;
    private String surname;
    private String email;
    private UserRole role;

    private Timestamp createdAt;

//    private List<CourseWithRelationJson> courses;

    public UserJson() {
    }

    public UserJson(UserEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.surname = entity.getSurname();
        this.email = entity.getEmail();
        this.role = entity.getRole();
        this.createdAt = entity.getCreatedAt();
//        this.courses = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUrl() {
        return ApiRoutes.USER_BASE_PATH + "/" + id;
    }

    public void setUrl(String s){}

    public String getCourseUrl() {
        return ApiRoutes.USER_BASE_PATH + "/" + id+"/courses";
    }
    public void setCourseUrl(String s){}

    public String getProjectUrl() {
        return ApiRoutes.PROJECT_BASE_PATH;
    }
    public void setProjectUrl(String s){}

}
