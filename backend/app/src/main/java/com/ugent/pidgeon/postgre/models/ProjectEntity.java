package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "projects")
public class ProjectEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "project_id", nullable = false)
        private long id;

        @Column(name="course_id", nullable = false)
        private long courseId;

        @Column(name="project_name", nullable = false)
        private String name;

        @Column(name="description", nullable = false)
        private String description;

        @Column(name="group_cluster_id", nullable = false)
        private long groupClusterId;

        @Column(name="test_id", nullable = false)
        private Long testId;

        @Column(name="visible", nullable = false)
        private Boolean visible;

        @Column(name="deadline", nullable = false)
        private OffsetDateTime deadline;

        @Column(name="max_score")
        private Integer maxScore;

        @Column(name = "visible_after")
        private OffsetDateTime visibleAfter;

        public ProjectEntity(long courseId, String name, String description, long groupClusterId, Long testId, Boolean visible, Integer maxScore, OffsetDateTime deadline) {
                this.courseId = courseId;
                this.name = name;
                this.description = description;
                this.groupClusterId = groupClusterId;
                this.testId = testId;
                this.visible = visible;
                this.maxScore = maxScore;
                this.deadline = deadline;
        }

        public ProjectEntity() {
        }


        public long getId() {
                return id;
        }

        public void setId(long id) {
                this.id = id;
        }

        public long getCourseId() {
                return courseId;
        }

        public void setCourseId(long courseId) {
                this.courseId = courseId;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getDescription() {
                return description;
        }

        public void setDescription(String description) {
                this.description = description;
        }

        public long getGroupClusterId() {
                return groupClusterId;
        }

        public void setGroupClusterId(long groupClusterId) {
                this.groupClusterId = groupClusterId;
        }

        public Long getTestId() {
                return testId;
        }

        public void setTestId(Long testId) {
                this.testId = testId;
        }

        public Boolean isVisible() {
                return visible;
        }

        public void setVisible(Boolean projectType) {
                this.visible = projectType;
        }

        public Integer getMaxScore() {
                return maxScore;
        }

        public void setMaxScore(Integer maxScore) {
                this.maxScore = maxScore;
        }

        public OffsetDateTime getDeadline() {
                return deadline;
        }

        public void setDeadline(OffsetDateTime deadline) {
                this.deadline = deadline;
        }

        public OffsetDateTime getVisibleAfter() {
                return visibleAfter;
        }

        public void setVisibleAfter(OffsetDateTime visibleAfter) {
                this.visibleAfter = visibleAfter;
        }
}
