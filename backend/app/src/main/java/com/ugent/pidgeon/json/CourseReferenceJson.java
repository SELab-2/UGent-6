package com.ugent.pidgeon.json;

import java.time.OffsetDateTime;

public class CourseReferenceJson {
    private String name;
    private String url;
    private Long courseId;
    private OffsetDateTime archivedAt;

    public CourseReferenceJson(String name, String url, Long courseId,
        OffsetDateTime archived) {
        this.name = name;
        this.url = url;
        this.courseId = courseId;
        this.archivedAt = archived;
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

    public OffsetDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(OffsetDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }
}
