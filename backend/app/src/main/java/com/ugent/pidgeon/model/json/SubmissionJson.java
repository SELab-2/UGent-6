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
    private String dockerStatus;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime submissionTime;

    private String structureFeedback;


    private DockerTestFeedbackJson dockerFeedback;
    private String artifactUrl;



    public SubmissionJson() {
    }

    public SubmissionJson(
            long id, String projectUrl, String groupUrl, Long projectId, Long groupId, String fileUrl,
            Boolean structureAccepted, OffsetDateTime submissionTime, String structureFeedbackUrl, DockerTestFeedbackJson dockerFeedback, String dockerStatus,
        String artifactUrl) {
        this.submissionId = id;
        this.projectUrl = projectUrl;
        this.groupUrl = groupUrl;
        this.projectId = projectId;
        this.groupId = groupId;
        this.fileUrl = fileUrl;
        this.structureAccepted = structureAccepted;
        this.submissionTime = submissionTime;
        this.dockerFeedback = dockerFeedback;
        this.structureFeedback = structureFeedbackUrl;
        this.dockerStatus = dockerStatus;
      this.artifactUrl = artifactUrl;
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



    public String getStructureFeedback() {
        return structureFeedback;
    }

    public void setStructureFeedback(String structureFeedback) {
        this.structureFeedback = structureFeedback;
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

    public DockerTestFeedbackJson getDockerFeedback() {
        return dockerFeedback;
    }

    public void setDockerFeedback(DockerTestFeedbackJson dockerFeedback) {
        this.dockerFeedback = dockerFeedback;
    }

    public String getArtifactUrl() {
        return artifactUrl;
    }

    public void setArtifactUrl(String artifactUrl) {
        this.artifactUrl = artifactUrl;
    }
}
