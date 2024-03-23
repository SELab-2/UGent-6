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
import com.ugent.pidgeon.util.PermissionHandler;
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

    @Autowired
    private CourseUserRepository courseUserRepository;

    private ResponseEntity<String> handleCommonChecks(long groupId, long projectId, UpdateGroupScoreRequest request, UserEntity user) {
        // Access check
        ResponseEntity<String> accessCheck = PermissionHandler.userHasAccesToGroup(groupRepository, user, groupId);
        if (accessCheck != null) {
            return accessCheck;
        }

        // Project check
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);
        ResponseEntity<String> projectCheck = PermissionHandler.projectNotFound(project);
        if (projectCheck != null) return projectCheck;

        // Score validation
        return PermissionHandler.scoreValidation(request, project);
    }

    /**
     * Function to update the score of a group
     *
     * @param groupId   identifier of a group
     * @param projectId identifier of a project
     * @param request   request object containing the new score
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity<String>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883691">apiDog documentation</a>
     * @HttpMethod Patch
     * @AllowedRoles teacher
     * @ApiPath /api/groups/{groupid}/projects/{projectid}/feedback
     */
    @PatchMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher})
    public ResponseEntity<String> updateGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {
        ResponseEntity<String> errorResponse1 = getStringResponseEntity(groupId, projectId, request, auth);
        if (errorResponse1 != null) return errorResponse1;

        if (groupFeedbackRepository.updateGroupScore(request.getScore(), groupId, projectId, request.getFeedback()) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }
        return null;
    }

    private ResponseEntity<String> getStringResponseEntity(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {
        UserEntity user = auth.getUserEntity();

        ResponseEntity<String> errorResponse = handleCommonChecks(groupId, projectId, request, user);
        if (errorResponse != null) {
            return errorResponse;
        }
        errorResponse = PermissionHandler.userIsCouresAdmin(courseUserRepository.findByCourseIdAndUserId(user.getId(), groupId));
        return errorResponse;
    }

    /**
     * Function to add a score to a group
     *
     * @param groupId   identifier of a group
     * @param projectId identifier of a project
     * @param request   request object containing the new score
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity<String>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883691">apiDog documentation</a>
     * @HttpMethod Post
     * @AllowedRoles teacher
     * @ApiPath /api/groups/{groupid}/projects/{projectid}/feedback
     */
    @PostMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher})
    public ResponseEntity<String> addGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {
        ResponseEntity<String> errorResponse = getStringResponseEntity(groupId, projectId, request, auth);
        if (errorResponse != null) return errorResponse;


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

    /**
     * Function to get the score of a group
     *
     * @param groupId   identifier of a group
     * @param projectId identifier of a project
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity<Object>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883689">apiDog documentation</a>
     * @HttpMethod Get
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/groups/{groupid}/score
     */
    @GetMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> getGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, Auth auth) {
        UserEntity user = auth.getUserEntity();
        if (user.getRole() == UserRole.student && !groupRepository.userInGroup(groupId, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not in this group");
        } else if (user.getRole() == UserRole.teacher && !groupRepository.userAccessToGroup(user.getId(), groupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Teacher does not have access to this group");
        }

        GroupFeedbackEntity groupFeedbackEntity = groupFeedbackRepository.getGroupFeedback(groupId, projectId);
        if (groupFeedbackEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }

        return ResponseEntity.ok(new GroupFeedbackJson(groupFeedbackEntity.getScore(), groupFeedbackEntity.getFeedback(), groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId()));
    }

}
