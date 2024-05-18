package com.ugent.pidgeon.util;


import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.*;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.DockerTestState;
import com.ugent.pidgeon.postgre.models.types.DockerTestType;
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
  @Autowired
  private TestUtil testUtil;
  @Autowired
  private TestRepository testRepository;
  @Autowired
  private FileRepository fileRepository;


  public GroupJson groupEntityToJson(GroupEntity groupEntity, boolean hideStudentNumber) {
        GroupClusterEntity cluster = groupClusterRepository.findById(groupEntity.getClusterId()).orElse(null);
        if (cluster == null) {
            throw new RuntimeException("Cluster not found");
        }
        GroupJson group = new GroupJson(cluster.getMaxSize(), groupEntity.getId(), groupEntity.getName(), ApiRoutes.CLUSTER_BASE_PATH + "/" + groupEntity.getClusterId());
        if (cluster.getMaxSize() > 1){
            group.setGroupClusterUrl(ApiRoutes.CLUSTER_BASE_PATH + "/" + cluster.getId());
        } else {
            group.setGroupClusterUrl(null);
        }
        // Get the members of the group
        List<UserReferenceJson> members = groupRepository.findGroupUsersReferencesByGroupId(groupEntity.getId()).stream().map(user ->
                new UserReferenceJson(user.getName(), user.getEmail(), user.getUserId(), hideStudentNumber ? null : user.getStudentNumber())
        ).toList();

        // Return the group with its members
        group.setMembers(members);
        return group;
    }


    public GroupClusterJson clusterEntityToClusterJson(GroupClusterEntity cluster, boolean hideStudentNumber) {
        List<GroupJson> groups = groupRepository.findAllByClusterId(cluster.getId()).stream().map(
                g -> groupEntityToJson(g, hideStudentNumber)
        ).toList();
        return new GroupClusterJson(
                cluster.getId(),
                cluster.getName(),
                cluster.getMaxSize(),
                cluster.getGroupAmount(),
                cluster.getCreatedAt(),
                groups,
                cluster.getLockGroupsAfter(),
                ApiRoutes.COURSE_BASE_PATH + "/" + cluster.getCourseId()
        );
    }

    public UserReferenceJson userEntityToUserReference(UserEntity user, boolean hideStudentNumber) {
        return new UserReferenceJson(
            user.getName() + " " + user.getSurname(),
            user.getEmail(), user.getId(),
            hideStudentNumber ? null : user.getStudentNumber()
        );
    }

    public UserReferenceWithRelation userEntityToUserReferenceWithRelation(UserEntity user, CourseRelation relation, boolean hideStudentNumber) {
        return new UserReferenceWithRelation(userEntityToUserReference(user, hideStudentNumber), relation.toString());
    }

    public CourseWithInfoJson courseEntityToCourseWithInfo(CourseEntity course, String joinLink, boolean hideKey) {
        UserEntity teacher = courseRepository.findTeacherByCourseId(course.getId());
        UserReferenceJson teacherJson = userEntityToUserReference(teacher, true);

        List<UserEntity> assistants = courseRepository.findAssistantsByCourseId(course.getId());
        List<UserReferenceJson> assistantsJson = assistants.stream().map(
            u -> userEntityToUserReference(u, true)
        ).toList();

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
                    ProjectStatus.no_group.toString()
            );
        }
        SubmissionEntity sub = submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(project.getId(), groupId).orElse(null);
        ProjectStatus status;
        if (sub == null) {
            status = ProjectStatus.not_started;
        } else if (sub.getStructureAccepted() && sub.getDockerAccepted()) {
            status = ProjectStatus.correct;
        } else {
            status = ProjectStatus.incorrect;
        }


        return new ProjectResponseJsonWithStatus(
                projectEntityToProjectResponseJson(project, course, user),
                status.toString()
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
            throw new RuntimeException("User not found in course");
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
                clusterId,
                project.getVisibleAfter()
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
      DockerTestFeedbackJson feedback;
        if (submission.getDockerTestState().equals(DockerTestState.running)) {
          feedback = null;
        } else if (submission.getDockerTestType().equals(DockerTestType.NONE)) {
          feedback =  new DockerTestFeedbackJson(DockerTestType.NONE, "", true);
        }
        else if (submission.getDockerTestType().equals(DockerTestType.SIMPLE)) {
          feedback = new DockerTestFeedbackJson(DockerTestType.SIMPLE, submission.getDockerFeedback(), submission.getDockerAccepted());
        } else {
          feedback = new DockerTestFeedbackJson(DockerTestType.TEMPLATE, submission.getDockerFeedback(), submission.getDockerAccepted());
        }
        return new SubmissionJson(
                submission.getId(),
                ApiRoutes.PROJECT_BASE_PATH + "/" + submission.getProjectId(),
                submission.getGroupId() == null ? null : ApiRoutes.GROUP_BASE_PATH + "/" + submission.getGroupId(),
                submission.getProjectId(),
                submission.getGroupId(),
                ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId() + "/file",
                submission.getStructureAccepted(),
                submission.getSubmissionTime(),
                submission.getStructureFeedback(),
                feedback,
                submission.getDockerTestState().toString(),
            ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId() + "/artifacts"
        );
    }

    public TestJson testEntityToTestJson(TestEntity testEntity, long projectId) {
        FileEntity extrafiles = testEntity.getExtraFilesId() == null ? null : fileRepository.findById(testEntity.getExtraFilesId()).orElse(null);
        return new TestJson(
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId,
            testEntity.getDockerImage(),
            testEntity.getDockerTestScript(),
            testEntity.getDockerTestTemplate(),
            testEntity.getStructureTemplate(),
            testEntity.getExtraFilesId() == null ? null : ApiRoutes.PROJECT_BASE_PATH + "/" + projectId + "/tests/extrafiles",
            extrafiles == null ? null : extrafiles.getName()
        );
    }
}