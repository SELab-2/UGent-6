package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

@Entity
@Table(name="group_clusters")
public class GroupClusterEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="group_cluster_id", nullable=false)
    private int id;

    @Column(name="course_id", nullable=false)
    private int course_id;

    @Column(name="max_size", nullable=false)
    private int max_size;

    @Column(name="cluster_name", nullable=false)
    private String cluster_name;

    @Column(name="group_amount", nullable=false)
    private int group_amount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourse_id() {
        return course_id;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    public int getMax_size() {
        return max_size;
    }

    public void setMax_size(int max_size) {
        this.max_size = max_size;
    }

    public String getCluster_name() {
        return cluster_name;
    }

    public void setCluster_name(String cluster_name) {
        this.cluster_name = cluster_name;
    }

    public int getGroup_amount() {
        return group_amount;
    }

    public void setGroup_amount(int group_amount) {
        this.group_amount = group_amount;
    }
}
