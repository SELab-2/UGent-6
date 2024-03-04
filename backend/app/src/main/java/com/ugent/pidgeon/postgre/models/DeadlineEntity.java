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

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @Column(name = "deadline")
    private Timestamp deadline;

    public DeadlineEntity() {
    }

    public DeadlineEntity(ProjectEntity project, Timestamp deadline) {
        this.project = project;
        this.deadline = deadline;
    }

    public Long getDeadlineId() {
        return deadlineId;
    }

    public void setDeadlineId(Long deadlineId) {
        this.deadlineId = deadlineId;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public Timestamp getDeadline() {
        return deadline;
    }

    public void setDeadline(Timestamp deadline) {
        this.deadline = deadline;
    }
}
