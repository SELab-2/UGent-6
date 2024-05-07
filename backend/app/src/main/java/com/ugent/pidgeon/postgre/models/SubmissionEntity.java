package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="submissions")
public class SubmissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="submission_id", nullable=false)
    private long id;

    @Column(name="project_id", nullable=false)
    private long projectId;

    @Column(name="group_id", nullable=false)
    private long groupId;

    @Column(name="file_id", nullable=false)
    private long fileId;

    @Column(name="submission_time", nullable=false)
    private OffsetDateTime submissionTime;

    @Column(name="structure_accepted", nullable=false)
    private Boolean structureAccepted;

    @Column(name="docker_accepted", nullable = false)
    private Boolean dockerAccepted;

    @Column(name="structure_feedback")
    private String structureFeedback;

    @Column(name="docker_feedback")
    private String dockerFeedback;

    @Column(name="docker_test_state")
    private Integer dockerTestState;

    public SubmissionEntity() {
    }

    public SubmissionEntity(long projectId, long groupId, Long fileId, OffsetDateTime submissionTime, Boolean structureAccepted, Boolean dockerAccepted) {
        this.projectId = projectId;
        this.groupId = groupId;
        this.fileId = fileId;
        this.submissionTime = submissionTime;
        this.structureAccepted = structureAccepted;
        this.dockerAccepted = dockerAccepted;
    }

    public long getGroupId() {
        return groupId;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public OffsetDateTime getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(OffsetDateTime submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Boolean getStructureAccepted() {
        return structureAccepted;
    }

    public void setStructureAccepted(Boolean accepted) {
        this.structureAccepted = accepted;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean getDockerAccepted() {
        return dockerAccepted;
    }

    public void setDockerAccepted(Boolean dockerAccepted) {
        this.dockerAccepted = dockerAccepted;
    }

    public String getStructureFeedback() {
        return structureFeedback;
    }

    public void setStructureFeedback(String structureFeedbackFileId) {
        this.structureFeedback = structureFeedbackFileId;
    }

    public String getDockerFeedback() {
        return dockerFeedback;
    }

    public void setDockerFeedback(String dockerFeedbackFileId) {
        this.dockerFeedback = dockerFeedbackFileId;
    }
    public Integer getDockerTestState() {
        return dockerTestState;
    }

    public void setDockerTestState(Integer dockerTestState) {
        this.dockerTestState = dockerTestState;
    }
}
