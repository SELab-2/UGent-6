package com.ugent.pidgeon.postgre.models;

import java.io.Serializable;

// Hulpklasse zodat de CourseUserRepository correct met meerdere primary keys kan werken
public class CourseUserId implements Serializable {
    private long courseId;
    private long userId;

    public CourseUserId(long courseId, long userId) {
        this.courseId = courseId;
        this.userId = userId;
    }

    public CourseUserId() {
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
