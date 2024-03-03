package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name="group_clusters")
public class GroupClusterEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="group_cluster_id", nullable=false)
    private long id;

    @Column(name="course_id", nullable=false)
    private long courseId;

    @Column(name="max_size", nullable=false)
    private int maxSize;

    @Column(name="cluster_name", nullable=false)
    private String name;

    @Column(name="group_amount", nullable=false)
    private int groupAmount;

    @Column(name = "created_at")
    private Timestamp createdAt;

    public GroupClusterEntity(long courseId, int maxSize, String name, int groupAmount) {
        this.courseId = courseId;
        this.maxSize = maxSize;
        this.name = name;
        this.groupAmount = groupAmount;
    }

    public GroupClusterEntity() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long course_id) {
        this.courseId = course_id;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int max_size) {
        this.maxSize = max_size;
    }

    public String getName() {
        return name;
    }

    public void setName(String cluster_name) {
        this.name = cluster_name;
    }

    public int getGroupAmount() {
        return groupAmount;
    }

    public void setGroupAmount(int group_amount) {
        this.groupAmount = group_amount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
