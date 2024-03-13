package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.json.UpdateGroupScoreRequest;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupFeedbackJson;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class GroupFeedbackController {

    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private ResponseEntity<String> handleCommonChecks(long groupId, long projectId, UpdateGroupScoreRequest request, UserEntity user) {
        // Access check
        if (!groupRepository.userAccessToGroup(user.getId(), groupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this group");
        }

        // Project check
        ProjectEntity project = projectRepository.findById(projectId).orElseThrow(() -> {
             ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
             return new RuntimeException("Project not found");
        });
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }

        // Score validation
        float score = request.getScore();

        if (score < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Score can't be negative");
        } else if (project.getMaxScore() != null && project.getMaxScore() < score) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Score can't be higher than the defined max score (" + project.getMaxScore() + ")");
        }
        return null;
    }

    @PatchMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher})
    public ResponseEntity<String> updateGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {
        UserEntity user = auth.getUserEntity();

        ResponseEntity<String> errorResponse = handleCommonChecks(groupId, projectId, request, user);
        if (errorResponse != null) {
            return errorResponse;
        }

        if (groupFeedbackRepository.updateGroupScore(request.getScore(), groupId, projectId, request.getFeedback()) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }
        return null;
    }

    @PostMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher})
    public ResponseEntity<String> addGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {
        UserEntity user = auth.getUserEntity();

        ResponseEntity<String> errorResponse = handleCommonChecks(groupId, projectId, request, user);
        if (errorResponse != null) {
            return errorResponse;
        }

        try {
            if (groupFeedbackRepository.addGroupScore(request.getScore(), groupId, projectId, request.getFeedback()) == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not add score to group feedback");
        }
        return ResponseEntity.ok("Score added successfully");
    }

    @GetMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> getGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, Auth auth){
        UserEntity user = auth.getUserEntity();
        if(user.getRole() == UserRole.student && !groupRepository.userInGroup(groupId, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not in this group");
        } else if (user.getRole() == UserRole.teacher && !groupRepository.userAccessToGroup(user.getId(), groupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Teacher does not have access to this group");
        }

         GroupFeedbackEntity groupFeedbackEntity = groupFeedbackRepository.getGroupFeedback(groupId, projectId);
        if(groupFeedbackEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }

        return ResponseEntity.ok(new GroupFeedbackJson(groupFeedbackEntity.getScore(), groupFeedbackEntity.getFeedback(), groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId()));
    }

}
