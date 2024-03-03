package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name="submissions")
public class SubmissionEntity {
    @Id
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
