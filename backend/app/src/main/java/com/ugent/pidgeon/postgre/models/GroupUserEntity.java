package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@IdClass(GroupUserId.class)
@Table(name="group_users")
public class GroupUserEntity {
    @Id
    @Column(name="group_id", nullable=false)
    private long groupId;

    @Id
    @Column(name="user_id", nullable=false)
    private long userId;

    public GroupUserEntity() {
    }

    public GroupUserEntity(long group_id, long user_id) {
        this.groupId = group_id;
        this.userId = user_id;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long group_id) {
        this.groupId = group_id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long user_id) {
        this.userId = user_id;
    }
}

