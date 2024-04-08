package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.controllers.SubmissionController;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.ProjectJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class ProjectUtil {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ClusterUtil clusterUtil;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private CourseUserRepository courseUserRepository;
    @Autowired
    private GroupRepository groupRepository;

    public boolean userPartOfProject(long projectId, long userId) {
        return projectRepository.userPartOfProject(projectId, userId);
    }

    public ProjectEntity getProjectIfExists(long projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

    public CheckResult<Void> isProjectAdmin(long projectId, UserEntity user) {
        if(!projectRepository.adminOfProject(projectId, user.getId()) && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "You are not and admin of this project", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }

    public CheckResult<ProjectEntity> getProjectIfAdmin(long projectId, UserEntity user) {
        ProjectEntity project = getProjectIfExists(projectId);
        if (project == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Project not found", null);
        }
        if (!projectRepository.adminOfProject(projectId, user.getId()) && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "You are not an admin of this project", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", project);
    }

    public CheckResult<Void> checkProjectJson(ProjectJson projectJson, long courseId) {
        if (projectJson.getName() == null ||
                projectJson.getDescription() == null ||
                projectJson.getMaxScore() == null ||
                projectJson.getGroupClusterId() == null ||
                projectJson.getDeadline() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "name, description, maxScore and deadline are required fields", null);
        }

        if (projectJson.getName().isBlank()) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "name cannot be empty", null);
        }

        CheckResult<Void> clusterCheck = clusterUtil.partOfCourse(projectJson.getGroupClusterId(), courseId);
        if (!clusterCheck.getStatus().equals(HttpStatus.OK)) {
            return clusterCheck;
        }

        if (projectJson.getDeadline().isBefore(OffsetDateTime.now())) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Deadline is in the past", null);
        }

        if (projectJson.getMaxScore() < 0) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Max score cannot be negative", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", null);
    }

    public ProjectResponseJson projectEntityToProjectResponseJson(ProjectEntity project, CourseEntity course, UserEntity user) {
        // Calculate the progress of the project for all groups
        List<Long> groupIds = projectRepository.findGroupIdsByProjectId(project.getId());
        Integer total = groupIds.size();
        Integer completed = groupIds.stream().map(groupId -> {
            Long submissionId = submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(project.getId(), groupId);
            if (submissionId == null) {
                return 0;
            }
            SubmissionEntity submission = submissionRepository.findById(submissionId).orElse(null);
            if (submission == null) {
                return 0;
            }
            if (submission.getDockerAccepted() && submission.getStructureAccepted()) return 1;
            return 0;
        }).reduce(0, Integer::sum);

        // Get the submissonUrl, depends on if the user is a course_admin or enrolled
        String submissionUrl = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/submissions";
        CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(course.getId(), user.getId())).orElse(null);
        if (courseUserEntity == null) {
            return null;
        }
        if (courseUserEntity.getRelation() == CourseRelation.enrolled) {
            Long groupId = groupRepository.groupIdByProjectAndUser(project.getId(), user.getId());
            if (groupId == null) {
                return null;
            }
            submissionUrl += "/" + groupId;
        }

        return new ProjectResponseJson(
                new CourseReferenceJson(course.getName(), ApiRoutes.COURSE_BASE_PATH + "/" + course.getId(), course.getId()),
                project.getDeadline(),
                project.getDescription(),
                project.getId(),
                project.getName(),
                submissionUrl,
                ApiRoutes.TEST_BASE_PATH + "/" + project.getTestId(),
                project.getMaxScore(),
                project.isVisible(),
                new ProjectProgressJson(completed, total)
        );
    }

    public CheckResult<ProjectEntity> canGetProject(long projectId, UserEntity user) {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Project not found", null);
        }

        boolean studentof = projectRepository.userPartOfProject(projectId, user.getId());
        boolean isAdmin = (user.getRole() == UserRole.admin) || (projectRepository.adminOfProject(projectId, user.getId()));

        if (studentof || isAdmin) {
            return new CheckResult<>(HttpStatus.OK, "", project);
        } else {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User does not have access to this project", null);
        }
    }

}
