package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.json.UpdateGroupScoreRequest;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;

import java.util.Optional;

public class PermissionHandler {

    public static Permission userHasAccesToGroup(GroupRepository groupRepository, UserEntity user, long groupId) {
        return new Permission(groupRepository.userAccessToGroup(user.getId(), groupId), "User does not have access to this group");
    }

    public static Permission projectNotFound(ProjectEntity project) {
        return new Permission(project != null, "Project not found");
    }

    public static Permission scoreValidation(UpdateGroupScoreRequest request, ProjectEntity project) {
        Permission permission = new Permission(false, "");
        float score = request.getScore();
        if (score < 0) {
           permission.setErrorMessage("Score can't be lower than 0");
        } else if (project.getMaxScore() != null && project.getMaxScore() < score) {
            permission.setErrorMessage("Score can't be higher than the defined max score (" + project.getMaxScore() + ")");
        }else{
            permission.setPermission(true);
        }
        return permission;
    }

    public static Permission userIsCouresAdmin( Optional<CourseUserEntity> courseUserEntity) {
        Permission permission = new Permission(false, "");
        if (courseUserEntity.isEmpty()) {
            permission.setErrorMessage("User is not in course");
        }
        else if (courseUserEntity.get().getRelation() != CourseRelation.course_admin && courseUserEntity.get().getRelation() != CourseRelation.creator) {
            permission.setErrorMessage("User is not a course admin");
        }else {
            permission.setPermission(true);
        }
        return permission;
    }

    public static Permission accesToSubmission(GroupRepository groupRepository, ProjectRepository projectRepository, long groupId, long projectId, UserEntity user) {
        boolean inGroup = groupRepository.userInGroup(groupId, user.getId());
        boolean isAdmin = (user.getRole() == UserRole.admin) || (projectRepository.adminOfProject(projectId, user.getId()));
        return new Permission(inGroup || isAdmin,"User does not have acces to the submission");
    }




}
