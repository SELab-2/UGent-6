package com.ugent.pidgeon.util;


import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.*;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.Check;
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
  @Autowired
  private FileRepository fileRepository;
  @Autowired
  private CourseRepository courseRepository;
  @Autowired
  private CourseUserRepository courseUserRepository;


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
     * @param user user to add to the group
     * @return true if the group was created successfully
     */
    public boolean createNewIndividualClusterGroup(long courseId, UserEntity user) {
        GroupClusterEntity groupClusterEntity = groupClusterRepository.findIndividualClusterByCourseId(courseId).orElse(null);
        if (groupClusterEntity == null) {
            return false;
        }
        // Create new group for the cluster
        GroupEntity groupEntity = new GroupEntity(user.getName() + " " + user.getSurname(), groupClusterEntity.getId());
        groupClusterEntity.setGroupAmount(groupClusterEntity.getGroupAmount() + 1);
        groupClusterRepository.save(groupClusterEntity);
        groupEntity = groupRepository.save(groupEntity);

        // Add user to the group
        GroupUserEntity groupUserEntity = new GroupUserEntity(groupEntity.getId(), user.getId());
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

            if (projectEntity.getTestId() != null) {
                TestEntity testEntity = testRepository.findById(projectEntity.getTestId()).orElse(null);
                if (testEntity == null) {
                    return new CheckResult<>(HttpStatus.NOT_FOUND, "Test not found", null);
                }
                CheckResult<Void> delRes =  deleteTestById(projectEntity, testEntity);
                return delRes;
            }

            projectRepository.delete(projectEntity);

            return new CheckResult<>(HttpStatus.OK, "", null);
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
        projectEntity.setTestId(null);
        projectRepository.save(projectEntity);
        testRepository.deleteById(testEntity.getId());
        if(!testRepository.imageIsUsed(testEntity.getDockerImage())){
            DockerSubmissionTestModel.removeDockerImage(testEntity.getDockerImage());
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
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


    /**
     * Copy a course and all its related data. Assumes that permissions are already checked
     * @param course course to copy
     * @return CheckResult with the status of the copy and the new course
     */
    public CheckResult<CourseEntity> copyCourse(CourseEntity course, long userId) {
        // Copy the course
        CourseEntity newCourse = new CourseEntity(course.getName(), course.getDescription(), course.getCourseYear());
        // Change the createdAt, archivedAt and joinKey
        newCourse.setCreatedAt(OffsetDateTime.now());
        newCourse.setArchivedAt(null);
        newCourse.setJoinKey(UUID.randomUUID().toString());

        newCourse = courseRepository.save(newCourse);

        Map<Long, Long> groupClusterMap = new HashMap<>();
        // Copy the group(clusters) linked to the course
        GroupClusterEntity groupCluster = groupClusterRepository.findIndividualClusterByCourseId(
            course.getId()).orElse(null);
        if (groupCluster != null) {
            CheckResult<GroupClusterEntity> checkResult = copyGroupCluster(groupCluster, newCourse.getId(), false);
            if (!checkResult.getStatus().equals(HttpStatus.OK)) {
                return new CheckResult<>(checkResult.getStatus(), checkResult.getMessage(), null);
            }
            groupClusterMap.put(groupCluster.getId(), checkResult.getData().getId());
        } else {
            return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error while copying course", null);
        }

        List<GroupClusterEntity> groupClusters = groupClusterRepository.findClustersWithoutInvidualByCourseId(course.getId());
        for (GroupClusterEntity cluster : groupClusters) {
            CheckResult<GroupClusterEntity> checkResult = copyGroupCluster(cluster,
                newCourse.getId(), true);
            if (!checkResult.getStatus().equals(HttpStatus.OK)) {
                return new CheckResult<>(checkResult.getStatus(), checkResult.getMessage(), null);
            }
            groupClusterMap.put(cluster.getId(), checkResult.getData().getId());
        }

        // Copy the projects linked to the course
        List<ProjectEntity> projects = projectRepository.findByCourseId(course.getId());
        for (ProjectEntity project : projects) {
            CheckResult<ProjectEntity> checkResult = copyProject(project, newCourse.getId(), groupClusterMap.get(project.getGroupClusterId()));
            if (!checkResult.getStatus().equals(HttpStatus.OK)) {
                return new CheckResult<>(checkResult.getStatus(), checkResult.getMessage(), null);
            }
        }

        // Add user to course
        CourseUserEntity courseUserEntity = new CourseUserEntity(newCourse.getId(), userId, CourseRelation.creator);
        courseUserRepository.save(courseUserEntity);

        return new CheckResult<>(HttpStatus.OK, "", newCourse);
    }

    /**
     * Copy a group cluster and all its related data. Assumes that permissions are already checked
     * @param groupCluster group cluster that needs to be copied
     * @return CheckResult with the status of the copy and the new group cluster
     */
    public CheckResult<GroupClusterEntity> copyGroupCluster(GroupClusterEntity groupCluster, long courseId, boolean copyGroups) {
        GroupClusterEntity newGroupCluster = new GroupClusterEntity(
            courseId,
            groupCluster.getMaxSize(),
            groupCluster.getName(),
            groupCluster.getGroupAmount()
        );
        newGroupCluster.setCreatedAt(OffsetDateTime.now());

        newGroupCluster = groupClusterRepository.save(newGroupCluster);
        if (copyGroups) {
            List<GroupEntity> groups = groupRepository.findAllByClusterId(groupCluster.getId());
            for (GroupEntity group : groups) {
                GroupEntity newGroup = new GroupEntity(group.getName(), newGroupCluster.getId());
                groupRepository.save(newGroup);
            }
        }

        return new CheckResult<>(HttpStatus.OK, "", newGroupCluster);
    }



    /**
     * Copy a project and all its related data. Assumes that permissions are already checked
     * @param project project that needs to be copied
     * @param courseId id of the course the project is linked to
     * @param clusterId id of the cluster the project is linked to
     * @return CheckResult with the status of the copy and the new project
     */
    public CheckResult<ProjectEntity> copyProject(ProjectEntity project, long courseId, long clusterId) {
        // Copy the project
        ProjectEntity newProject = new ProjectEntity(
            courseId,
            project.getName(),
            project.getDescription(),
            clusterId,
            null,
            project.isVisible(),
            project.getMaxScore(),
            project.getDeadline());

        newProject = projectRepository.save(newProject);


        // Copy the test linked to the project
        if (project.getTestId() != null) {
            TestEntity test = testRepository.findById(project.getTestId()).orElse(null);
            if (test != null) {
                CheckResult<TestEntity> checkResult = copyTest(test);
                if (!checkResult.getStatus().equals(HttpStatus.OK)) {
                    return new CheckResult<>(checkResult.getStatus(), checkResult.getMessage(), null);
                }
                newProject.setTestId(checkResult.getData().getId());
                newProject = projectRepository.save(newProject);
            } else {
                return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error while copying project", null);
            }
        }


        return new CheckResult<>(HttpStatus.OK, "", newProject);
    }

    /**
     * Copy a test and all its related data. Assumes that permissions are already checked and that the parameters are valid.
     * @param test test that needs to be copied
     * @return CheckResult with the status of the copy and the new test
     */
    public CheckResult<TestEntity> copyTest(TestEntity test) {
        // Copy the test
        TestEntity newTest = new TestEntity(
            test.getDockerImage(),
            test.getDockerTestScript(),
            test.getDockerTestTemplate(),
            test.getStructureTemplate()
        );

        newTest = testRepository.save(newTest);
        return new CheckResult<>(HttpStatus.OK, "", newTest);
    }
}
