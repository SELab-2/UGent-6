package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="course_users")
public class CourseUserEntity {
    private long course_id;
    private long user_id;

    @Id
    @Column(name="course_id", nullable=false)
    public long getCourse_id() {
        return course_id;
    }

    public void setCourse_id(long course_id) {
        this.course_id = course_id;
    }

    @Id
    @Column(name="user_id", nullable=false)
    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }
}
