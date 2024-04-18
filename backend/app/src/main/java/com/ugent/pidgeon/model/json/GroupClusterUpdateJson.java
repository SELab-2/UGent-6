package com.ugent.pidgeon.model.json;

public class GroupClusterUpdateJson {
    private String name;
    private Integer capacity;

    public GroupClusterUpdateJson() {
    }

    // Getters
    public String getName() {
        return name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}