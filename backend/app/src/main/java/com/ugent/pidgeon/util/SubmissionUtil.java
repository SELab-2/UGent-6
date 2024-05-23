package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.logging.Logger;

@Component
public class SubmissionUtil {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ProjectUtil projectUtil;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private GroupUtil groupUtil;
  @Autowired
  private GroupClusterRepository groupClusterRepository;


    /**
     * Check if a user can get a submission
     * @param submissionId id of the submission
     * @param user user that wants to get the submission
     * @return CheckResult with the status of the check and the submission
     */
    public CheckResult<SubmissionEntity> canGetSubmission(long submissionId, UserEntity user) {
        SubmissionEntity submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Submission not found", null);
        }
        CheckResult<Void> groupCheck = groupUtil.canGetProjectGroupData(submission.getGroupId(), submission.getProjectId(), user);
        if (groupCheck.getStatus().equals(HttpStatus.OK)) {
            return new CheckResult<>(HttpStatus.OK, "", submission);
        } else {
            return new CheckResult<>(groupCheck.getStatus(), groupCheck.getMessage(), null);
        }
    }

    /**
     * Check if a user can delete a submission
     * @param submissionId id of the submission
     * @param user user that wants to delete the submission
     * @return CheckResult with the status of the check and the submission
     */
    public CheckResult<SubmissionEntity> canDeleteSubmission(long submissionId, UserEntity user) {
        SubmissionEntity submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Submission not found", null);
        }
        CheckResult<Void> projectCheck = projectUtil.isProjectAdmin(submission.getProjectId(), user);
        if (projectCheck.getStatus().equals(HttpStatus.OK)) {
            return new CheckResult<>(HttpStatus.OK, "", submission);
        } else {
            return new CheckResult<>(projectCheck.getStatus(), projectCheck.getMessage(), null);
        }
    }

    /**
     * Check if a user can submit a submission
     * @param projectId id of the project
     * @param user user that wants to submit the submission
     * @return CheckResult with the status of the check and the group id
     */
    public CheckResult<Long> checkOnSubmit(long projectId, UserEntity user) {
        CheckResult<ProjectEntity> projectCheck = projectUtil.getProjectIfExists(projectId);
        if (projectCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<> (projectCheck.getStatus(), projectCheck.getMessage(), null);
        }

        ProjectEntity project = projectCheck.getData();

        if (!projectUtil.userPartOfProject(projectId, user.getId())) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "You aren't part of this project", null);
        }

        Long groupId = groupRepository.groupIdByProjectAndUser(projectId, user.getId());
        if (groupId == null) {
            CheckResult<Void> projectAdminCheck = projectUtil.isProjectAdmin(projectId, user);
            if (projectAdminCheck.getStatus() != HttpStatus.OK) {
                return new CheckResult<>(HttpStatus.BAD_REQUEST, "User is not part of a group for this project", null);
            }
        } else {
            CheckResult<GroupEntity> groupCheck = groupUtil.getGroupIfExists(groupId);
            if (groupCheck.getStatus() != HttpStatus.OK) {
                return new CheckResult<>(groupCheck.getStatus(), groupCheck.getMessage(), null);
            }

            if (groupClusterRepository.inArchivedCourse(project.getGroupClusterId())) {
                return new CheckResult<>(HttpStatus.FORBIDDEN, "Cannot submit for a project in an archived course", null);
            }
        }

        OffsetDateTime time = OffsetDateTime.now();
        Logger.getGlobal().info("Time: " + time + " Deadline: " + project.getDeadline());
        if (time.isAfter(project.getDeadline()) && groupId != null) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Project deadline has passed", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", groupId);
    }
}
