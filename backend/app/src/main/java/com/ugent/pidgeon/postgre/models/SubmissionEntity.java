package com.ugent.pidgeon.postgre.models;

import com.ugent.pidgeon.postgre.models.types.DockerTestType;
import com.ugent.pidgeon.postgre.models.types.DockerTestState;
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
    private Long groupId;

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
    private String dockerTestState;

    @Column(name="docker_type")
    private String dockerType;

    public SubmissionEntity() {
    }

    public SubmissionEntity(long projectId, Long groupId, Long fileId, OffsetDateTime submissionTime, Boolean structureAccepted, Boolean dockerAccepted) {
        this.projectId = projectId;
        this.groupId = groupId;
        this.fileId = fileId;
        this.submissionTime = submissionTime;
        this.structureAccepted = structureAccepted;
        this.dockerAccepted = dockerAccepted;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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
    public DockerTestState getDockerTestState() {
        if(dockerTestState == null) {
            return DockerTestState.no_test;
        }
        return switch (dockerTestState) {
            case "running" -> DockerTestState.running;
            case "finished" -> DockerTestState.finished;
            case "aborted" -> DockerTestState.aborted;
            default -> null;
        };
    }

    public void setDockerTestState(DockerTestState dockerTestState) {
        this.dockerTestState = dockerTestState.toString();
    }

    public DockerTestType getDockerTestType() {
        if (dockerType == null) {
            return DockerTestType.NONE;
        }
        return switch (dockerType) {
            case "SIMPLE" -> DockerTestType.SIMPLE;
            case "TEMPLATE" -> DockerTestType.TEMPLATE;
            case "NONE" -> DockerTestType.NONE;
            default -> null;
        };
    }

    public void setDockerType(DockerTestType dockerType) {
        this.dockerType = dockerType.toString();
    }


}
