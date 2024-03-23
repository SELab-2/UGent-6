package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.json.UpdateGroupScoreRequest;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public class PermissionHandler {

    public static ResponseEntity<String> userHasAccesToGroup(GroupRepository groupRepository, UserEntity user, long groupId) {
        if (!groupRepository.userAccessToGroup(user.getId(), groupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this group");
        }
        return null;
    }

    public static ResponseEntity<String> projectNotFound(ProjectEntity project) {
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }
        return null;
    }

    public static ResponseEntity<String> scoreValidation(UpdateGroupScoreRequest request, ProjectEntity project) {
        float score = request.getScore();
        if (score < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Score can't be negative");
        } else if (project.getMaxScore() != null && project.getMaxScore() < score) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Score can't be higher than the defined max score (" + project.getMaxScore() + ")");
        }
        return null;
    }

    public static ResponseEntity<String> userIsCouresAdmin( Optional<CourseUserEntity> courseUserEntity) {
        if (courseUserEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not in course");
        }
        if (courseUserEntity.get().getRelation() != CourseRelation.course_admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not a course admin");
        }
        return null;
    }


}
