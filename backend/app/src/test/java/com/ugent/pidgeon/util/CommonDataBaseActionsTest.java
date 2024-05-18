package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.GroupUserRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class CommonDataBaseActionsTest {

  @Mock
  private GroupRepository groupRepository;
  @Mock
  private GroupClusterRepository groupClusterRepository;
  @Mock
  private GroupUserRepository groupUserRepository;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private GroupFeedbackRepository groupFeedbackRepository;
  @Mock
  private SubmissionRepository submissionRepository;
  @Mock
  private TestRepository testRepository;
  @Mock
  private CourseUserRepository courseUserRepository;
  @Mock
  private CourseRepository courseRepository;

  @Mock
  private FileUtil fileUtil;

  @Spy
  @InjectMocks
  private CommonDatabaseActions commonDatabaseActions;

  private GroupClusterEntity groupClusterEntity;
  private GroupEntity groupEntity;
  private UserEntity userEntity;
  private CourseEntity courseEntity;
  private ProjectEntity projectEntity;
  private GroupFeedbackEntity groupFeedbackEntity;
  private SubmissionEntity submissionEntity;
  private TestEntity testEntity;


  @BeforeEach
  public void setUp() {
    courseEntity = new CourseEntity("name", "description",2024);
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
    groupEntity.setClusterId(groupClusterEntity.getId());

    userEntity = new UserEntity();
    userEntity.setId(44L);



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

    groupFeedbackEntity = new GroupFeedbackEntity(
        groupEntity.getId(),
        projectEntity.getId(),
        5.0f,
        "feedback"
    );

    submissionEntity = new SubmissionEntity(
        22,
        45L,
        99L,
        OffsetDateTime.MIN,
        true,
        true
    );



  }

  @Test
  public void removeGroup() {
    long groupId = groupEntity.getId();
    int originalGroupCount = groupClusterEntity.getGroupAmount();

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));
    when(groupClusterRepository.findById(groupEntity.getClusterId())).thenReturn(Optional.of(groupClusterEntity));

    assertTrue(commonDatabaseActions.removeGroup(groupId));
    verify(groupRepository, times(1)).deleteGroupUsersByGroupId(groupId);
    verify(groupRepository, times(1)).deleteSubmissionsByGroupId(groupId);
    verify(groupRepository, times(1)).deleteGroupFeedbacksByGroupId(groupId);
    verify(groupRepository, times(1)).deleteById(groupId);
    verify(groupClusterRepository, times(1)).save(groupClusterEntity);

    assertEquals(originalGroupCount - 1, groupClusterEntity.getGroupAmount());

    /* Group not found */
    when(groupRepository.findById(groupId)).thenReturn(Optional.empty());
    assertTrue(commonDatabaseActions.removeGroup(groupId));
    verify(groupRepository, times(1)).deleteGroupUsersByGroupId(groupId);
    verify(groupRepository, times(1)).deleteSubmissionsByGroupId(groupId);
    verify(groupRepository, times(1)).deleteGroupFeedbacksByGroupId(groupId);
    verify(groupRepository, times(1)).deleteById(groupId);
    verify(groupClusterRepository, times(1)).save(groupClusterEntity);

    assertEquals(originalGroupCount - 1, groupClusterEntity.getGroupAmount());

    /* Unexpected error */
    when(groupRepository.findById(groupId)).thenThrow(new RuntimeException());
    assertFalse(commonDatabaseActions.removeGroup(groupId));
  }

  @Test
  public void testCreateNewIndividualClusterGroup () {
    int originalGroupCount = groupClusterEntity.getGroupAmount();

    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(
        Optional.of(groupClusterEntity));
    when(groupRepository.save(argThat(
        group ->
            group.getClusterId() == groupClusterEntity.getId() &&
            group.getName().equals(userEntity.getName() + " " + userEntity.getSurname())
    ))).thenReturn(groupEntity);
    assertTrue(
        commonDatabaseActions.createNewIndividualClusterGroup(courseEntity.getId(), userEntity));

    verify(groupClusterRepository, times(1)).save(groupClusterEntity);
    verify(groupUserRepository, times(1)).save(argThat(
        groupUser ->
            groupUser.getGroupId() == groupEntity.getId() &&
                groupUser.getUserId() == userEntity.getId()
    ));
    assertEquals(originalGroupCount + 1, groupClusterEntity.getGroupAmount());

    /* Group cluster not found */
    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(
        Optional.empty());
    assertFalse(commonDatabaseActions.createNewIndividualClusterGroup(courseEntity.getId(), userEntity));
  }

  @Test
  public void testRemoveIndividualClusterGroup() {
    long groupId = groupEntity.getId();
    int originalGroupCount = groupClusterEntity.getGroupAmount();

    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(
        Optional.of(groupClusterEntity));
    when(groupRepository.groupByClusterAndUser(groupClusterEntity.getId(), userEntity.getId()))
        .thenReturn(Optional.of(groupEntity));

    assertTrue(commonDatabaseActions.removeIndividualClusterGroup(courseEntity.getId(),
        userEntity.getId()));

    verify(commonDatabaseActions, times(1)).removeGroup(groupId);
    verify(groupClusterRepository, times(1)).save(groupClusterEntity);
    assertEquals(originalGroupCount - 1, groupClusterEntity.getGroupAmount());

    /* Group not found */
    when(groupRepository.groupByClusterAndUser(groupClusterEntity.getId(), userEntity.getId()))
        .thenReturn(Optional.empty());
    assertFalse(commonDatabaseActions.removeIndividualClusterGroup(courseEntity.getId(),
        userEntity.getId()));

    /* Group cluster not found */
    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(
        Optional.empty());
    assertFalse(commonDatabaseActions.removeIndividualClusterGroup(courseEntity.getId(),
        userEntity.getId()));
  }

  @Test
  public void testDeleteProject() {
    List<GroupFeedbackEntity> groupFeedbackEntities = List.of(groupFeedbackEntity);
    List<SubmissionEntity> submissionEntities = List.of(submissionEntity);
    when(projectRepository.findById(projectEntity.getId())).thenReturn(Optional.of(projectEntity));
    when(groupFeedbackRepository.findByProjectId(projectEntity.getId())).thenReturn(groupFeedbackEntities);
    when(submissionRepository.findByProjectId(projectEntity.getId())).thenReturn(submissionEntities);
    doReturn(new CheckResult<>(HttpStatus.OK, "", null)).when(commonDatabaseActions).deleteSubmissionById(submissionEntity.getId());
    when(testRepository.findById(projectEntity.getTestId())).thenReturn(Optional.of(testEntity));
    doReturn(new CheckResult<>(HttpStatus.OK, "", null)).when(commonDatabaseActions).deleteTestById(projectEntity, testEntity);

    CheckResult<Void> result = commonDatabaseActions.deleteProject(projectEntity.getId());
    assertEquals(HttpStatus.OK, result.getStatus());

    verify(projectRepository, times(1)).delete(projectEntity);
    verify(groupFeedbackRepository, times(1)).deleteAll(groupFeedbackEntities);

    /* No test */
    reset(testRepository);
    projectEntity.setTestId(null);
    result = commonDatabaseActions.deleteProject(projectEntity.getId());
    assertEquals(HttpStatus.OK, result.getStatus());

    verify(testRepository, times(0)).delete(testEntity);


    /* Test not found */
    projectEntity.setTestId(testEntity.getId());
    when(testRepository.findById(projectEntity.getTestId())).thenReturn(Optional.empty());
    result = commonDatabaseActions.deleteProject(projectEntity.getId());
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

    /* Test deletion failed */
    when(testRepository.findById(projectEntity.getTestId())).thenReturn(Optional.of(testEntity));
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null)).when(commonDatabaseActions).deleteTestById(projectEntity, testEntity);
    result = commonDatabaseActions.deleteProject(projectEntity.getId());
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* Submission deletion failed */
    reset(commonDatabaseActions);
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null)).when(commonDatabaseActions).deleteSubmissionById(submissionEntity.getId());
    result = commonDatabaseActions.deleteProject(projectEntity.getId());
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* Project not found */
    when(projectRepository.findById(projectEntity.getId())).thenReturn(Optional.empty());
    result = commonDatabaseActions.deleteProject(projectEntity.getId());
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

    /* Unexpected error */
    when(projectRepository.findById(projectEntity.getId())).thenThrow(new RuntimeException());
    result = commonDatabaseActions.deleteProject(projectEntity.getId());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
  }

  @Test
  public void testDeleteSubmissionById() {
    when(submissionRepository.findById(submissionEntity.getId())).thenReturn(Optional.of(submissionEntity));
    when(fileUtil.deleteFileById(submissionEntity.getFileId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    CheckResult<Void> result = commonDatabaseActions.deleteSubmissionById(submissionEntity.getId());
    assertEquals(HttpStatus.OK, result.getStatus());

    verify(submissionRepository, times(1)).delete(submissionEntity);

    /* File deletion failed */
    when(fileUtil.deleteFileById(submissionEntity.getFileId())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));
    result = commonDatabaseActions.deleteSubmissionById(submissionEntity.getId());
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* Submission not found */
    when(submissionRepository.findById(submissionEntity.getId())).thenReturn(Optional.empty());
    result = commonDatabaseActions.deleteSubmissionById(submissionEntity.getId());
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

    /* Unexpected error */
    when(submissionRepository.findById(submissionEntity.getId())).thenThrow(new RuntimeException());
    result = commonDatabaseActions.deleteSubmissionById(submissionEntity.getId());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
  }

  @Test
  public void testDeleteTestById() {
    when(testRepository.imageIsUsed(testEntity.getDockerImage())).thenReturn(false);

    CheckResult<Void> result = commonDatabaseActions.deleteTestById(projectEntity, testEntity);
    assertEquals(HttpStatus.OK, result.getStatus());

    verify(testRepository, times(1)).deleteById(testEntity.getId());
    verify(projectRepository, times(1)).save(projectEntity);
    assertNull(projectEntity.getTestId());

    /*  Image is used */
    when(testRepository.imageIsUsed(testEntity.getDockerImage())).thenReturn(true);
    result = commonDatabaseActions.deleteTestById(projectEntity, testEntity);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Unexpected error */
    when(testRepository.imageIsUsed(testEntity.getDockerImage())).thenThrow(new RuntimeException());
    result = commonDatabaseActions.deleteTestById(projectEntity, testEntity);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
  }

  @Test
  public void testDeleteClusterById() {
    List<GroupEntity> groupEntities = List.of(groupEntity);
    when(groupRepository.findAllByClusterId(groupClusterEntity.getId())).thenReturn(groupEntities);
    doReturn(true).when(commonDatabaseActions).removeGroup(groupEntity.getId());

    CheckResult<Void> result = commonDatabaseActions.deleteClusterById(groupClusterEntity.getId());
    assertEquals(HttpStatus.OK, result.getStatus());

    verify(groupClusterRepository, times(1)).deleteById(groupClusterEntity.getId());

    /* Group deletion failed */
    reset(commonDatabaseActions);
    doReturn(false).when(commonDatabaseActions).removeGroup(groupEntity.getId());
    result = commonDatabaseActions.deleteClusterById(groupClusterEntity.getId());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());

    /* Unexpected error */
    when(groupRepository.findAllByClusterId(groupClusterEntity.getId())).thenThrow(new RuntimeException());
    result = commonDatabaseActions.deleteClusterById(groupClusterEntity.getId());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
  }

  @Test
  public void testCopyCourse() {
    String originalCourseKey = "courseKey";
    courseEntity.setJoinKey(originalCourseKey);
    Long newCourseId = 39L;
    CourseEntity newCourse  = new CourseEntity(courseEntity.getName(), courseEntity.getDescription(), courseEntity.getCourseYear());
    newCourse.setJoinKey("randomnewkey");
    newCourse.setId(newCourseId);

    GroupClusterEntity individualCluster = new GroupClusterEntity(
        courseEntity.getId(), 20, "clustername", 5
    );
    when(courseRepository.save(argThat(
        course ->
            course.getName().equals(courseEntity.getName()) &&
                course.getDescription().equals(courseEntity.getDescription()) &&
                course.getCourseYear() == courseEntity.getCourseYear() &&
                !course.getJoinKey().equals(originalCourseKey)
    ))).thenReturn(newCourse);

    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(
        Optional.of(individualCluster));

    doReturn(new CheckResult<>(HttpStatus.OK, "", individualCluster))
        .when(commonDatabaseActions).copyGroupCluster(individualCluster, newCourseId, false);

    List<GroupClusterEntity> groupClusterEntities = List.of(groupClusterEntity);
    long newGroupClusterId = 42L;
    GroupClusterEntity groupClusterCopy = new GroupClusterEntity(
        67L, groupClusterEntity.getGroupAmount(), groupClusterEntity.getName(), groupClusterEntity.getMaxSize()
    );
    groupClusterCopy.setId(newGroupClusterId);
    when(groupClusterRepository.findClustersWithoutInvidualByCourseId(courseEntity.getId()))
        .thenReturn(groupClusterEntities);
    doReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterCopy))
        .when(commonDatabaseActions).copyGroupCluster(groupClusterEntity, newCourseId, true);

    List<ProjectEntity> projectEntities = List.of(projectEntity);
    projectEntity.setGroupClusterId(groupClusterEntity.getId());
    when(projectRepository.findByCourseId(courseEntity.getId())).thenReturn(projectEntities);
    doReturn(new CheckResult<>(HttpStatus.OK, "", null))
        .when(commonDatabaseActions).copyProject(projectEntity, newCourseId, newGroupClusterId);

    CheckResult<CourseEntity> result = commonDatabaseActions.copyCourse(courseEntity, userEntity.getId());
    assertEquals(HttpStatus.OK, result.getStatus());

    verify(courseUserRepository, times(1)).save(argThat(
        courseUser ->
            courseUser.getCourseId() == result.getData().getId() &&
                courseUser.getUserId() == userEntity.getId() &&
                courseUser.getRelation().equals(CourseRelation.creator)
    ));

    assertNotEquals(originalCourseKey, result.getData().getJoinKey());
    assertEquals(newCourseId, result.getData().getId());

    CheckResult<CourseEntity> failedResult;
    /* Copyproject fails */
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null))
        .when(commonDatabaseActions).copyProject(projectEntity, newCourseId, newGroupClusterId);
    failedResult = commonDatabaseActions.copyCourse(courseEntity, userEntity.getId());
    assertEquals(HttpStatus.I_AM_A_TEAPOT, failedResult.getStatus());

    /* CopyGroupCluster fails */
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null))
        .when(commonDatabaseActions).copyGroupCluster(groupClusterEntity, newCourseId, true);
    failedResult = commonDatabaseActions.copyCourse(courseEntity, userEntity.getId());
    assertEquals(HttpStatus.I_AM_A_TEAPOT, failedResult.getStatus());

    /* CopyIndividualCluster fails */
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null))
        .when(commonDatabaseActions).copyGroupCluster(individualCluster, newCourseId, false);
    failedResult = commonDatabaseActions.copyCourse(courseEntity, userEntity.getId());
    assertEquals(HttpStatus.I_AM_A_TEAPOT, failedResult.getStatus());

    /* Individual cluster isn't found */
    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(
        Optional.empty());
    failedResult = commonDatabaseActions.copyCourse(courseEntity, userEntity.getId());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, failedResult.getStatus());

  }

  @Test
  public void testCopyGroupCluster() {
    long newCourseId = 39L;
    GroupClusterEntity newGroupCluster = new GroupClusterEntity(
        newCourseId, groupClusterEntity.getGroupAmount(), groupClusterEntity.getName(), groupClusterEntity.getMaxSize()
    );
    newGroupCluster.setId(42L);

    when(groupClusterRepository.save(argThat(
        groupCluster ->
            groupCluster.getCourseId() == newCourseId &&
                groupCluster.getGroupAmount() == groupClusterEntity.getGroupAmount() &&
                groupCluster.getName().equals(groupClusterEntity.getName()) &&
                groupCluster.getMaxSize() == groupClusterEntity.getMaxSize()
    ))).thenReturn(newGroupCluster);

    List<GroupEntity> groupEntities = List.of(groupEntity);
    when(groupRepository.findAllByClusterId(groupClusterEntity.getId())).thenReturn(groupEntities);
    when(groupRepository.save(argThat(
        group ->
            group.getClusterId() == newGroupCluster.getId() &&
                group.getName().equals(groupEntity.getName())
    ))).thenReturn(groupEntity);

    CheckResult<GroupClusterEntity> result = commonDatabaseActions.copyGroupCluster(groupClusterEntity, newCourseId, true);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(newGroupCluster, result.getData());

    /* Don't copy groups */
    reset(groupClusterRepository);
    when(groupClusterRepository.save(argThat(
        groupCluster ->
            groupCluster.getCourseId() == newCourseId &&
                groupCluster.getGroupAmount() == 0 &&
                groupCluster.getName().equals(groupClusterEntity.getName()) &&
                groupCluster.getMaxSize() == groupClusterEntity.getMaxSize()
    ))).thenReturn(newGroupCluster);
    result = commonDatabaseActions.copyGroupCluster(groupClusterEntity, newCourseId, false);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(newGroupCluster, result.getData());

    verify(groupRepository, times(1)).save(any());
  }

  @Test
  public void testCopyProject() {
    long newCourseId = 39L;
    long newGroupClusterId = 42L;
    long newProjectId = 99L;
    long newTestId = 88L;
    testEntity.setId(newTestId);
    ProjectEntity newProject = new ProjectEntity(
        newCourseId,
        projectEntity.getName(),
        projectEntity.getDescription(),
        newGroupClusterId,
        projectEntity.getTestId(),
        projectEntity.isVisible(),
        projectEntity.getMaxScore(),
        projectEntity.getDeadline()
    );
    newProject.setId(newProjectId);

    when(projectRepository.save(any())).thenReturn(newProject);

    when(testRepository.findById(projectEntity.getTestId())).thenReturn(Optional.of(testEntity));
    doReturn(new CheckResult<>(HttpStatus.OK, "", testEntity))
        .when(commonDatabaseActions).copyTest(testEntity);

    CheckResult<ProjectEntity> result = commonDatabaseActions.copyProject(projectEntity, newCourseId, newGroupClusterId);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(newProject, result.getData());
    assertEquals(newProjectId, result.getData().getId());



    verify(projectRepository, times(1)).save(argThat(
        project -> project.getCourseId() == newCourseId &&
              project.getName().equals(projectEntity.getName()) &&
              project.getDescription().equals(projectEntity.getDescription()) &&
            project.getGroupClusterId() == newGroupClusterId &&
            Objects.equals(project.getTestId(), null) &&
            project.isVisible() == projectEntity.isVisible() &&
            Objects.equals(project.getMaxScore(), projectEntity.getMaxScore()) &&
            project.getDeadline().equals(projectEntity.getDeadline())
    ));

    verify(projectRepository, times(1)).save(argThat(
        project ->
            project.getCourseId() == newCourseId &&
                project.getName().equals(projectEntity.getName()) &&
                project.getDescription().equals(projectEntity.getDescription()) &&
                project.getGroupClusterId() == newGroupClusterId &&
                Objects.equals(project.getTestId(), newTestId) &&
                project.isVisible() == projectEntity.isVisible() &&
                Objects.equals(project.getMaxScore(), projectEntity.getMaxScore()) &&
                project.getDeadline().equals(projectEntity.getDeadline())
    ));

    /* CopyTestFails */
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null))
        .when(commonDatabaseActions).copyTest(testEntity);
    result = commonDatabaseActions.copyProject(projectEntity, newCourseId, newGroupClusterId);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* Test not found */
    when(testRepository.findById(projectEntity.getTestId())).thenReturn(Optional.empty());
    result = commonDatabaseActions.copyProject(projectEntity, newCourseId, newGroupClusterId);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());

    /* project has no test */
    reset(projectRepository);
    reset(testRepository);
    reset(commonDatabaseActions);
    when(projectRepository.save(any())).thenReturn(newProject);
    projectEntity.setTestId(null);
    result = commonDatabaseActions.copyProject(projectEntity, newCourseId, newGroupClusterId);
    assertEquals(HttpStatus.OK, result.getStatus());

    verify(projectRepository, times(1)).save(argThat(
        project -> project.getCourseId() == newCourseId &&
              project.getName().equals(projectEntity.getName()) &&
              project.getDescription().equals(projectEntity.getDescription()) &&
            project.getGroupClusterId() == newGroupClusterId &&
            Objects.equals(project.getTestId(), null) &&
            project.isVisible() == projectEntity.isVisible() &&
            Objects.equals(project.getMaxScore(), projectEntity.getMaxScore()) &&
            project.getDeadline().equals(projectEntity.getDeadline())
    ));
    verify(testRepository, times(0)).findById(projectEntity.getTestId());
    verify(commonDatabaseActions, times(0)).copyTest(testEntity);
  }

  @Test
  public void testCopyTest() {
    long newTestId = 9088L;
    TestEntity newTest = new TestEntity(
        testEntity.getDockerImage(),
        testEntity.getDockerTestScript(),
        testEntity.getDockerTestTemplate(),
        testEntity.getStructureTemplate()
    );
    newTest.setId(newTestId);

    when(testRepository.save(argThat(
        test ->
            test.getDockerImage().equals(testEntity.getDockerImage()) &&
                test.getDockerTestScript().equals(testEntity.getDockerTestScript()) &&
                test.getDockerTestTemplate().equals(testEntity.getDockerTestTemplate()) &&
                test.getStructureTemplate().equals(testEntity.getStructureTemplate())
    ))).thenReturn(newTest);

    CheckResult<TestEntity> result = commonDatabaseActions.copyTest(testEntity);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(newTest, result.getData());
    assertEquals(newTestId, result.getData().getId());
  }
}
