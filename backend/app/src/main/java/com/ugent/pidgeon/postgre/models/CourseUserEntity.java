package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

@Entity
@Table(name="course_users")
public class CourseUserEntity {

    @Id
    @Column(name="course_id", nullable=false)
    private long course_id;

    @Id
    @Column(name="user_id", nullable=false)
    private long user_id;

    @Column(name = "course_relation")
    @Enumerated(EnumType.STRING)
    private CourseRelation relation;


    public long getCourse_id() {
        return course_id;
    }


    public void setCourse_id(long course_id) {
        this.course_id = course_id;
    }


    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

}
