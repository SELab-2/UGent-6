package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.json.GroupFeedbackJson;
import com.ugent.pidgeon.model.json.UpdateGroupScoreRequest;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
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


    public CheckResult<GroupFeedbackEntity> getGroupFeedbackIfExists(long groupId, long projectId) {
        GroupFeedbackId id = new GroupFeedbackId(groupId, projectId);
        GroupFeedbackEntity groupFeedback = groupFeedbackRepository.findById(id).orElse(null);
        if (groupFeedback == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group feedback not found", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", groupFeedback);
    }

    public GroupFeedbackJson groupFeedbackEntityToJson(GroupFeedbackEntity groupFeedbackEntity) {
        return new GroupFeedbackJson(
                groupFeedbackEntity.getScore(),
                groupFeedbackEntity.getFeedback(),
                groupFeedbackEntity.getGroupId(),
                groupFeedbackEntity.getProjectId()
        );
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

    public CheckResult<Void> checkGroupFeedbackUpdateJson(UpdateGroupScoreRequest request, Long projectId) {
        CheckResult<ProjectEntity> projectCheck = projectUtil.getProjectIfExists(projectId);
        if (projectCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(projectCheck.getStatus(), projectCheck.getMessage(), null);
        }
        Integer maxScore = projectCheck.getData().getMaxScore();
        if (request.getScore() == null || request.getFeedback() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Score and feedback need to be provided", null);
        }

        if (maxScore != null && request.getScore() < 0) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Score can't be lower than 0", null);
        }

        if (maxScore != null && request.getScore() > maxScore) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Score can't be higher than the defined max score (" + maxScore + ")", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", null);
    }
}
