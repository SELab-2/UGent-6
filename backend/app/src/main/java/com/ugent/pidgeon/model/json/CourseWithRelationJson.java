package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;

public class CourseWithRelationJson {
    private Long courseId;
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

    public CourseWithRelationJson(String url, CourseRelation relation, String name, Long courseId) {
        this.url = url;
        this.relation = relation;
        this.name = name;
        this.courseId = courseId;
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

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long id) {
        this.courseId = id;
    }
}
