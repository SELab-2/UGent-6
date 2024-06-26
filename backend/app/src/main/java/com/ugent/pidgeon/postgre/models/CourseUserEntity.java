package com.ugent.pidgeon.postgre.models;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

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
    private String relation;

    public CourseUserEntity() {
    }

    public CourseUserEntity(long courseId, long userId, CourseRelation relation) {
        this.courseId = courseId;
        this.userId = userId;
        this.relation = relation.toString();
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

    public CourseRelation getRelation() {
        return switch (relation) {
            case "creator" -> CourseRelation.creator;
            case "course_admin" -> CourseRelation.course_admin;
            case "enrolled" -> CourseRelation.enrolled;
            default -> null;
        };
    }

    public void setRelation(CourseRelation relation) {
        this.relation = relation.toString();
    }

}

