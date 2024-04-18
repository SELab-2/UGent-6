package com.ugent.pidgeon.model.json;

public class CourseReferenceJson {
    private String name;
    private String url;
    private Long courseId;

    public CourseReferenceJson(String name, String url, Long courseId) {
        this.name = name;
        this.url = url;
        this.courseId = courseId;
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

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
