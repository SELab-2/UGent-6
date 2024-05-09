package com.ugent.pidgeon.util;


import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.*;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Class that converts entities to json objects
 */
@Component
public class EntityToJsonConverter {

    @Autowired
    private GroupClusterRepository groupClusterRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private CourseUserRepository courseUserRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
  @Autowired
  private ClusterUtil clusterUtil;


    public GroupJson groupEntityToJson(GroupEntity groupEntity) {
        GroupClusterEntity cluster = groupClusterRepository.findById(groupEntity.getClusterId()).orElse(null);
        GroupJson group = new GroupJson(cluster.getMaxSize(), groupEntity.getId(), groupEntity.getName(), ApiRoutes.CLUSTER_BASE_PATH + "/" + groupEntity.getClusterId());
        if (cluster != null && cluster.getGroupAmount() > 1){
            group.setGroupClusterUrl(ApiRoutes.CLUSTER_BASE_PATH + "/" + cluster.getId());
        } else {
            group.setGroupClusterUrl(null);
        }
        // Get the members of the group
        List<UserReferenceJson> members = groupRepository.findGroupUsersReferencesByGroupId(groupEntity.getId()).stream().map(user ->
                new UserReferenceJson(user.getName(), user.getEmail(), user.getUserId())
        ).toList();

        // Return the group with its members
        group.setMembers(members);
        return group;
    }


    public GroupClusterJson clusterEntityToClusterJson(GroupClusterEntity cluster) {
        List<GroupJson> groups = groupRepository.findAllByClusterId(cluster.getId()).stream().map(
                this::groupEntityToJson
        ).toList();
        return new GroupClusterJson(
                cluster.getId(),
                cluster.getName(),
                cluster.getMaxSize(),
                cluster.getGroupAmount(),
                cluster.getCreatedAt(),
                groups,
                ApiRoutes.COURSE_BASE_PATH + "/" + cluster.getCourseId()
        );
    }

    public UserReferenceJson userEntityToUserReference(UserEntity user) {
        return new UserReferenceJson(user.getName() + " " + user.getSurname(), user.getEmail(), user.getId());
    }

    public UserReferenceWithRelation userEntityToUserReferenceWithRelation(UserEntity user, CourseRelation relation) {
        return new UserReferenceWithRelation(userEntityToUserReference(user), relation.toString());
    }

    public CourseWithInfoJson courseEntityToCourseWithInfo(CourseEntity course, String joinLink, boolean hideKey) {
        UserEntity teacher = courseRepository.findTeacherByCourseId(course.getId());
        UserReferenceJson teacherJson = userEntityToUserReference(teacher);

        List<UserEntity> assistants = courseRepository.findAssistantsByCourseId(course.getId());
        List<UserReferenceJson> assistantsJson = assistants.stream().map(this::userEntityToUserReference).toList();

        return new CourseWithInfoJson(
                course.getId(),
                course.getName(),
                course.getDescription(),
                teacherJson,
                assistantsJson,
                ApiRoutes.COURSE_BASE_PATH + "/" + course.getId() + "/members",
                hideKey ? null : joinLink,
                hideKey ? null : course.getJoinKey(),
                course.getArchivedAt(),
                course.getCreatedAt(),
                course.getCourseYear()
        );
    }

    public CourseWithRelationJson courseEntityToCourseWithRelation(CourseEntity course, CourseRelation relation) {
        int memberCount = courseUserRepository.countUsersInCourse(course.getId());
        return new CourseWithRelationJson(
                ApiRoutes.COURSE_BASE_PATH + "/" + course.getId(),
                relation,
                course.getName(),
                course.getId(),
                course.getArchivedAt(),
                memberCount,
                course.getCreatedAt(),
                course.getCourseYear()
        );
    }

    public GroupFeedbackJson groupFeedbackEntityToJson(GroupFeedbackEntity groupFeedbackEntity) {
        return new GroupFeedbackJson(
                groupFeedbackEntity.getScore(),
                groupFeedbackEntity.getFeedback(),
                groupFeedbackEntity.getGroupId(),
                groupFeedbackEntity.getProjectId()
        );
    }

