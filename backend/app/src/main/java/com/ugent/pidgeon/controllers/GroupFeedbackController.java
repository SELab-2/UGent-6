package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.json.UpdateGroupScoreRequest;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupFeedbackJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.Permission;
import com.ugent.pidgeon.util.PermissionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
public class GroupFeedbackController {

    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseUserRepository courseUserRepository;

    private Permission handleCommonChecks(long groupId, long projectId, UpdateGroupScoreRequest request, UserEntity user) {
        // Access check
        Permission permission;
        if (!user.getRole().equals(UserRole.admin)) {
            permission = PermissionHandler.userHasAccesToGroup(groupRepository, user, groupId);
            if (!permission.hasPermission()) {
                return permission;
            }
        }


        // Project check
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);
        permission = PermissionHandler.projectNotFound(project);
        if (!permission.hasPermission()) {
            return permission;
        }

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
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}/projects/{projectid}/feedback
     */
    @PatchMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
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

        Permission permission = handleCommonChecks(groupId, projectId, request, user);
        if (!permission.hasPermission()) {
            return permission.getResponseEntity();
        }
        CourseEntity courseEntity = courseRepository.findCourseEntityByGroupId(groupId).get(0);
        if (courseEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
        if (!user.getRole().equals(UserRole.admin)) {
            Optional<CourseUserEntity> courseUserEntity = courseUserRepository.findByCourseIdAndUserId(courseEntity.getId(), user.getId());
            if (courseUserEntity.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in course");
            }
            return PermissionHandler.userIsCourseAdmin(courseUserEntity.get()).getResponseEntity();
        } else {
            return null;
        }
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
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}/projects/{projectid}/feedback
     */
    @PostMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
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
        if (!user.getRole().equals(UserRole.admin)) {
            if (!groupRepository.userInGroup(groupId, user.getId()) && !groupRepository.isAdminOfGroup(user.getId(), groupId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this group");
            }
        }


        GroupFeedbackEntity groupFeedbackEntity = groupFeedbackRepository.getGroupFeedback(groupId, projectId);
        if (groupFeedbackEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }

        return ResponseEntity.ok(new GroupFeedbackJson(groupFeedbackEntity.getScore(), groupFeedbackEntity.getFeedback(), groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId()));
    }

}
