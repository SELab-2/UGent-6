package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseJson;
import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.CourseWithInfoJson;
import com.ugent.pidgeon.model.json.CourseWithRelationJson;
import com.ugent.pidgeon.model.json.GroupClusterJson;
import com.ugent.pidgeon.model.json.GroupFeedbackJson;
import com.ugent.pidgeon.model.json.GroupFeedbackJsonWithProject;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;
import com.ugent.pidgeon.model.json.ProjectResponseJsonWithStatus;
import com.ugent.pidgeon.model.json.ProjectStatus;
import com.ugent.pidgeon.model.json.SubmissionJson;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.model.json.UserReferenceWithRelation;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.DockerTestState;
import com.ugent.pidgeon.postgre.models.types.DockerTestType;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.postgre.repository.GroupRepository.UserReference;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EntityToJsonConverterTest {

  @Mock
  private GroupClusterRepository groupClusterRepository;

  @Mock
  private ClusterUtil clusterUtil;

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private CourseRepository courseRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private CourseUserRepository courseUserRepository;

  @Mock
  private SubmissionRepository submissionRepository;

  @Mock
  private FileRepository fileRepository;

  @Spy
  @InjectMocks
  private EntityToJsonConverter entityToJsonConverter;

  private GroupClusterEntity groupClusterEntity;
  private GroupEntity groupEntity;
  private UserEntity userEntity;
  private UserEntity otherUser;
  private CourseEntity courseEntity;
  private ProjectEntity projectEntity;
  private GroupFeedbackEntity groupFeedbackEntity;
  private SubmissionEntity submissionEntity;
  private TestEntity testEntity;


  private GroupJson groupJson;
  private UserReferenceJson userReferenceJson;
  private UserReferenceJson otherUserReferenceJson;
  private GroupFeedbackJson groupFeedbackJson;
  private ProjectResponseJson projectResponseJson;
  private CourseReferenceJson courseJson;

  @BeforeEach
  public void setUp() {
    courseEntity = new CourseEntity("name", "description",2024);
    courseEntity.setJoinKey("joinKey");
    courseEntity.setId(9L);

    groupClusterEntity = new GroupClusterEntity(
        courseEntity.getId(),
        20,
        "clusterName",
        5
    );
    groupClusterEntity.setGroupAmount(5);
    groupClusterEntity.setId(9L);

    groupEntity = new GroupEntity(
        "groupName",
        groupClusterEntity.getId()
    );
    groupEntity.setId(4L);

    groupJson = new GroupJson(
        20,
        4L,
        "groupName",
        ApiRoutes.CLUSTER_BASE_PATH + "/" + groupClusterEntity.getId()
    );

    userEntity = new UserEntity(
        "name",
        "surname",
        "email",
        UserRole.student,
        "azureId"
    );
    userEntity.setId(44L);
    userReferenceJson = new UserReferenceJson(
        userEntity.getName() + " " + userEntity.getSurname(),
        userEntity.getEmail(),
        userEntity.getId()
    );

    otherUser = new UserEntity(
        "otherName",
        "otherSurname",
        "otherEmail",
        UserRole.student,
        "otherAzureId"
    );
    otherUserReferenceJson = new UserReferenceJson(
        otherUser.getName() + " " + otherUser.getSurname(),
        otherUser.getEmail(),
        otherUser.getId()
    );



    testEntity = new TestEntity(
        "dockerImageBasic",
        "dockerTestScriptBasic",
        "dockerTestTemplateBasic",
        "structureTemplateBasic"
    );
    testEntity.setId(38L);

    projectEntity = new ProjectEntity(
        courseEntity.getId(),
        "projectName",
        "projectDescription",
        groupClusterEntity.getId(),
        testEntity.getId(),
        true,
        34,
        OffsetDateTime.now()
    );
    projectEntity.setId(64);

    courseJson = new CourseReferenceJson(courseEntity.getName(), "courseUrl", courseEntity.getId(), null);


    projectResponseJson = new ProjectResponseJson(
        courseJson,
        projectEntity.getDeadline(),
        projectEntity.getDescription(),
        projectEntity.getId(),
        projectEntity.getName(),
        "SubmissionURL",
        "TestURL",
        projectEntity.getMaxScore(),
        projectEntity.isVisible(),
        new ProjectProgressJson(44, 60),
        groupEntity.getId(),
        groupClusterEntity.getId()
    );

    groupFeedbackEntity = new GroupFeedbackEntity(
        groupEntity.getId(),
        projectEntity.getId(),
        5.0f,
        "feedback"
    );

    groupFeedbackJson = new GroupFeedbackJson(
        groupFeedbackEntity.getScore(),
        groupFeedbackEntity.getFeedback(),
        groupFeedbackEntity.getGroupId(),
        groupFeedbackEntity.getProjectId()
    );

    submissionEntity = new SubmissionEntity(
        22,
        45,
        99L,
        OffsetDateTime.MIN,
        true,
        true
    );
  }

  @Test
  public void testGroupEntityToJson() {
    when(groupClusterRepository.findById(groupEntity.getClusterId())).thenReturn(Optional.of(groupClusterEntity));
    when(groupRepository.findGroupUsersReferencesByGroupId(anyLong())).thenReturn(
        List.of(new UserReference[]{
          new UserReference() {
              @Override
              public Long getUserId() {
                return userEntity.getId();
              }

              @Override
              public String getName() {
                return userEntity.getName() + " " + userEntity.getSurname();
              }

              @Override
              public String getEmail() {
                return userEntity.getEmail();
              }
            }

        })
    );
    GroupJson result = entityToJsonConverter.groupEntityToJson(groupEntity);
    assertEquals(groupClusterEntity.getMaxSize(), result.getCapacity());
    assertEquals(groupEntity.getId(), result.getGroupId());
    assertEquals(groupEntity.getName(), result.getName());
    assertEquals(ApiRoutes.CLUSTER_BASE_PATH + "/" + groupClusterEntity.getId(), result.getGroupClusterUrl());
    assertEquals(1, result.getMembers().size());
    UserReferenceJson userReferenceJson = result.getMembers().get(0);
    assertEquals(userEntity.getId(), userReferenceJson.getUserId());
    assertEquals(userEntity.getName() + " " + userEntity.getSurname(), userReferenceJson.getName());
    assertEquals(userEntity.getEmail(), userReferenceJson.getEmail());

    /* Cluster is individual */
    groupClusterEntity.setMaxSize(1);
    result = entityToJsonConverter.groupEntityToJson(groupEntity);
    assertEquals(1, result.getCapacity());
    assertNull(result.getGroupClusterUrl());

    /* Issue when groupClusterEntity is null */
    when(groupClusterRepository.findById(groupEntity.getClusterId())).thenReturn(Optional.empty());
    assertThrows(RuntimeException.class, () -> entityToJsonConverter.groupEntityToJson(groupEntity));

  }

  @Test
  public void testClusterEntityToClusterJson() {
    when(groupRepository.findAllByClusterId(groupClusterEntity.getId())).thenReturn(List.of(groupEntity));
    doReturn(groupJson).when(entityToJsonConverter).groupEntityToJson(groupEntity);

    GroupClusterJson result = entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity);

    assertEquals(groupClusterEntity.getId(), result.clusterId());
    assertEquals(groupClusterEntity.getName(), result.name());
    assertEquals(groupClusterEntity.getMaxSize(), result.capacity());
    assertEquals(groupClusterEntity.getGroupAmount(), result.groupCount());
    assertEquals(groupClusterEntity.getCreatedAt(), result.createdAt());
    assertEquals(1, result.groups().size());
    assertEquals(groupJson, result.groups().get(0));
    assertEquals(ApiRoutes.COURSE_BASE_PATH + "/" + courseEntity.getId(), result.courseUrl());
  }

  @Test
  public void testUserEntityToUserReference() {
    UserReferenceJson result = entityToJsonConverter.userEntityToUserReference(userEntity);
    assertEquals(userEntity.getId(), result.getUserId());
    assertEquals(userEntity.getName() + " " + userEntity.getSurname(), result.getName());
    assertEquals(userEntity.getEmail(), result.getEmail());
  }

  @Test
  public void testUserEntityToUserReferenceWithRelation() {
    doReturn(userReferenceJson).when(entityToJsonConverter).userEntityToUserReference(userEntity);
    UserReferenceWithRelation result = entityToJsonConverter.userEntityToUserReferenceWithRelation(userEntity, CourseRelation.creator);
    assertEquals(userReferenceJson, result.getUser());
    assertEquals(CourseRelation.creator.toString(), result.getRelation());

    result = entityToJsonConverter.userEntityToUserReferenceWithRelation(userEntity, CourseRelation.course_admin);
    assertEquals(CourseRelation.course_admin.toString(), result.getRelation());

    result = entityToJsonConverter.userEntityToUserReferenceWithRelation(userEntity, CourseRelation.enrolled);
    assertEquals(CourseRelation.enrolled.toString(), result.getRelation());
  }

  @Test
  public void testCourseEntityToCourseWithInfo() {
    String joinLink = "JOIN LINK";
    courseEntity.setArchivedAt(OffsetDateTime.now());
    courseEntity.setCreatedAt(OffsetDateTime.MIN);

    when(courseRepository.findTeacherByCourseId(courseEntity.getId())).thenReturn(userEntity);
    when(courseRepository.findAssistantsByCourseId(courseEntity.getId())).thenReturn(List.of(otherUser));

    doReturn(userReferenceJson).when(entityToJsonConverter).userEntityToUserReference(userEntity);
    doReturn(otherUserReferenceJson).when(entityToJsonConverter).userEntityToUserReference(otherUser);

    CourseWithInfoJson result = entityToJsonConverter.courseEntityToCourseWithInfo(courseEntity, joinLink, false);
    assertEquals(courseEntity.getId(), result.courseId());
    assertEquals(courseEntity.getName(), result.name());
    assertEquals(courseEntity.getDescription(), result.description());
    assertEquals(userReferenceJson, result.teacher());
    assertEquals(List.of(otherUserReferenceJson), result.assistants());
    assertEquals(joinLink, result.joinUrl());
    assertEquals(courseEntity.getJoinKey(), result.joinKey());
    assertEquals(courseEntity.getArchivedAt().toInstant(), result.archivedAt().toInstant());
    assertEquals(courseEntity.getCreatedAt().toInstant(), result.createdAt().toInstant());
    assertEquals(courseEntity.getCourseYear(), result.year());

    /* Hide key */
    result = entityToJsonConverter.courseEntityToCourseWithInfo(courseEntity, joinLink, true);
    assertNull(result.joinKey());
    assertNull(result.joinUrl());

  }

  @Test
  public void testCourseEntityToCourseWithRelation() {

    int userCount = 5;
    courseEntity.setArchivedAt(OffsetDateTime.now());
    courseEntity.setCreatedAt(OffsetDateTime.MIN);

    when(courseUserRepository.countUsersInCourse(courseEntity.getId())).thenReturn(userCount);
    CourseWithRelationJson result = entityToJsonConverter.courseEntityToCourseWithRelation(courseEntity, CourseRelation.creator);
    assertEquals(ApiRoutes.COURSE_BASE_PATH + "/" + courseEntity.getId(), result.url());
    assertEquals(CourseRelation.creator, result.relation());
    assertEquals(courseEntity.getName(), result.name());
    assertEquals(courseEntity.getId(), result.courseId());
    assertEquals(courseEntity.getArchivedAt().toInstant(), result.archivedAt().toInstant());
    assertEquals(userCount, result.memberCount());
    assertEquals(courseEntity.getCreatedAt().toInstant(), result.createdAt().toInstant());
    assertEquals(courseEntity.getCourseYear(), result.year());

  }

  @Test
  public void testGroupFeedbackEntityToJson() {
    GroupFeedbackJson result = entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity);
    assertEquals(groupFeedbackEntity.getScore(), result.getScore());
    assertEquals(groupFeedbackEntity.getFeedback(), result.getFeedback());
    assertEquals(groupFeedbackEntity.getGroupId(), result.getGroupId());
    assertEquals(groupFeedbackEntity.getProjectId(), result.getProjectId());
  }

  @Test
  public void testGroupFeedbackEntityToJsonWithProjec() {
    doReturn(groupFeedbackJson).when(entityToJsonConverter).groupFeedbackEntityToJson(groupFeedbackEntity);
    GroupFeedbackJsonWithProject result = entityToJsonConverter.groupFeedbackEntityToJsonWithProject(groupFeedbackEntity, projectEntity);
    assertEquals(projectEntity.getName(), result.getProjectName());
    assertEquals(ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId(), result.getProjectUrl());
    assertEquals(projectEntity.getId(), result.getProjectId());
    assertEquals(groupFeedbackJson, result.getGroupFeedback());
    assertEquals(projectEntity.getMaxScore().intValue(), result.getMaxScore());

    /* No feedback */
    result = entityToJsonConverter.groupFeedbackEntityToJsonWithProject(null, projectEntity);
    assertNull(result.getGroupFeedback());
  }

  @Test
  public void testProjectEntityToProjectResponseJsonWithStatus() {
    submissionEntity.setDockerAccepted(true);
    submissionEntity.setStructureAccepted(true);
    when(groupRepository.groupIdByProjectAndUser(projectEntity.getId(), userEntity.getId())).thenReturn(groupEntity.getId());
    when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(projectEntity.getId(), groupEntity.getId())).thenReturn(Optional.of(submissionEntity));

    doReturn(projectResponseJson).when(entityToJsonConverter).projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
    ProjectResponseJsonWithStatus result = entityToJsonConverter.projectEntityToProjectResponseJsonWithStatus(projectEntity, courseEntity, userEntity);
    assertEquals(projectResponseJson, result.project());
    assertEquals(ProjectStatus.correct.toString(),  result.status());

    /* Check different statuses */

    submissionEntity.setDockerAccepted(false);
    result = entityToJsonConverter.projectEntityToProjectResponseJsonWithStatus(projectEntity, courseEntity, userEntity);
    assertEquals(ProjectStatus.incorrect.toString(),  result.status());

    submissionEntity.setDockerAccepted(true);
    submissionEntity.setStructureAccepted(false);
    result = entityToJsonConverter.projectEntityToProjectResponseJsonWithStatus(projectEntity, courseEntity, userEntity);
    assertEquals(ProjectStatus.incorrect.toString(),  result.status());

    when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(projectEntity.getId(), groupEntity.getId())).thenReturn(Optional.empty());
    result = entityToJsonConverter.projectEntityToProjectResponseJsonWithStatus(projectEntity, courseEntity, userEntity);
    assertEquals(ProjectStatus.not_started.toString(),  result.status());

    /* User not in group yet */
    when(groupRepository.groupIdByProjectAndUser(projectEntity.getId(), userEntity.getId())).thenReturn(null);
    result = entityToJsonConverter.projectEntityToProjectResponseJsonWithStatus(projectEntity, courseEntity, userEntity);
    assertEquals(ProjectStatus.no_group.toString(),  result.status());
  }

  @Test
  public void testProjectEntityToProjectResponseJson() {
    GroupEntity secondGroup = new GroupEntity("secondGroup", groupClusterEntity.getId());
    SubmissionEntity secondSubmission = new SubmissionEntity(22, 232, 90L, OffsetDateTime.MIN, true, true);
    CourseUserEntity courseUser = new CourseUserEntity(projectEntity.getCourseId(), userEntity.getId(), CourseRelation.creator);

    when(projectRepository.findGroupIdsByProjectId(projectEntity.getId())).thenReturn(List.of(groupEntity.getId(), secondGroup.getId()));
    when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(projectEntity.getId(), groupEntity.getId())).thenReturn(Optional.of(submissionEntity));
    when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(projectEntity.getId(), secondGroup.getId())).thenReturn(Optional.of(secondSubmission));
    when(courseUserRepository.findById(argThat(
        id -> id.getCourseId() == projectEntity.getCourseId() && id.getUserId() == userEntity.getId()
    ))).thenReturn(Optional.of(courseUser));

    when(groupRepository.groupIdByProjectAndUser(projectEntity.getId(), userEntity.getId())).thenReturn(null);
    when(clusterUtil.isIndividualCluster(projectEntity.getGroupClusterId())).thenReturn(false);

    doReturn(courseJson).when(entityToJsonConverter).courseEntityToCourseReference(courseEntity);

    ProjectResponseJson result = entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
    assertEquals(courseJson, result.course());
    assertEquals(projectEntity.getDeadline(), result.deadline());
    assertEquals(projectEntity.getDescription(), result.description());
    assertEquals(projectEntity.getId(), result.projectId());
    assertEquals(projectEntity.getName(), result.name());
    assertEquals(ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId() + "/submissions", result.submissionUrl());
    assertEquals(ApiRoutes.TEST_BASE_PATH + "/" + projectEntity.getTestId(), result.testUrl());
    assertEquals(projectEntity.getMaxScore(), result.maxScore());
    assertEquals(projectEntity.isVisible(), result.visible());
    assertEquals(2, result.progress().completed());
    assertEquals(2, result.progress().total());
    assertNull(result.groupId()); // User is a creator/course_admin -> no group
    assertEquals(groupClusterEntity.getId(), result.clusterId());

    /* TestId is null */
    projectEntity.setTestId(null);
    result = entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
    assertNull(result.testUrl());

    /* Individual cluster */
    when(clusterUtil.isIndividualCluster(projectEntity.getGroupClusterId())).thenReturn(true);
    result = entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
    assertNull(result.clusterId());

    /* User is enrolled and in group */
    courseUser.setRelation(CourseRelation.enrolled);
    when(groupRepository.groupIdByProjectAndUser(projectEntity.getId(), userEntity.getId())).thenReturn(groupEntity.getId());
    result = entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
    assertEquals(ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId() + "/submissions/" + groupEntity.getId(), result.submissionUrl());
    assertEquals(groupEntity.getId(), result.groupId());

    /* User is enrolled but not in group */
    when(groupRepository.groupIdByProjectAndUser(projectEntity.getId(), userEntity.getId())).thenReturn(null);
    result = entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
    assertNull(result.submissionUrl());

    /* One submission is not correct */
    secondSubmission.setDockerAccepted(false);
    result = entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
    assertEquals(1, result.progress().completed());
    assertEquals(2, result.progress().total());

    secondSubmission.setDockerAccepted(true);
    secondSubmission.setStructureAccepted(false);
    result = entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
    assertEquals(1, result.progress().completed());
    assertEquals(2, result.progress().total());

    /* One group didn't make a submission yet */
    when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(projectEntity.getId(), secondGroup.getId())).thenReturn(Optional.empty());
    result = entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
    assertEquals(1, result.progress().completed());
    assertEquals(2, result.progress().total());

    /* Error while getting courseUser */
    reset(courseUserRepository);
    when(courseUserRepository.findById(argThat(
        id -> id.getCourseId() == projectEntity.getCourseId() && id.getUserId() == userEntity.getId()
    ))).thenReturn(Optional.empty());
    assertThrows(RuntimeException.class, () -> entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity));
  }

  @Test
  public void testCourseEntityToCourseReference() {
    CourseReferenceJson result = entityToJsonConverter.courseEntityToCourseReference(courseEntity);
    assertEquals(courseEntity.getName(), result.getName());
    assertEquals(ApiRoutes.COURSE_BASE_PATH + "/" + courseEntity.getId(), result.getUrl());
    assertEquals(courseEntity.getId(), result.getCourseId());
    assertNull(result.getArchivedAt());
  }

  @Test
  public void testGetSubmissionJson() {
    submissionEntity.setDockerTestState(DockerTestState.running);
    submissionEntity.setSubmissionTime(OffsetDateTime.now());
    submissionEntity.setStructureAccepted(true);
    submissionEntity.setStructureFeedback("feedback");
    SubmissionJson result = entityToJsonConverter.getSubmissionJson(submissionEntity);
    assertEquals(submissionEntity.getId(), result.getSubmissionId());
    assertEquals(ApiRoutes.PROJECT_BASE_PATH + "/" + submissionEntity.getProjectId(), result.getProjectUrl());
    assertEquals(ApiRoutes.GROUP_BASE_PATH + "/" + submissionEntity.getGroupId(), result.getGroupUrl());
    assertEquals(submissionEntity.getProjectId(), result.getProjectId());
    assertEquals(submissionEntity.getGroupId(), result.getGroupId());
    assertEquals(ApiRoutes.SUBMISSION_BASE_PATH + "/" + submissionEntity.getId() + "/file", result.getFileUrl());
    assertTrue(result.getStructureAccepted());
    assertEquals(submissionEntity.getSubmissionTime(), result.getSubmissionTime());
    assertEquals(submissionEntity.getStructureFeedback(), result.getStructureFeedback());
    assertNull(result.getDockerFeedback());
    assertEquals(DockerTestState.running.toString(), result.getDockerStatus());
    assertEquals(ApiRoutes.SUBMISSION_BASE_PATH + "/" + submissionEntity.getId() + "/artifacts", result.getArtifactUrl());

    /* Docker finished running */
    submissionEntity.setDockerTestState(DockerTestState.finished);
    /* No docker test */
    submissionEntity.setDockerType(DockerTestType.NONE);
    result = entityToJsonConverter.getSubmissionJson(submissionEntity);
    assertEquals(DockerTestState.finished.toString(), result.getDockerStatus());
    assertEquals(DockerTestType.NONE, result.getDockerFeedback().type());

    /* Simple docker test */
    submissionEntity.setDockerFeedback("dockerFeedback - simple");
    submissionEntity.setDockerAccepted(true);
    submissionEntity.setDockerType(DockerTestType.SIMPLE);
    result = entityToJsonConverter.getSubmissionJson(submissionEntity);
    assertEquals(DockerTestType.SIMPLE, result.getDockerFeedback().type());
    assertEquals(submissionEntity.getDockerFeedback(), result.getDockerFeedback().feedback());
    assertTrue(result.getDockerFeedback().allowed());

    /* Template docker test */
    submissionEntity.setDockerFeedback("dockerFeedback - template");
    submissionEntity.setDockerAccepted(false);
    submissionEntity.setDockerType(DockerTestType.TEMPLATE);
    result = entityToJsonConverter.getSubmissionJson(submissionEntity);
    assertEquals(DockerTestType.TEMPLATE, result.getDockerFeedback().type());
    assertEquals(submissionEntity.getDockerFeedback(), result.getDockerFeedback().feedback());
    assertFalse(result.getDockerFeedback().allowed());

    /* Docker aborted */
    submissionEntity.setDockerTestState(DockerTestState.aborted);
    result = entityToJsonConverter.getSubmissionJson(submissionEntity);
    assertEquals(DockerTestState.aborted.toString(), result.getDockerStatus());
    assertEquals(DockerTestType.TEMPLATE, result.getDockerFeedback().type());
    assertEquals(submissionEntity.getDockerFeedback(), result.getDockerFeedback().feedback());
    assertFalse(result.getDockerFeedback().allowed());
  }

  @Test
  public void testTestEntityToTestJson() {
    testEntity.setExtraFilesId(5L);
    when(fileRepository.findById(testEntity.getExtraFilesId()))
        .thenReturn(Optional.of(new FileEntity("nameoffiles", "path", 5L)));

    TestJson result = entityToJsonConverter.testEntityToTestJson(testEntity, projectEntity.getId());


    assertEquals(ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId(), result.getProjectUrl());
    assertEquals(testEntity.getDockerImage(), result.getDockerImage());
    assertEquals(testEntity.getDockerTestScript(), result.getDockerScript());
    assertEquals(testEntity.getDockerTestTemplate(), result.getDockerTemplate());
    assertEquals(testEntity.getStructureTemplate(), result.getStructureTest());
    assertEquals(ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId() + "/tests/extrafiles", result.getExtraFilesUrl());
    assertEquals("nameoffiles", result.getExtraFilesName());

    testEntity.setExtraFilesId(null);
    result = entityToJsonConverter.testEntityToTestJson(testEntity, projectEntity.getId());
    assertNull(result.getExtraFilesUrl());
    assertNull(result.getExtraFilesName());
  }


}