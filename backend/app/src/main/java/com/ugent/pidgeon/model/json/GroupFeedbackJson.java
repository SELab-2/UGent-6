package com.ugent.pidgeon.model.json;

public class GroupFeedbackJson {

    private float score;
    private String feedback;

    private long groupId;
    private long projectId;

    public GroupFeedbackJson() {
    }

    public GroupFeedbackJson(float score, String feedback, long groupId, long projectId) {
        this.score = score;
        this.feedback = feedback;
        this.groupId = groupId;
        this.projectId = projectId;
    }


    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

}
