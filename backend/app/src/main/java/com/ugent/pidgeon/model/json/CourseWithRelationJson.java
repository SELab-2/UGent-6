package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;

public class CourseWithRelationJson {
    private Long id;
    private String url;
    private String name;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public CourseWithRelationJson() {
    }

    public CourseWithRelationJson(String url, CourseRelation relation, String name, Long id) {
        this.url = url;
        this.relation = relation;
        this.name = name;
        this.id = id;
    }

    private CourseRelation relation;

    public CourseRelation getRelation() {
        return relation;
    }

    public void setRelation(CourseRelation relation) {
        this.relation = relation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
