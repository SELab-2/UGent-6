package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@IdClass(CourseUserId.class)
@Table(name="course_users")
public class CourseUserEntity {

    @Id
    @Column(name="course_id", nullable=false)
    private long courseId;

    @Id
    @Column(name="user_id", nullable=false)
    private long userId;

    @Column(name = "course_relation")
    @Enumerated(EnumType.STRING)
    private CourseRelation relation;


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

class CourseUserId implements Serializable {
    private long courseId;
    private long userId;
}