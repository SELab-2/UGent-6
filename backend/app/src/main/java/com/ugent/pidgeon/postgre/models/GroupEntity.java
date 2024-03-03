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
    private long clusterId;

    public GroupEntity(String name, long clusterId) {
        this.name = name;
        this.clusterId = clusterId;
    }

    public GroupEntity() {

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

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(int cluster) {
        this.clusterId = cluster;
    }
}
