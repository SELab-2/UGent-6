package com.ugent.pidgeon.json;

import java.time.OffsetDateTime;

public class ProjectUpdateDTO {
    private String name;
    private String description;
    private OffsetDateTime deadline;

    // Getters and Setters
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

    public OffsetDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(OffsetDateTime deadline) {
        this.deadline = deadline;
    }
}