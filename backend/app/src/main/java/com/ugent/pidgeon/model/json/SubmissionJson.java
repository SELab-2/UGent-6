package com.ugent.pidgeon.model.json;

import java.sql.Timestamp;

public class SubmissionJson {
    private long id;
    private String projectUrl;
    private String groupUrl;

    private String fileUrl;

    private Boolean structureAccepted;
    private Boolean dockerAccepted;

    private Timestamp submissionTime;

    public SubmissionJson() {
    }

    public SubmissionJson(long id, String projectUrl, String groupUrl, String fileUrl, Boolean structureAccepted, Timestamp submissionTime, Boolean dockerAccepted) {
        this.id = id;
        this.projectUrl = projectUrl;
        this.groupUrl = groupUrl;
        this.fileUrl = fileUrl;
        this.structureAccepted = structureAccepted;
        this.submissionTime = submissionTime;
        this.dockerAccepted = dockerAccepted;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getGroupUrl() {
        return groupUrl;
    }

    public void setGroupUrl(String groupUrl) {
        this.groupUrl = groupUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Boolean getStructureAccepted() {
        return structureAccepted;
    }

    public void setStructureAccepted(Boolean structureAccepted) {
        this.structureAccepted = structureAccepted;
    }

    public Timestamp getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Timestamp submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Boolean getDockerAccepted() {
        return dockerAccepted;
    }

    public void setDockerAccepted(Boolean dockerAccepted) {
        this.dockerAccepted = dockerAccepted;
    }
}
