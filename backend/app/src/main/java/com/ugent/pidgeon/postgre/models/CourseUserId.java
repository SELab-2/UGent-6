package com.ugent.pidgeon.postgre.models;

import java.io.Serializable;

public class CourseUserId implements Serializable {

    private long courseId;
    private long userId;

    public CourseUserId(long courseId, long userId) {
        this.courseId = courseId;
        this.userId = userId;
    }
}
