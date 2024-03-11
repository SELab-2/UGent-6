package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;

public class CourseWithRelationJson {
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

    public CourseWithRelationJson(String url, CourseRelation relation, String name) {
        this.url = url;
        this.relation = relation;
        this.name = name;
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
}
