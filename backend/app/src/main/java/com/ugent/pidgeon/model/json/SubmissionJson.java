package com.ugent.pidgeon.model.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ugent.pidgeon.postgre.models.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;

public class SubmissionJson {
    private long submissionId;
    private String projectUrl;
    private String groupUrl;
    private Long projectId;
    private Long groupId;
    private String fileUrl;

    private Boolean structureAccepted;
    private Boolean dockerAccepted;
    private String dockerStatus;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime submissionTime;

    private String structureFeedbackUrl;

    public String getDockerFeedbackUrl() {
        return dockerFeedbackUrl;
    }

    public void setDockerFeedbackUrl(String dockerFeedbackUrl) {
        this.dockerFeedbackUrl = dockerFeedbackUrl;
    }

    private String dockerFeedbackUrl;

    public SubmissionJson() {
    }

    public SubmissionJson(
            long id, String projectUrl, String groupUrl, Long projectId, Long groupId, String fileUrl,
            Boolean structureAccepted, OffsetDateTime submissionTime, Boolean dockerAccepted, String structureFeedbackUrl, String dockerFeedbackUrl, String dockerStatus) {
        this.submissionId = id;
        this.projectUrl = projectUrl;
        this.groupUrl = groupUrl;
        this.projectId = projectId;
        this.groupId = groupId;
        this.fileUrl = fileUrl;
        this.structureAccepted = structureAccepted;
        this.submissionTime = submissionTime;
        this.dockerAccepted = dockerAccepted;
        this.structureFeedbackUrl = structureFeedbackUrl;
        this.dockerFeedbackUrl = dockerFeedbackUrl;
        this.dockerStatus = dockerStatus;
    }

    public long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(long submissionId) {
        this.submissionId = submissionId;
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

    public OffsetDateTime getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(OffsetDateTime submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Boolean getDockerAccepted() {
        return dockerAccepted;
    }

    public void setDockerAccepted(Boolean dockerAccepted) {
        this.dockerAccepted = dockerAccepted;
    }

    public String getStructureFeedbackUrl() {
        return structureFeedbackUrl;
    }

    public void setStructureFeedbackUrl(String structureFeedbackUrl) {
        this.structureFeedbackUrl = structureFeedbackUrl;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getDockerStatus() {
        return dockerStatus;
    }

    public void setDockerStatus(String dockerStatus) {
        this.dockerStatus = dockerStatus;
    }
}
