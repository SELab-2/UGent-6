package com.ugent.pidgeon.model.json;

public class ProjectJson {

    private String name;
    private String description;
    private long groupClusterId;
    private long testId;
    private boolean visible;
    private int maxScore;
    //TODO: Deadline toevoegen als de functionaliteit er is

    public ProjectJson(String name, String description, long groupClusterId, long testId, boolean visible, int maxScore) {
        this.name = name;
        this.description = description;
        this.groupClusterId = groupClusterId;
        this.testId = testId;
        this.visible = visible;
        this.maxScore = maxScore;
    }

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

    public long getGroupClusterId() {
        return groupClusterId;
    }

    public void setGroupClusterId(long groupClusterId) {
        this.groupClusterId = groupClusterId;
    }

    public long getTestId() {
        return testId;
    }

    public void setTestId(long testId) {
        this.testId = testId;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }
}
