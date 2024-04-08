package com.ugent.pidgeon.model.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ugent.pidgeon.postgre.models.OffsetDateTimeSerializer;


import java.time.OffsetDateTime;



public class ProjectJson {

    private String name;
    private String description;
    private Long groupClusterId;
    private boolean visible;
    private Integer maxScore;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime deadline;

    public ProjectJson(String name, String description, Long groupClusterId, Long testId, boolean visible, Integer maxScore, OffsetDateTime deadline) {
        this.name = name;
        this.description = description;
        this.groupClusterId = groupClusterId;
        this.visible = visible;
        this.maxScore = maxScore;
        this.deadline = deadline;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(OffsetDateTime deadline) {
        this.deadline = deadline;
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

    public Long getGroupClusterId() {
        return groupClusterId;
    }

    public void setGroupClusterId(Long groupClusterId) {
        this.groupClusterId = groupClusterId;
    }


    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }
}
