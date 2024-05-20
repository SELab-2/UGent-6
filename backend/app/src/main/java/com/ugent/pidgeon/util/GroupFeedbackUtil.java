package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.json.UpdateGroupScoreRequest;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GroupFeedbackUtil {

    @Autowired
    private ProjectUtil projectUtil;
    @Autowired
    private GroupUtil groupUtil;
    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;


    /**
     * Check if a group feedback exists
     * @param groupId id of the group
     * @param projectId id of the project
     * @return CheckResult with the status of the check and the group feedback
     */
    public CheckResult<GroupFeedbackEntity> getGroupFeedbackIfExists(long groupId, long projectId) {
        GroupFeedbackId id = new GroupFeedbackId(groupId, projectId);
        GroupFeedbackEntity groupFeedback = groupFeedbackRepository.findById(id).orElse(null);
        if (groupFeedback == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group feedback not found", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", groupFeedback);
    }

    /**
     * Check if a project and group exist and if the group belongs to the project
     * @param groupId id of the group
     * @param projectId id of the project
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> checkGroupFeedback(long groupId, long projectId) {
        CheckResult<ProjectEntity> projectCheck = projectUtil.getProjectIfExists(projectId);
        if (projectCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(projectCheck.getStatus(), projectCheck.getMessage(), null);
        }
        ProjectEntity project = projectCheck.getData();

        CheckResult<GroupEntity> groupCheck = groupUtil.getGroupIfExists(groupId);
        if (groupCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(groupCheck.getStatus(), groupCheck.getMessage(), null);
        }
        GroupEntity group = groupCheck.getData();

        if (group.getClusterId() != project.getGroupClusterId()) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Group does not belong to project", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }

    /**
     * Check if a user can update a group feedback
     * @param groupId id of the group
     * @param projectId id of the project
     * @param user user that wants to update the group feedback
     * @param httpMethod http method of the request
     * @return CheckResult with the status of the check and the group feedback
     */
    public CheckResult<GroupFeedbackEntity> checkGroupFeedbackUpdate(long groupId, long projectId, UserEntity user, HttpMethod httpMethod) {
        CheckResult<Void> checkGroupFeedback = checkGroupFeedback(groupId, projectId);
        if (checkGroupFeedback.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(checkGroupFeedback.getStatus(), checkGroupFeedback.getMessage(), null);
        }

        CheckResult<Void> adminCheck = groupUtil.isAdminOfGroup(groupId, user);
        if (adminCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(adminCheck.getStatus(), adminCheck.getMessage(), null);
        }

        GroupFeedbackEntity groupFeedbackEntity = groupFeedbackRepository.findById(new GroupFeedbackId(groupId, projectId)).orElse(null);
        if (httpMethod.equals(HttpMethod.POST) && groupFeedbackEntity != null) {
            return new CheckResult<>(HttpStatus.CONFLICT, "Group feedback already exists", null);
        } else if (!httpMethod.equals(HttpMethod.POST) && groupFeedbackEntity == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group feedback not found", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity);
    }

    /**
     * Check if the json for updating a group feedback is valid
     * @param request json for updating a group feedback
     * @param projectId id of the project
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> checkGroupFeedbackUpdateJson(UpdateGroupScoreRequest request, Long projectId) {
        CheckResult<ProjectEntity> projectCheck = projectUtil.getProjectIfExists(projectId);
        if (projectCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(projectCheck.getStatus(), projectCheck.getMessage(), null);
        }
        Integer maxScore = projectCheck.getData().getMaxScore();
        if (request.getFeedback() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Feedbacks need to be provided", null);
        }

        if (request.getScore() != null && request.getScore() < 0) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Score can't be lower than 0", null);
        }

        if (maxScore != null && request.getScore() != null && request.getScore() > maxScore) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Score can't be higher than the defined max score (" + maxScore + ")", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", null);
    }
}
