package com.ugent.pidgeon.model.json;

import java.sql.Timestamp;

public class SubmissionJson {
    private long id;
    private String projectUrl;
    private String groupUrl;

    private String fileUrl;

    private Boolean accepted;

    private Timestamp submissionTime;

    public SubmissionJson() {
    }

    public SubmissionJson(long id, String projectUrl, String groupUrl, String fileUrl, Boolean accepted, Timestamp submissionTime) {
        this.id = id;
        this.projectUrl = projectUrl;
        this.groupUrl = groupUrl;
        this.fileUrl = fileUrl;
        this.accepted = accepted;
        this.submissionTime = submissionTime;
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

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Timestamp getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Timestamp submissionTime) {
        this.submissionTime = submissionTime;
    }
}
