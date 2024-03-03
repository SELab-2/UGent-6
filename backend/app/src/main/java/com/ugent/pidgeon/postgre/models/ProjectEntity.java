package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;


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
        private long testId;

        @Column(name="visible", nullable = false)
        private Boolean projectType;

        @OneToMany(mappedBy = "project")
        private List<DeadlineEntity> deadlines;

        @Column(name="max_score")
        private Integer maxScore;

        public ProjectEntity(long courseId, String name, String description, long groupClusterId, long testId, Boolean projectType, Integer maxScore) {
                this.courseId = courseId;
                this.name = name;
                this.description = description;
                this.groupClusterId = groupClusterId;
                this.testId = testId;
                this.projectType = projectType;
                this.maxScore = maxScore;
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

        public long getTestId() {
                return testId;
        }

        public void setTestId(long testId) {
                this.testId = testId;
        }

        public Boolean getProjectType() {
                return projectType;
        }

        public void setProjectType(Boolean projectType) {
                this.projectType = projectType;
        }

        public List<DeadlineEntity> getDeadlines() {
                return deadlines;
        }
        public Integer getMaxScore() {
                return maxScore;
        }

        public void setMaxScore(Integer maxScore) {
                this.maxScore = maxScore;
        }
}
