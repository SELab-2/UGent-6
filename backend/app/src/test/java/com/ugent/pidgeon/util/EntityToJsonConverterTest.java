package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseWithInfoJson;
import com.ugent.pidgeon.model.json.GroupClusterJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.SubmissionJson;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.repository.*;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EntityToJsonConverterTest {

  @Mock
  private GroupClusterRepository groupClusterRepository;

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

  @InjectMocks
  private EntityToJsonConverter entityToJsonConverter;

  private GroupEntity groupEntity;
  private GroupClusterEntity groupClusterEntity;
  private UserEntity userEntity;
  private CourseEntity courseEntity;
  private ProjectEntity projectEntity;
  private SubmissionEntity submissionEntity;
  private TestEntity testEntity;

  @BeforeEach
  public void setUp() {
    groupEntity = new GroupEntity();
    groupEntity.setId(1L);
    groupEntity.setName("Test Group");
    groupEntity.setClusterId(1L);

    groupClusterEntity = new GroupClusterEntity();
    groupClusterEntity.setId(1L);
    groupClusterEntity.setName("Test Cluster");
    groupClusterEntity.setGroupAmount(1);

    userEntity = new UserEntity();
    userEntity.setId(1L);
    userEntity.setName("Test User");

    courseEntity = new CourseEntity();
    courseEntity.setId(1L);
    courseEntity.setName("Test Course");

    projectEntity = new ProjectEntity();
    projectEntity.setId(1L);
    projectEntity.setName("Test Project");

    submissionEntity = new SubmissionEntity(1L,1L,1L, OffsetDateTime.now(),true,true);

    testEntity = new TestEntity();
    testEntity.setId(1L);
    testEntity.setDockerImage("Test Docker Image");
  }

  @Test
  public void testGroupEntityToJson() {
    when(groupClusterRepository.findById(anyLong())).thenReturn(Optional.of(groupClusterEntity));
    when(groupRepository.findGroupUsersReferencesByGroupId(anyLong())).thenReturn(Collections.emptyList());
    GroupJson result = entityToJsonConverter.groupEntityToJson(groupEntity);
    assertEquals(groupEntity.getName(), result.getName());
  }

  @Test
  public void testClusterEntityToClusterJson() {
    when(groupRepository.findAllByClusterId(anyLong())).thenReturn(Collections.singletonList(groupEntity));
    GroupClusterJson result = entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity);
    assertEquals(groupClusterEntity.getId(), result.clusterId());
    assertEquals(groupClusterEntity.getName(), result.name());
  }

//  @Test
//  public void testUserEntityToUserReference() {
//    UserReferenceJson result = entityToJsonConverter.userEntityToUserReference(userEntity);
//    assertEquals(userEntity.getId(), result.getId());
//    assertEquals(userEntity.getName(), result.getName());
//  }

  @Test
  public void testCourseEntityToCourseWithInfo() {
    when(courseRepository.findTeacherByCourseId(anyLong())).thenReturn(userEntity);
    when(courseRepository.findAssistantsByCourseId(anyLong())).thenReturn(Collections.emptyList());
    CourseWithInfoJson result = entityToJsonConverter.courseEntityToCourseWithInfo(courseEntity, "joinLink");
    assertEquals(courseEntity.getId(), result.courseId());
    assertEquals(courseEntity.getName(), result.name());
  }

//  @Test
//  public void testProjectEntityToProjectResponseJson() {
//    when(projectRepository.findGroupIdsByProjectId(anyLong())).thenReturn(Collections.singletonList(1L));
//    when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(anyLong(), anyLong())).thenReturn(1L);
//    when(submissionRepository.findById(anyLong())).thenReturn(Optional.of(submissionEntity));
//    when(courseUserRepository.findById(any(CourseUserId.class))).thenReturn(Optional.empty());
//    ProjectResponseJson result = entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, userEntity);
//    assertEquals(projectEntity.getId(), result.projectId());
//    assertEquals(projectEntity.getName(), result.name());
//  }

  @Test
  public void testGetSubmissionJson() {
    SubmissionJson result = entityToJsonConverter.getSubmissionJson(submissionEntity);
    assertEquals(submissionEntity.getId(), result.getSubmissionId());
    assertEquals(submissionEntity.getProjectId(), result.getProjectId());
  }

  @Test
  public void testTestEntityToTestJson() {
    TestJson result = entityToJsonConverter.testEntityToTestJson(testEntity, 1L);
    assertEquals(testEntity.getDockerImage(), result.getDockerImage());
  }
}