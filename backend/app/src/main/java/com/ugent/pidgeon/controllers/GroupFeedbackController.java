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
     * @ApiPath /api/projects/{projectid}/groups/{groupid}/score
     */
    @PatchMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {

        CheckResult checkResult = checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        GroupFeedbackEntity groupFeedbackEntity = groupFeedbackRepository.findById(new GroupFeedbackId(groupId, projectId)).orElse(null);
        if (groupFeedbackEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }

        if (request.getScore() == null) {
            request.setScore(groupFeedbackEntity.getScore());
        }

        if (request.getFeedback() == null) {
            request.setFeedback(groupFeedbackEntity.getFeedback());
        }

        CheckResult checkResultJson = checkGroupFeedbackUpdateJson(request, projectId);
        if (checkResultJson.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResultJson.getStatus()).body(checkResultJson.getMessage());
        }

        return doGroupFeedbackUpdate(groupFeedbackEntity, request);
    }

    @PutMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateGroupScorePut(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {

        CheckResult checkResult = checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }


        CheckResult checkResultJson = checkGroupFeedbackUpdateJson(request, projectId);
        if (checkResultJson.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResultJson.getStatus()).body(checkResultJson.getMessage());
        }
        GroupFeedbackEntity groupFeedbackEntity = groupFeedbackRepository.findById(new GroupFeedbackId(groupId, projectId)).orElse(null);
        if (groupFeedbackEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }

        return doGroupFeedbackUpdate(groupFeedbackEntity, request);
    }

    public ResponseEntity<?> doGroupFeedbackUpdate(GroupFeedbackEntity groupFeedbackEntity, UpdateGroupScoreRequest request) {
        groupFeedbackEntity.setScore(request.getScore());
        groupFeedbackEntity.setFeedback(request.getFeedback());
        try {
            groupFeedbackRepository.save(groupFeedbackEntity);
            return ResponseEntity.status(HttpStatus.OK).body(groupFeedbackEntity);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not update score of group feedback");
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
    public ResponseEntity<?> addGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {

        CheckResult checkResult = checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        CheckResult checkResultJson = checkGroupFeedbackUpdateJson(request, projectId);
        if (checkResultJson.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResultJson.getStatus()).body(checkResultJson.getMessage());
        }

        if (groupFeedbackRepository.findById(new GroupFeedbackId(groupId, projectId)).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Group feedback already exists");
        }

        GroupFeedbackEntity groupFeedbackEntity = new GroupFeedbackEntity(groupId, projectId, request.getScore(), request.getFeedback());

        try {
            groupFeedbackEntity = groupFeedbackRepository.save(groupFeedbackEntity);
            return ResponseEntity.status(HttpStatus.CREATED).body(groupFeedbackEntity);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not add score to group feedback");
        }
    }

    private CheckResult checkGroupFeedback(long groupId, long projectId) {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return new CheckResult(HttpStatus.NOT_FOUND, "Project not found");
        }
        GroupEntity group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return new CheckResult(HttpStatus.NOT_FOUND, "Group not found");
        }

        if (group.getClusterId() != project.getGroupClusterId()) {
            return new CheckResult(HttpStatus.FORBIDDEN, "Group does not belong to project");
        }
        return new CheckResult(HttpStatus.OK, "");
    }

    private CheckResult checkGroupFeedbackUpdateJson(UpdateGroupScoreRequest request, Long projectId) {
        Integer maxScore = projectRepository.findById(projectId).get().getMaxScore();
        if (request.getScore() == null || request.getFeedback() == null) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "Score and feedback need to be provided");
        }

        if (maxScore != null && request.getScore() < 0) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "Score can't be lower than 0");
        }

        if (maxScore != null && request.getScore() > maxScore) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "Score can't be higher than the defined max score (" + maxScore + ")");
        }

        return new CheckResult(HttpStatus.OK, "");
    }

    private CheckResult checkGroupFeedbackUpdate(long groupId, long projectId, UserEntity user) {
        CheckResult checkResult = checkGroupFeedback(groupId, projectId);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return checkResult;
        }

        if (!user.getRole().equals(UserRole.admin)) {
            if (!groupRepository.isAdminOfGroup(user.getId(), groupId)) {
                return new CheckResult(HttpStatus.FORBIDDEN, "User does not have access to update this groups feedback");
            }
        }

        return new CheckResult(HttpStatus.OK, "");
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

        if (checkGroupFeedback(groupId, projectId).getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkGroupFeedback(groupId, projectId).getStatus()).body(checkGroupFeedback(groupId, projectId).getMessage());
        }

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
