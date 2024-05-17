package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;
import com.ugent.pidgeon.model.json.ProjectResponseJsonWithStatus;
import com.ugent.pidgeon.model.json.UserProjectsJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.ClusterUtil;
import com.ugent.pidgeon.util.CommonDatabaseActions;
import com.ugent.pidgeon.util.CourseUtil;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.Pair;
import com.ugent.pidgeon.util.ProjectUtil;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectControllerTest extends ControllerTest  {

  @InjectMocks
  private ProjectController projectController;

  @Mock
  private CourseRepository courseRepository;

  @Mock
  private CourseUserRepository courseUserRepository;

  @Mock
  private GroupClusterRepository groupClusterRepository;

  @Mock
  private TestRepository testRepository;

  @Mock
  private CourseUtil courseUtil;

  @Mock
  private ProjectUtil projectUtil;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private EntityToJsonConverter entityToJsonConverter;

  @Mock
  private CommonDatabaseActions commonDatabaseActions;

  @Mock
  private ClusterUtil clusterUtil;

  @Mock
  private GroupRepository grouprRepository;

  private final ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();
  private ProjectEntity projectEntity;
  private ProjectEntity projectEntity2;
  private ProjectResponseJson projectResponseJson;
  private ProjectResponseJson projectResponseJson2;
  private CourseEntity courseEntity;
  private CourseEntity courseEntity2;
  private final long groupClusterId = 7L;

  @BeforeEach
  void setUp() {
    setUpController(projectController);

    courseEntity = new CourseEntity("courseName", "courseUrl", 2020);
    courseEntity.setId(24L);
    courseEntity2 = new CourseEntity("courseName2", "courseUrl2", 2021);
    courseEntity2.setId(25L);

    projectEntity = new ProjectEntity(
      courseEntity.getId(),
      "projectName",
      "projectDescription",
      groupClusterId,
      38L,
      true,
      34,
      OffsetDateTime.now()
    );
    projectEntity.setId(64);
    projectResponseJson = new ProjectResponseJson(
      new CourseReferenceJson(courseEntity.getName(), "course1URL", courseEntity.getId(), null),
      OffsetDateTime.now(),
      projectEntity.getName(),
      projectEntity.getId(),
      projectEntity.getDescription(),
      "submissionUrl",
      "testUrl",
      projectEntity.getMaxScore(),
      projectEntity.isVisible(),
      new ProjectProgressJson(0, 0),
      1L,
      groupClusterId,
        OffsetDateTime.now()
    );

    projectEntity2 = new ProjectEntity(
      courseEntity2.getId(),
      "projectName2",
      "projectDescription2",
      groupClusterId,
      39L,
      true,
      32,
      OffsetDateTime.now()
    );
    projectEntity2.setId(65);
    projectResponseJson2 = new ProjectResponseJson(
      new CourseReferenceJson(courseEntity2.getName(), "course2URL", courseEntity2.getId(), null),
      OffsetDateTime.now(),
      projectEntity2.getName(),
      projectEntity2.getId(),
      projectEntity2.getDescription(),
      "submissionUrl",
      "testUrl",
      projectEntity2.getMaxScore(),
      projectEntity2.isVisible(),
      new ProjectProgressJson(0, 0),
      1L,
      groupClusterId,
        OffsetDateTime.now()
    );

  }


  @Test
  void testGetProjects() throws Exception {
    String url = ApiRoutes.PROJECT_BASE_PATH;
    List<ProjectEntity> projectEntities = List.of(projectEntity, projectEntity2);
    ProjectResponseJsonWithStatus projectJsonWithStatus = new ProjectResponseJsonWithStatus(
        projectResponseJson2,
        "completed"
    );
    when(courseUtil.getCourseIfUserInCourse(courseEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, CourseRelation.creator))
    );
    when(courseUtil.getCourseIfUserInCourse(courseEntity2.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity2, CourseRelation.enrolled))
    );
    when(projectRepository.findProjectsByUserId(getMockUser().getId())).thenReturn(projectEntities);
    when(entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, getMockUser()))
        .thenReturn(projectResponseJson);
    when(entityToJsonConverter.projectEntityToProjectResponseJsonWithStatus(projectEntity2, courseEntity2, getMockUser()))
        .thenReturn(projectJsonWithStatus);

    /* Returns the user's projects */
    UserProjectsJson userProjectsJson = new UserProjectsJson(
        List.of(projectJsonWithStatus),
        List.of(projectResponseJson)
    );
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(userProjectsJson)));

    /* If project isn't visible and role enrolled, don't return it */
    projectEntity2.setVisible(false);
    userProjectsJson = new UserProjectsJson(
        Collections.emptyList(),
        List.of(projectResponseJson)
    );
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(userProjectsJson)));

    /* If project isn't visible but visibleAfter is passed,  update visibility */
    projectEntity2.setVisibleAfter(OffsetDateTime.now().minusDays(1));
    userProjectsJson = new UserProjectsJson(
        List.of(projectJsonWithStatus),
        List.of(projectResponseJson)
    );
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(userProjectsJson)));

    verify(projectRepository, times(1)).save(projectEntity2);
    assertTrue(projectEntity2.isVisible());

    /* If project isn't visible and visibleAfter is in the future, don't return it */
    projectEntity2.setVisible(false);
    projectEntity2.setVisibleAfter(OffsetDateTime.now().plusDays(1));
    userProjectsJson = new UserProjectsJson(
        Collections.emptyList(),
        List.of(projectResponseJson)
    );

    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(userProjectsJson)));

    assertFalse(projectEntity2.isVisible());

    /* If a coursecheck fails, return corresponding status */
    when(courseUtil.getCourseIfUserInCourse(courseEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isIAmATeapot());
  }

  @Test
  void testGetProject() throws Exception {
    String url = ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId();

    /* If user can get project, return project */
    when(projectUtil.canGetProject(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(courseUtil.getCourseIfUserInCourse(projectEntity.getCourseId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, CourseRelation.enrolled))
    );
    when(entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity, getMockUser()))
        .thenReturn(projectResponseJson);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(projectResponseJson)));

    /* If user is enrolled and project not visible, return forbidden */
    projectEntity.setVisible(false);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isNotFound());

    /* if visibleAfter is passed, update visibility */
    projectEntity.setVisibleAfter(OffsetDateTime.now().minusDays(1));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk());

    verify(projectRepository, times(1)).save(projectEntity);
    assertTrue(projectEntity.isVisible());

    /* If visibleAfter is in the future, return 404 */
    projectEntity.setVisible(false);
    projectEntity.setVisibleAfter(OffsetDateTime.now().plusDays(1));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isNotFound());

    assertFalse(projectEntity.isVisible());

    /* If user is not enrolled and project not visible, return project */
    when(courseUtil.getCourseIfUserInCourse(projectEntity.getCourseId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, CourseRelation.course_admin))
    );
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(projectResponseJson)));

    /* If get course with relation check fails, return correpsonding status */
    when(courseUtil.getCourseIfUserInCourse(projectEntity.getCourseId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isIAmATeapot());

    /* If user can't get project, return corresponding status */
    when(projectUtil.canGetProject(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isIAmATeapot());
  }

  @Test
  public void testCreateProject() throws Exception {
    String url = ApiRoutes.COURSE_BASE_PATH + "/" + courseEntity.getId() + "/projects";
    projectEntity.setVisibleAfter(OffsetDateTime.now().plusDays(1));
    String request = "{\n" +
        "  \"name\": \"" + projectEntity.getName() + "\",\n" +
        "  \"description\": \"" + projectEntity.getDescription() + "\",\n" +
        "  \"groupClusterId\": " + projectEntity.getGroupClusterId() + ",\n" +
        "  \"visible\": " + projectEntity.isVisible() + ",\n" +
        "  \"maxScore\": " + projectEntity.getMaxScore() + ",\n" +
        "  \"deadline\": \"" + projectEntity.getDeadline() + "\",\n" +
        "  \"visibleAfter\": \"" + projectEntity.getVisibleAfter() + "\"\n" +
        "}";

    /* If all checks succeed, create course */
    when(courseUtil.getCourseIfAdmin(courseEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", courseEntity)
    );
    when(projectUtil.checkProjectJson(argThat(
        json -> json.getName().equals(projectEntity.getName() )
              && json.getDescription().equals(projectEntity.getDescription())
              && json.getGroupClusterId().equals(projectEntity.getGroupClusterId())
              && json.isVisible().equals(projectEntity.isVisible())
              && json.getMaxScore().equals(projectEntity.getMaxScore())
              && json.getDeadline().toInstant().equals(projectEntity.getDeadline().toInstant())
    ), eq(courseEntity.getId()))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(projectRepository.save(any())).thenReturn(projectEntity);
    when(entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity,
        getMockUser()))
        .thenReturn(projectResponseJson);
    mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(projectResponseJson)));
    verify(projectRepository).save(argThat(
        project -> project.getName().equals(projectEntity.getName())
            && project.getDescription().equals(projectEntity.getDescription())
            && project.getGroupClusterId() == projectEntity.getGroupClusterId()
            && project.isVisible().equals(projectEntity.isVisible())
            && project.getMaxScore().equals(projectEntity.getMaxScore())
            && project.getDeadline().toInstant().equals(projectEntity.getDeadline().toInstant())
            && project.getVisibleAfter().toInstant().equals(projectEntity.getVisibleAfter().toInstant())
    ));

    /* If groupClusterId is not provided, use invalid groupClusterId */
    reset(projectUtil);
    request = "{\n" +
        "  \"name\": \"" + projectEntity.getName() + "\",\n" +
        "  \"description\": \"" + projectEntity.getDescription() + "\",\n" +
        "  \"visible\": " + projectEntity.isVisible() + ",\n" +
        "  \"maxScore\": " + projectEntity.getMaxScore() + ",\n" +
        "  \"deadline\": \"" + projectEntity.getDeadline() + "\"\n" +
        "}";
    GroupClusterEntity individualClusterEntity = new GroupClusterEntity(courseEntity.getId(), 2, "Individual", 1);

    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(
        Optional.of(individualClusterEntity));
    when(projectUtil.checkProjectJson(argThat(
        json -> json.getName().equals(projectEntity.getName() )
              && json.getDescription().equals(projectEntity.getDescription())
              && json.getGroupClusterId().equals(individualClusterEntity.getId())
              && json.isVisible().equals(projectEntity.isVisible())
              && json.getMaxScore().equals(projectEntity.getMaxScore())
              && json.getDeadline().toInstant().equals(projectEntity.getDeadline().toInstant())
    ), eq(courseEntity.getId()))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());
    verify(projectRepository).save(argThat(
        project -> project.getName().equals(projectEntity.getName())
            && project.getDescription().equals(projectEntity.getDescription())
            && project.getGroupClusterId() == individualClusterEntity.getId()
            && project.isVisible().equals(projectEntity.isVisible())
            && project.getMaxScore().equals(projectEntity.getMaxScore())
            && project.getDeadline().toInstant().equals(projectEntity.getDeadline().toInstant())
    ));

    /* If unexpected error occurs, return internal server error */
    doThrow(new RuntimeException()).when(projectRepository).save(any());
    mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isInternalServerError());

    /* If project json is invalid, return corresponding status */
    reset(projectUtil);
    when(projectUtil.checkProjectJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BAD_REQUEST, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());

    /* If no individual cluster is found, return internal server error */
    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(Optional.empty());
    mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isInternalServerError());

    /* If user no access to course, return corresponding status code */
    when(courseUtil.getCourseIfAdmin(courseEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isIAmATeapot());
  }

  @Test
  void testPutProjectById() throws Exception {
    String url = ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId();
    OffsetDateTime newDeadline = OffsetDateTime.now().plusDays(1);
    String request = "{\n" +
        "  \"name\": \"" + "UpdatedName" + "\",\n" +
        "  \"description\": \"" + "UpdatedDescription" + "\",\n" +
        "  \"groupClusterId\": " + groupClusterId * 4 + ",\n" +
        "  \"visible\": " + false + ",\n" +
        "  \"maxScore\": " + (projectEntity.getMaxScore() + 33) + ",\n" +
        "  \"deadline\": \"" + newDeadline + "\"\n" +
        "}";
    String orginalName = projectEntity.getName();
    String orginalDescription = projectEntity.getDescription();
    long orginalGroupClusterId = projectEntity.getGroupClusterId();
    boolean orginalVisible = projectEntity.isVisible();
    int orginalMaxScore = projectEntity.getMaxScore();
    OffsetDateTime orginalDeadline = projectEntity.getDeadline();
    ProjectResponseJson updatedJson = new ProjectResponseJson(
        new CourseReferenceJson(courseEntity.getName(), "course1URL", courseEntity.getId(), null),
        newDeadline,
        "UpdatedName",
        projectEntity.getId(),
        "UpdatedDescription",
        "submissionUrl",
        "testUrl",
        projectEntity.getMaxScore() + 33,
        false,
        new ProjectProgressJson(0, 0),
        1L,
        groupClusterId * 4,
        OffsetDateTime.now()
    );
    /* If all checks pass, update and return the project */
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(projectUtil.checkProjectJson(argThat(
        json -> json.getName().equals("UpdatedName")
              && json.getDescription().equals("UpdatedDescription")
              && json.getGroupClusterId().equals(groupClusterId * 4)
              && json.isVisible().equals(false)
              && json.getMaxScore().equals(projectEntity.getMaxScore() + 33)
              && json.getDeadline().toInstant().equals(newDeadline.toInstant())
    ), eq(courseEntity.getId()))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(projectRepository.save(any())).thenReturn(projectEntity);
    when(courseRepository.findById(courseEntity.getId())).thenReturn(Optional.of(courseEntity));
    when(entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity,
        getMockUser()))
        .thenReturn(updatedJson);
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(updatedJson)));
    assertEquals(projectEntity.getName(), "UpdatedName");
    assertEquals(projectEntity.getDescription(), "UpdatedDescription");
    assertEquals(projectEntity.getGroupClusterId(), groupClusterId * 4);
    assertEquals(projectEntity.isVisible(), false);
    assertEquals(projectEntity.getMaxScore(), orginalMaxScore + 33);
    assertEquals(projectEntity.getDeadline().toInstant(), newDeadline.toInstant());
    verify(projectRepository, times(1)).save(projectEntity);
    projectEntity.setName(orginalName);
    projectEntity.setDescription(orginalDescription);
    projectEntity.setGroupClusterId(orginalGroupClusterId);
    projectEntity.setVisible(orginalVisible);
    projectEntity.setMaxScore(orginalMaxScore);
    projectEntity.setDeadline(orginalDeadline);

    /* If visible after is passed, update visibility */
    projectEntity.setVisibleAfter(OffsetDateTime.now().minusDays(1));
    request = "{\n" +
        "  \"name\": \"" + "UpdatedName" + "\",\n" +
        "  \"description\": \"" + "UpdatedDescription" + "\",\n" +
        "  \"groupClusterId\": " + groupClusterId * 4 + ",\n" +
        "  \"visible\": " + false + ",\n" +
        "  \"maxScore\": " + (projectEntity.getMaxScore() + 33) + ",\n" +
        "  \"deadline\": \"" + newDeadline + "\",\n" +
        "  \"visibleAfter\": \"" + OffsetDateTime.now().minusDays(1) + "\"\n" +
        "}";
    mockMvc.perform(MockMvcRequestBuilders.put(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request));

    verify(projectRepository, times(2)).save(projectEntity);
    assertTrue(projectEntity.isVisible());

    /* If visible after isn't passed, don't update visibility */
    projectEntity.setVisible(false);
    request = "{\n" +
        "  \"name\": \"" + "UpdatedName" + "\",\n" +
        "  \"description\": \"" + "UpdatedDescription" + "\",\n" +
        "  \"groupClusterId\": " + groupClusterId * 4 + ",\n" +
        "  \"visible\": " + false + ",\n" +
        "  \"maxScore\": " + (projectEntity.getMaxScore() + 33) + ",\n" +
        "  \"deadline\": \"" + newDeadline + "\",\n" +
        "  \"visibleAfter\": \"" + OffsetDateTime.now().plusDays(1) + "\"\n" +
        "}";
    mockMvc.perform(MockMvcRequestBuilders.put(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request));

    assertFalse(projectEntity.isVisible());

    projectEntity.setName(orginalName);
    projectEntity.setDescription(orginalDescription);
    projectEntity.setGroupClusterId(orginalGroupClusterId);
    projectEntity.setVisible(orginalVisible);
    projectEntity.setMaxScore(orginalMaxScore);
    projectEntity.setDeadline(orginalDeadline);

    /* If groupClusterId is not provided, use invalid groupClusterId */
    reset(projectUtil);
    request = "{\n" +
        "  \"name\": \"" + "UpdatedName" + "\",\n" +
        "  \"description\": \"" + "UpdatedDescription" + "\",\n" +
        "  \"visible\": " + false + ",\n" +
        "  \"maxScore\": " + (projectEntity.getMaxScore() + 33) + ",\n" +
        "  \"deadline\": \"" + newDeadline + "\"\n" +
        "}";
    GroupClusterEntity individualClusterEntity = new GroupClusterEntity(courseEntity.getId(), 2, "Individual", 1);

    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(
        Optional.of(individualClusterEntity));
    when(projectUtil.checkProjectJson(argThat(
        json -> json.getName().equals("UpdatedName")
              && json.getDescription().equals("UpdatedDescription")
              && json.getGroupClusterId().equals(individualClusterEntity.getId())
              && json.isVisible().equals(false)
              && json.getMaxScore().equals(projectEntity.getMaxScore() + 33)
              && json.getDeadline().toInstant().equals(newDeadline.toInstant())
    ), eq(courseEntity.getId()))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());
    assertEquals(projectEntity.getName(), "UpdatedName");
    assertEquals(projectEntity.getDescription(), "UpdatedDescription");
    assertEquals(projectEntity.getGroupClusterId(), individualClusterEntity.getId());
    assertEquals(projectEntity.isVisible(), false);
    assertEquals(projectEntity.getMaxScore(), orginalMaxScore + 33);
    assertEquals(projectEntity.getDeadline().toInstant(), newDeadline.toInstant());
    verify(projectRepository, times(4)).save(projectEntity);
    projectEntity.setGroupClusterId(orginalGroupClusterId);



    /* If project json is invalid, return corresponding status */
    reset(projectUtil);
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(projectUtil.checkProjectJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BAD_REQUEST, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());

    /* If individual cluster is not found, return internal server error */
    when(groupClusterRepository.findIndividualClusterByCourseId(courseEntity.getId())).thenReturn(Optional.empty());
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isInternalServerError());

    /* If user has no acces to project, return corresponding status */
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isIAmATeapot());
  }

  @Test // Same as above but patch instead of put
  void testPatchProjectById() throws Exception {
    String url = ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId();
    OffsetDateTime newDeadline = OffsetDateTime.now().plusDays(1);
    String request = "{\n" +
        "  \"name\": \"" + "UpdatedName" + "\",\n" +
        "  \"description\": \"" + "UpdatedDescription" + "\",\n" +
        "  \"groupClusterId\": " + groupClusterId * 4 + ",\n" +
        "  \"visible\": " + false + ",\n" +
        "  \"maxScore\": " + (projectEntity.getMaxScore() + 33) + ",\n" +
        "  \"deadline\": \"" + newDeadline + "\",\n" +
        "  \"visibleAfter\": \"" + OffsetDateTime.now().plusDays(1) + "\"\n" +
        "}";
    String orginalName = projectEntity.getName();
    String orginalDescription = projectEntity.getDescription();
    long orginalGroupClusterId = projectEntity.getGroupClusterId();
    boolean orginalVisible = projectEntity.isVisible();
    int orginalMaxScore = projectEntity.getMaxScore();
    OffsetDateTime orginalDeadline = projectEntity.getDeadline();
    ProjectResponseJson updatedJson = new ProjectResponseJson(
        new CourseReferenceJson(courseEntity.getName(), "course1URL", courseEntity.getId(), null),
        newDeadline,
        "UpdatedName",
        projectEntity.getId(),
        "UpdatedDescription",
        "submissionUrl",
        "testUrl",
        projectEntity.getMaxScore() + 33,
        false,
        new ProjectProgressJson(0, 0),
        1L,
        groupClusterId * 4,
        OffsetDateTime.now()
    );
    /* If all checks pass, update and return the project */
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(projectUtil.checkProjectJson(argThat(
        json -> json.getName().equals("UpdatedName")
            && json.getDescription().equals("UpdatedDescription")
            && json.getGroupClusterId().equals(groupClusterId * 4)
            && json.isVisible().equals(false)
            && json.getMaxScore().equals(projectEntity.getMaxScore() + 33)
            && json.getDeadline().toInstant().equals(newDeadline.toInstant())
    ), eq(courseEntity.getId()))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(projectRepository.save(any())).thenReturn(projectEntity);
    when(courseRepository.findById(courseEntity.getId())).thenReturn(Optional.of(courseEntity));
    when(entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, courseEntity,
        getMockUser()))
        .thenReturn(updatedJson);
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(updatedJson)));
    assertEquals(projectEntity.getName(), "UpdatedName");
    assertEquals(projectEntity.getDescription(), "UpdatedDescription");
    assertEquals(projectEntity.getGroupClusterId(), groupClusterId * 4);
    assertEquals(projectEntity.isVisible(), false);
    assertEquals(projectEntity.getMaxScore(), orginalMaxScore + 33);
    assertEquals(projectEntity.getDeadline().toInstant(), newDeadline.toInstant());
    verify(projectRepository, times(1)).save(projectEntity);
    projectEntity.setName(orginalName);
    projectEntity.setDescription(orginalDescription);
    projectEntity.setGroupClusterId(orginalGroupClusterId);
    projectEntity.setVisible(orginalVisible);
    projectEntity.setMaxScore(orginalMaxScore);
    projectEntity.setDeadline(orginalDeadline);

    /* If project json is invalid, return corresponding status */
    reset(projectUtil);
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(projectUtil.checkProjectJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BAD_REQUEST, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());

    /* Only update the fields that are provided */
    reset(projectUtil);
    request = "{\n" +
        "  \"name\": \"" + "UpdatedName" + "\",\n" +
        "  \"description\": \"" + "UpdatedDescription" + "\"\n" +
        "}";
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(projectUtil.checkProjectJson(argThat(
        json -> json.getName().equals("UpdatedName")
            && json.getDescription().equals("UpdatedDescription")
            && json.getGroupClusterId().equals(orginalGroupClusterId)
            && json.isVisible().equals(orginalVisible)
            && json.getMaxScore().equals(orginalMaxScore)
            && json.getDeadline().toInstant().equals(orginalDeadline.toInstant())
    ), eq(courseEntity.getId()))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());
    assertEquals(projectEntity.getName(), "UpdatedName");
    assertEquals(projectEntity.getDescription(), "UpdatedDescription");
    assertEquals(projectEntity.getGroupClusterId(), orginalGroupClusterId);
    assertEquals(projectEntity.isVisible(), orginalVisible);
    assertEquals(projectEntity.getMaxScore(), orginalMaxScore);
    assertEquals(projectEntity.getDeadline().toInstant(), orginalDeadline.toInstant());
    verify(projectRepository, times(2)).save(projectEntity);
    projectEntity.setName(orginalName);
    projectEntity.setDescription(orginalDescription);

    /* Different fields not present */
    reset(projectUtil);
    request = "{\n" +
        "  \"deadline\": \"" + newDeadline + "\"\n" +
        "}";
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(projectUtil.checkProjectJson(argThat(
        json -> json.getName().equals(orginalName)
            && json.getDescription().equals(orginalDescription)
            && json.getGroupClusterId().equals(orginalGroupClusterId)
            && json.isVisible().equals(orginalVisible)
            && json.getMaxScore().equals(orginalMaxScore)
            && json.getDeadline().toInstant().equals(newDeadline.toInstant())
    ), eq(courseEntity.getId()))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());
    assertEquals(projectEntity.getName(), orginalName);
    assertEquals(projectEntity.getDescription(), orginalDescription);
    assertEquals(projectEntity.getGroupClusterId(), orginalGroupClusterId);
    assertEquals(projectEntity.isVisible(), orginalVisible);
    assertEquals(projectEntity.getMaxScore(), orginalMaxScore);
    assertEquals(projectEntity.getDeadline().toInstant(), newDeadline.toInstant());
    verify(projectRepository, times(3)).save(projectEntity);
    projectEntity.setDeadline(orginalDeadline);

    /* If user has no acces to project, return corresponding status */
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isIAmATeapot());
  }

  @Test
  void getGroupsOfProject() throws Exception {
    String url = ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId() + "/groups";
    GroupEntity groupEntity = new GroupEntity("groupName",  1L);
    long groupId = 83L;
    groupEntity.setId(groupId);
    GroupJson groupJson = new GroupJson(44, groupEntity.getId(), groupEntity.getName(), "groupClusterUrl");

    /* If all checks pass, return groups */
    when(projectUtil.canGetProject(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(clusterUtil.isIndividualCluster(projectEntity.getGroupClusterId())).thenReturn(false);
    when(projectRepository.findGroupIdsByProjectId(projectEntity.getId())).thenReturn(List.of(groupId));
    when(grouprRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));
    when(entityToJsonConverter.groupEntityToJson(groupEntity)).thenReturn(groupJson);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(groupJson))));

    /* If inidividual cluster return no content */
    when(clusterUtil.isIndividualCluster(projectEntity.getGroupClusterId())).thenReturn(true);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isNoContent());

    /* If user has no acces to project, return corresponding status */
    when(projectUtil.canGetProject(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isIAmATeapot());
  }

  @Test
  void testDeleteProjectById() throws Exception {
    String url = ApiRoutes.PROJECT_BASE_PATH + "/" + projectEntity.getId();

    /* If all checks pass, delete project */
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", projectEntity)
    );
    when(commonDatabaseActions.deleteProject(projectEntity.getId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isOk());

    /* If deleting project fails, return corresponding status */
    when(commonDatabaseActions.deleteProject(projectEntity.getId())).thenReturn(new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "", null));
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isInternalServerError());

    /* If user has no acces to project, return corresponding status */
    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), getMockUser())).thenReturn(
        new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null)
    );
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isIAmATeapot());
  }
}