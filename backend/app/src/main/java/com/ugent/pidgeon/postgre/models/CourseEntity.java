package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "courses")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id", nullable = false)
    private long id;
    @Column(name = "course_name", nullable=false)
    private String name;
    @Column(name = "description", nullable=false)
    private String description;

    @Column(name = "course_year", nullable = true)
    private int courseYear;


    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    public String getJoinKey() {
        return joinKey;
    }

    public void setJoinKey(String joinKey) {
        this.joinKey = joinKey;
    }

    @Column(name = "join_key", nullable=true)
    private String joinKey;

    public CourseEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CourseEntity() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(OffsetDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public int getCourseYear() {
        return courseYear;
    }
    public void setCourseYear(int courseYear){
        this.courseYear = courseYear;
    }



}