    public GroupFeedbackJsonWithProject groupFeedbackEntityToJsonWithProject(GroupFeedbackEntity groupFeedbackEntity, ProjectEntity project) {
        return new GroupFeedbackJsonWithProject(
                project.getName(),
                ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId(),
                project.getId(),
                groupFeedbackEntity == null ? null : groupFeedbackEntityToJson(groupFeedbackEntity),
                project.getMaxScore()
        );
    }

    public ProjectResponseJsonWithStatus projectEntityToProjectResponseJsonWithStatus(ProjectEntity project, CourseEntity course, UserEntity user) {
        // Get status
        Long groupId = groupRepository.groupIdByProjectAndUser(project.getId(), user.getId());
        if (groupId == null) {
            return new ProjectResponseJsonWithStatus(
                    projectEntityToProjectResponseJson(project, course, user),
                    "no group"
            );
        }
        SubmissionEntity sub = submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(project.getId(), groupId).orElse(null);
        String status;
        if (sub == null) {
            status = "not started";
        } else if (sub.getStructureAccepted() && sub.getStructureAccepted()) {
            status = "correct";
        } else {
            status = "incorrect";
        }


        return new ProjectResponseJsonWithStatus(
                projectEntityToProjectResponseJson(project, course, user),
                status
        );
    }

    public ProjectResponseJson projectEntityToProjectResponseJson(ProjectEntity project, CourseEntity course, UserEntity user) {
        // Calculate the progress of the project for all groups
        List<Long> groupIds = projectRepository.findGroupIdsByProjectId(project.getId());
        Integer total = groupIds.size();
        Integer completed = groupIds.stream().map(groupId -> {
            SubmissionEntity submission = submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(project.getId(), groupId).orElse(null);
            if (submission == null) {
                return 0;
            }

            if (submission.getDockerAccepted() && submission.getStructureAccepted()) return 1;
            return 0;
        }).reduce(0, Integer::sum);

        // Get the submissonUrl, depends on if the user is a course_admin or enrolled
        String submissionUrl = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/submissions";
        CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(course.getId(), user.getId())).orElse(null);
        if (courseUserEntity == null) {
            return null;
        }

        // GroupId is null if the user is a course_admin/creator
        Long groupId = groupRepository.groupIdByProjectAndUser(project.getId(), user.getId());

        if (courseUserEntity.getRelation() == CourseRelation.enrolled) {
            if (groupId == null) {
                submissionUrl = null;
            } else {
                submissionUrl += "/" + groupId;
            }
        }

        Long clusterId = project.getGroupClusterId();
        if (clusterUtil.isIndividualCluster(clusterId)) {
            clusterId = null;
        }
        return new ProjectResponseJson(
                courseEntityToCourseReference(course),
                project.getDeadline(),
                project.getDescription(),
                project.getId(),
                project.getName(),
                submissionUrl,
                project.getTestId() == null ? null : ApiRoutes.TEST_BASE_PATH + "/" + project.getTestId(),
                project.getMaxScore(),
                project.isVisible(),
                new ProjectProgressJson(completed, total),
                groupId,
                clusterId
        );
    }

    public CourseReferenceJson courseEntityToCourseReference(CourseEntity course) {
        return new CourseReferenceJson(
            course.getName(),
            ApiRoutes.COURSE_BASE_PATH + "/" + course.getId(),
            course.getId(),
            course.getArchivedAt()
        );
    }



    public SubmissionJson getSubmissionJson(SubmissionEntity submission) {
        return new SubmissionJson(
                submission.getId(),
                ApiRoutes.PROJECT_BASE_PATH + "/" + submission.getProjectId(),
                ApiRoutes.GROUP_BASE_PATH + "/" + submission.getGroupId(),
                submission.getProjectId(),
                submission.getGroupId(),
                ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId() + "/file",
                submission.getStructureAccepted(),
                submission.getSubmissionTime(),
                submission.getDockerAccepted(),
                ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId() + "/structurefeedback",
                ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId() + "/dockerfeedback",
                submission.getDockerTestState().toString()
        );
    }

    public TestJson testEntityToTestJson(TestEntity testEntity, long projectId) {
        return new TestJson(
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId,
                testEntity.getDockerImage(),
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId + "/tests/dockertest",
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId + "/tests/structuretest"
        );
    }
}