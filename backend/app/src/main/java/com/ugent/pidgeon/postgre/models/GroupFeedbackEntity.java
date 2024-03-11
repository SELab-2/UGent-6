package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@IdClass(GroupFeedBackId.class)
@Table(name = "group_feedback")
public class GroupFeedbackEntity {

    @Id
    @Column(name = "group_id", nullable = false)
    private long groupId;

    @Id
    @Column(name = "project_id", nullable = false)
    private long projectId;

    @Column(name = "grade")
    private Float grade;

    @Column(name = "feedback")
    private String feedback;

    public GroupFeedbackEntity() {
    }

    public GroupFeedbackEntity(long groupId, long projectId, Float grade, String feedback) {
        this.groupId = groupId;
        this.projectId = projectId;
        this.grade = grade;
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

    public Float getGrade() {
        return grade;
    }

    public void setGrade(Float grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }


    public float getScore() {
        return grade;
    }

    public void setScore(float score) {
        this.grade = score;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}

class GroupFeedBackId implements Serializable {
    private long groupId;
    private long projectId;
}
