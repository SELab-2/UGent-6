package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.json.UpdateGroupScoreRequest;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupFeedbackJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.GroupFeedbackUtil;
import com.ugent.pidgeon.util.GroupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class GroupFeedbackController {

    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;
    @Autowired
    private GroupFeedbackUtil groupFeedbackUtil;
    @Autowired
    private GroupUtil groupUtil;

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

        CheckResult<GroupFeedbackEntity> checkResult = groupFeedbackUtil.checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity(), HttpMethod.PATCH);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        GroupFeedbackEntity groupFeedbackEntity = checkResult.getData();

        if (request.getScore() == null) {
            request.setScore(groupFeedbackEntity.getScore());
        }

        if (request.getFeedback() == null) {
            request.setFeedback(groupFeedbackEntity.getFeedback());
        }

        CheckResult<Void> checkResultJson = groupFeedbackUtil.checkGroupFeedbackUpdateJson(request, projectId);
        if (checkResultJson.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResultJson.getStatus()).body(checkResultJson.getMessage());
        }

        return doGroupFeedbackUpdate(groupFeedbackEntity, request);
    }

    @PutMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateGroupScorePut(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {

        CheckResult<GroupFeedbackEntity> checkResult = groupFeedbackUtil.checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity(), HttpMethod.PUT);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        GroupFeedbackEntity groupFeedbackEntity = checkResult.getData();

        CheckResult<Void> checkResultJson = groupFeedbackUtil.checkGroupFeedbackUpdateJson(request, projectId);
        if (checkResultJson.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResultJson.getStatus()).body(checkResultJson.getMessage());
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

        CheckResult<GroupFeedbackEntity> groupFeedback = groupFeedbackUtil.checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity(), HttpMethod.POST);
        if (groupFeedback.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(groupFeedback.getStatus()).body(groupFeedback.getMessage());
        }

        CheckResult<Void> checkResultJson = groupFeedbackUtil.checkGroupFeedbackUpdateJson(request, projectId);
        if (checkResultJson.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResultJson.getStatus()).body(checkResultJson.getMessage());
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

        CheckResult<Void> checkResult = groupFeedbackUtil.checkGroupFeedback(groupId, projectId);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        CheckResult<Void> canGetFeedback = groupUtil.canGetProjectGroupData(groupId, projectId, user);
        if (canGetFeedback.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(canGetFeedback.getStatus()).body(canGetFeedback.getMessage());
        }

        CheckResult<GroupFeedbackEntity> groupFeedback = groupFeedbackUtil.getGroupFeedbackIfExists(groupId, projectId);
        if (groupFeedback.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(groupFeedback.getStatus()).body(groupFeedback.getMessage());
        }
        GroupFeedbackEntity groupFeedbackEntity = groupFeedback.getData();

        return ResponseEntity.ok(groupFeedbackUtil.groupFeedbackEntityToJson(groupFeedbackEntity));
    }

}
