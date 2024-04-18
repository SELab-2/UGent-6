package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.ApiRoutes;
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

@Component
public class ProjectUtil {

  @Autowired
  private ProjectRepository projectRepository;
  @Autowired
  private ClusterUtil clusterUtil;

  /**
   * Check if a user is part of a project
   *
   * @param projectId id of the project
   * @param userId    id of the user
   * @return true if the user is part of the project
   */
  public boolean userPartOfProject(long projectId, long userId) {
    return projectRepository.userPartOfProject(projectId, userId);
  }

  /**
   * Check if a project exists
   *
   * @param projectId id of the project
   * @return CheckResult with the status of the check and the project
   */
  public CheckResult<ProjectEntity> getProjectIfExists(long projectId) {
    ProjectEntity project = projectRepository.findById(projectId).orElse(null);
    if (project == null) {
      return new CheckResult<>(HttpStatus.NOT_FOUND, "Project not found", null);
    }
    return new CheckResult<>(HttpStatus.OK, "", project);
  }


  /**
   * Check if a user is an admin of a project
   *
   * @param projectId id of the project
   * @param user      user that wants to get the project
   * @return CheckResult with the status of the check
   */
  public CheckResult<Void> isProjectAdmin(long projectId, UserEntity user) {
    if (!projectRepository.adminOfProject(projectId, user.getId()) && !user.getRole()
        .equals(UserRole.admin)) {
      return new CheckResult<>(HttpStatus.FORBIDDEN, "You are not and admin of this project", null);
    }
    return new CheckResult<>(HttpStatus.OK, "", null);
  }

  /**
   * Check if a user is part of a project and is an admin
   *
   * @param projectId id of the project
   * @param user      user that wants to get the project
   * @return CheckResult with the status of the check
   */
  public CheckResult<ProjectEntity> getProjectIfAdmin(long projectId, UserEntity user) {
    CheckResult<ProjectEntity> projectCheck = getProjectIfExists(projectId);
    if (!projectCheck.getStatus().equals(HttpStatus.OK)) {
      return new CheckResult<>(projectCheck.getStatus(), projectCheck.getMessage(), null);
    }
    if (!projectRepository.adminOfProject(projectId, user.getId()) && !user.getRole()
        .equals(UserRole.admin)) {
      return new CheckResult<>(HttpStatus.FORBIDDEN, "You are not an admin of this project", null);
    }
    return new CheckResult<>(HttpStatus.OK, "", projectCheck.getData());
  }

  /**
   * Check if a projectJson is valid
   *
   * @param projectJson project json to check
   * @param courseId    id of the course
   * @return CheckResult with the status of the check
   */
  public CheckResult<Void> checkProjectJson(ProjectJson projectJson, long courseId) {
    if (projectJson.getName() == null ||
        projectJson.getDescription() == null ||
        projectJson.getMaxScore() == null ||
        projectJson.getGroupClusterId() == null ||
        projectJson.getDeadline() == null) {
      return new CheckResult<>(HttpStatus.BAD_REQUEST,
          "name, description, maxScore and deadline are required fields", null);
    }

    if (projectJson.getName().isBlank()) {
      return new CheckResult<>(HttpStatus.BAD_REQUEST, "name cannot be empty", null);
    }

    CheckResult<Void> clusterCheck = clusterUtil.partOfCourse(projectJson.getGroupClusterId(),
        courseId);
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


  /**
   * Check if a user can get a project
   *
   * @param projectId id of the project
   * @param user      user that wants to get the project
   * @return CheckResult with the status of the check and the project
   */
  public CheckResult<ProjectEntity> canGetProject(long projectId, UserEntity user) {
    ProjectEntity project = projectRepository.findById(projectId).orElse(null);
    if (project == null) {
      return new CheckResult<>(HttpStatus.NOT_FOUND, "Project not found", null);
    }

    boolean studentof = projectRepository.userPartOfProject(projectId, user.getId());
    boolean isAdmin =
        (user.getRole() == UserRole.admin) || (projectRepository.adminOfProject(projectId,
            user.getId()));

    if (studentof || isAdmin) {
      return new CheckResult<>(HttpStatus.OK, "", project);
    } else {
      return new CheckResult<>(HttpStatus.FORBIDDEN, "User does not have access to this project",
          null);
    }
  }

}
