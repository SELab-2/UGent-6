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

    @Column(name="accepted", nullable=false)
    private Boolean accepted;

    public SubmissionEntity() {
    }

    public SubmissionEntity(long projectId, long groupId, long fileId, Timestamp submissionTime, Boolean accepted) {
        this.projectId = projectId;
        this.groupId = groupId;
        this.fileId = fileId;
        this.submissionTime = submissionTime;
        this.accepted = accepted;
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

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
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
}
