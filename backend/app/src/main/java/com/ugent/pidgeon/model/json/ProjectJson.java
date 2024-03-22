package com.ugent.pidgeon.model.json;

import java.sql.Timestamp;

public class ProjectJson {

    private String name;
    private String description;
    private Long groupClusterId;
    private Long testId;
    private boolean visible;
    private Integer maxScore;
    private Timestamp deadline;

    public ProjectJson(String name, String description, Long groupClusterId, Long testId, boolean visible, Integer maxScore, Timestamp deadline) {
        this.name = name;
        this.description = description;
        this.groupClusterId = groupClusterId;
        this.testId = testId;
        this.visible = visible;
        this.maxScore = maxScore;
        this.deadline = deadline;
    }

    public String getName() {
        return name;
    }

    public Timestamp getDeadline() {
        return deadline;
    }

    public void setDeadline(Timestamp deadline) {
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

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
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
