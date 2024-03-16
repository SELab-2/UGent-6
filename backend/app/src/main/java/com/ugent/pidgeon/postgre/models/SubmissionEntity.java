package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

import java.sql.Timestamp;

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
    private Timestamp submissionTime;

    @Column(name="structure_accepted", nullable=false)
    private Boolean structureAccepted;

    @Column(name="docker_accepted", nullable = false)
    private Boolean dockerAccepted;

    @Column(name="structure_feedback_fileid")
    private Long structureFeedbackFileId;

    @Column(name="docker_feedback_fileid")
    private Long dockerFeedbackFileId;

    public SubmissionEntity() {
    }

    public SubmissionEntity(long projectId, long groupId, long fileId, Timestamp submissionTime, Boolean structureAccepted, Boolean dockerAccepted) {
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

    public Timestamp getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Timestamp submissionTime) {
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

    public Long getStructureFeedbackFileId() {
        return structureFeedbackFileId;
    }

    public void setStructureFeedbackFileId(Long structureFeedbackFileId) {
        this.structureFeedbackFileId = structureFeedbackFileId;
    }

    public Long getDockerFeedbackFileId() {
        return dockerFeedbackFileId;
    }

    public void setDockerFeedbackFileId(Long dockerFeedbackFileId) {
        this.dockerFeedbackFileId = dockerFeedbackFileId;
    }
}
