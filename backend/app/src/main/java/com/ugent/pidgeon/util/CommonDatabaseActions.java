package com.ugent.pidgeon.util;


import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class CommonDatabaseActions {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupClusterRepository groupClusterRepository;
    @Autowired
    private GroupUserRepository groupUserRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private FileUtil fileUtil;


    /**
     * Remove a group from the database
     * @param groupId id of the group
     * @return true if the group was removed successfully
     */
    public boolean removeGroup(long groupId) {
        try {
            // Delete the group
            groupRepository.deleteGroupUsersByGroupId(groupId);
            groupRepository.deleteSubmissionsByGroupId(groupId);
            groupRepository.deleteGroupFeedbacksByGroupId(groupId);
            groupRepository.deleteById(groupId);

            // update groupcount in cluster
            groupClusterRepository.findById(groupId).ifPresent(cluster -> {
                cluster.setGroupAmount(cluster.getGroupAmount() - 1);
                groupClusterRepository.save(cluster);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Create a new individual cluster group for course
     * @param courseId id of the course
     * @param userId id of the user
     * @return true if the group was created successfully
     */
    public boolean createNewIndividualClusterGroup(long courseId, long userId) {
        GroupClusterEntity groupClusterEntity = groupClusterRepository.findIndividualClusterByCourseId(courseId).orElse(null);
        if (groupClusterEntity == null) {
            return false;
        }
        // Create new group for the cluster
        GroupEntity groupEntity = new GroupEntity("", groupClusterEntity.getId());
        groupClusterEntity.setGroupAmount(groupClusterEntity.getGroupAmount() + 1);
        groupClusterRepository.save(groupClusterEntity);
        groupEntity = groupRepository.save(groupEntity);

        // Add user to the group
        GroupUserEntity groupUserEntity = new GroupUserEntity(groupEntity.getId(), userId);
        groupUserRepository.save(groupUserEntity);
        return true;
    }

    /**
     * Remove an individual cluster group of a course
     * @param courseId id of the course
     * @param userId id of the user
     * @return true if the group was removed successfully
     */
    public boolean removeIndividualClusterGroup(long courseId, long userId) {
        GroupClusterEntity groupClusterEntity = groupClusterRepository.findIndividualClusterByCourseId(courseId).orElse(null);
        if (groupClusterEntity == null) {
            return false;
        }
        // Find the group of the user
        Optional<GroupEntity> groupEntityOptional = groupRepository.groupByClusterAndUser(groupClusterEntity.getId(), userId);
        return groupEntityOptional.filter(groupEntity -> removeGroup(groupEntity.getId())).isPresent();
    }

    /**
     * Delete a project and all its related data
     * @param projectId id of the project
     * @return CheckResult with the status of the deletion
     */
    public CheckResult<Void> deleteProject(long projectId) {
        try {
            ProjectEntity projectEntity = projectRepository.findById(projectId).orElse(null);
            if (projectEntity == null) {
                return new CheckResult<>(HttpStatus.NOT_FOUND, "Project not found", null);
            }

            groupFeedbackRepository.deleteAll(groupFeedbackRepository.findByProjectId(projectId));

            for (SubmissionEntity submissionEntity : submissionRepository.findByProjectId(projectId)) {
                CheckResult<Void> checkResult = deleteSubmissionById(submissionEntity.getId());
                if (!checkResult.getStatus().equals(HttpStatus.OK)) {
                    return checkResult;
                }
            }

            projectRepository.delete(projectEntity);

            TestEntity testEntity = testRepository.findById(projectEntity.getTestId()).orElse(null);
            if (testEntity == null) {
                return new CheckResult<>(HttpStatus.NOT_FOUND, "Test not found", null);
            }
            return deleteTestById(projectEntity, testEntity);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting project", null);
        }
    }

    /**
     * Delete a submission and its related data
     * @param submissionId id of the submission
     * @return CheckResult with the status of the deletion
     */
    public CheckResult<Void> deleteSubmissionById(long submissionId) {
        try {
            SubmissionEntity submission = submissionRepository.findById(submissionId).orElse(null);
            if (submission == null) {
                return new CheckResult<>(HttpStatus.NOT_FOUND, "Submission not found", null);
            }
            submissionRepository.delete(submission);
            return fileUtil.deleteFileById(submission.getFileId());
        } catch (Exception e) {
            return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting submission", null);
        }
    }


    /**
     * Delete a test and its related data
     * @param projectEntity project that the test is linked to
     * @param testEntity test to delete
     * @return CheckResult with the status of the deletion
     */
    public CheckResult<Void> deleteTestById(ProjectEntity projectEntity, TestEntity testEntity) {
        try {
            projectEntity.setTestId(null);
            projectRepository.save(projectEntity);
            testRepository.deleteById(testEntity.getId())   ;
            CheckResult<Void> checkAndDeleteRes = fileUtil.deleteFileById(testEntity.getStructureTestId());
            if (!checkAndDeleteRes.getStatus().equals(HttpStatus.OK)) {
                return checkAndDeleteRes;
            }
            return fileUtil.deleteFileById(testEntity.getDockerTestId());
        } catch (Exception e) {
            return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting test", null);
        }
    }

    /**
     * Delete a cluster and all its related data
     * @param clusterId id of the cluster
     * @return CheckResult with the status of the deletion
     */
    public CheckResult<Void> deleteClusterById(long clusterId) {
        try {
            for (GroupEntity group : groupRepository.findAllByClusterId(clusterId)) {
                // Delete all groupUsers
                groupUserRepository.deleteAllByGroupId(group.getId());
                groupRepository.deleteById(group.getId());
            }
            groupClusterRepository.deleteById(clusterId);
            return new CheckResult<>(HttpStatus.OK, "", null);
        } catch (Exception e) {
            return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting cluster", null);
        }
    }
}
