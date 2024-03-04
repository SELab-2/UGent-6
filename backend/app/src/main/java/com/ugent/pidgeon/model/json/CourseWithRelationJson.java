package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;

public class CourseWithRelationJson {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public CourseWithRelationJson() {
    }

    public CourseWithRelationJson(String url, CourseRelation relation) {
        this.url = url;
        this.relation = relation;
    }

    private CourseRelation relation;

    public CourseRelation getRelation() {
        return relation;
    }

    public void setRelation(CourseRelation relation) {
        this.relation = relation;
    }
}
