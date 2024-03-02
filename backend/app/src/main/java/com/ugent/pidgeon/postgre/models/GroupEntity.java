package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

@Entity
@Table(name="groups")
public class GroupEntity {

    @Id
    @GeneratedValue
    @Column(name="group_id", nullable=false)
    private long id;

    @Column(name="group_name", nullable=false)
    private String name;

    @Column(name="group_cluster", nullable = false)
    private int cluster;

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

    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }
}
