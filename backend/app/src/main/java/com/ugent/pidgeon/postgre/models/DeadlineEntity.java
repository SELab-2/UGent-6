package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "deadlines")
public class DeadlineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deadline_id")
    private Long deadlineId;

    @Column(name = "project_id")
    private long projectId;

    @Column(name = "deadline")
    private Timestamp deadline;

    public DeadlineEntity() {
    }

    public DeadlineEntity(long projectId, Timestamp deadline) {
        this.projectId = projectId;
        this.deadline = deadline;
    }

    public Long getDeadlineId() {
        return deadlineId;
    }

    public void setDeadlineId(Long deadlineId) {
        this.deadlineId = deadlineId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public Timestamp getDeadline() {
        return deadline;
    }

    public void setDeadline(Timestamp deadline) {
        this.deadline = deadline;
    }
}
