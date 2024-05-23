package com.ugent.pidgeon.json;

import java.time.OffsetDateTime;

public class GroupClusterUpdateJson {
    private String name;
    private Integer capacity;
    private OffsetDateTime lockGroupsAfter;

    public GroupClusterUpdateJson() {
    }

    // Getters
    public String getName() {
        return name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public OffsetDateTime getLockGroupsAfter() {
        return lockGroupsAfter;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setLockGroupsAfter(OffsetDateTime lockGroupsAfter) {
        this.lockGroupsAfter = lockGroupsAfter;
    }
}