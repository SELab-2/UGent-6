package com.ugent.pidgeon.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.json.GroupFeedbackJson;
import com.ugent.pidgeon.json.GroupFeedbackJsonWithProject;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.CourseUtil;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.GroupFeedbackUtil;
import com.ugent.pidgeon.util.GroupUtil;
import com.ugent.pidgeon.util.Pair;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(MockitoExtension.class)
public class GroupFeedbackControllerTest extends ControllerTest {

  @Mock
  private GroupFeedbackRepository groupFeedbackRepository;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private GroupFeedbackUtil groupFeedbackUtil;
  @Mock
  private GroupRepository groupRepository;
  @Mock
  private GroupUtil groupUtil;
  @Mock
  private CourseUtil courseUtil;
  @Mock
  private EntityToJsonConverter entityToJsonConverter;

  @InjectMocks
  private GroupFeedbackController groupFeedbackController;

  private GroupFeedbackEntity groupFeedbackEntity;
  private GroupFeedbackJson groupFeedbackJson;

  private final ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();

  @BeforeEach
  public void setup() {
    setUpController(groupFeedbackController);
    groupFeedbackEntity = new GroupFeedbackEntity(4L, 6L, 1F, "good job.... NOT!");
    groupFeedbackJson = new GroupFeedbackJson(groupFeedbackEntity.getScore(), groupFeedbackEntity.getFeedback(),
        groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());

  }

  @Test
  public void testUpdateGroupScore() throws Exception {
    String url = ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", ""+groupFeedbackEntity.getGroupId())
        .replace("{projectid}", ""+groupFeedbackEntity.getProjectId());
    String requestAllNull = "{\"score\": null,\"feedback\": null}";
    String requestScoreNull = "{\"score\": null,\"feedback\": \"Heel goed gedaan\"}";
    String requestFeedbackNull = "{\"score\": 4.4,\"feedback\": null}";
    String request = "{\"score\": 4.4,\"feedback\": \"Heel goed gedaan\"}";
    String originalFeedback = groupFeedbackEntity.getFeedback();
    Float orginalScore = groupFeedbackEntity.getScore();
    /* If all checks succeed, group feedback is updated succesfully */
    /* If fields are null, nothing is changed */
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PATCH)).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(argThat(
        json -> json.getScore() == groupFeedbackEntity.getScore() && json.getFeedback()
            .equals(groupFeedbackEntity.getFeedback())), eq(groupFeedbackEntity.getProjectId())))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity)).thenReturn(groupFeedbackJson);
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAllNull))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(groupFeedbackJson)));
    assertEquals(originalFeedback, groupFeedbackEntity.getFeedback());
    assertEquals(orginalScore, groupFeedbackEntity.getScore());
    verify(groupFeedbackRepository, times(1)).save(groupFeedbackEntity);
    /* If score is null, only feedback is updated */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PATCH)).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(argThat(
        json -> json.getScore() == groupFeedbackEntity.getScore() && json.getFeedback().equals("Heel goed gedaan")), eq(groupFeedbackEntity.getProjectId())))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    groupFeedbackJson.setFeedback("Heel goed gedaan");
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestScoreNull))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(groupFeedbackJson)));
    assertEquals("Heel goed gedaan", groupFeedbackEntity.getFeedback());
    assertEquals(orginalScore, groupFeedbackEntity.getScore());
    verify(groupFeedbackRepository, times(2)).save(groupFeedbackEntity);
    groupFeedbackEntity.setFeedback(originalFeedback);
    groupFeedbackJson.setFeedback(originalFeedback);
    /* If feedback is null, only score is updated */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PATCH)).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(argThat(
        json -> json.getScore() == 4.4F && json.getFeedback().equals(groupFeedbackEntity.getFeedback())), eq(groupFeedbackEntity.getProjectId())))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    groupFeedbackJson.setScore(4.4F);
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestFeedbackNull))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(groupFeedbackJson)));
    assertEquals(originalFeedback, groupFeedbackEntity.getFeedback());
    assertEquals(4.4F, groupFeedbackEntity.getScore());
    verify(groupFeedbackRepository, times(3)).save(groupFeedbackEntity);
    groupFeedbackEntity.setScore(orginalScore);
    groupFeedbackJson.setScore(orginalScore);
    /* If all fields are filled, both are updated */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PATCH)).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(argThat(
        json -> json.getScore() == 4.4F && json.getFeedback().equals("Heel goed gedaan")), eq(groupFeedbackEntity.getProjectId())))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    groupFeedbackJson.setFeedback("Heel goed gedaan");
    groupFeedbackJson.setScore(4.4F);
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(groupFeedbackJson)));
    assertEquals("Heel goed gedaan", groupFeedbackEntity.getFeedback());
    assertEquals(4.4F, groupFeedbackEntity.getScore());
    verify(groupFeedbackRepository, times(4)).save(groupFeedbackEntity);

    /* If an exception is thrown, return internal server error */
    doThrow(new RuntimeException()).when(groupFeedbackRepository).save(any());
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isInternalServerError());

    /* If json check fails, return corresponding status code */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PATCH)).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAllNull))
        .andExpect(status().isBadRequest());

    /* If group feedback check fails, return corresponding status code */
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PATCH)).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAllNull))
        .andExpect(status().isIAmATeapot());

  }

  @Test
  public void testUpdateGroupScorePut() throws Exception {
    String url = ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", ""+groupFeedbackEntity.getGroupId())
        .replace("{projectid}", ""+groupFeedbackEntity.getProjectId());
    String request = "{\"score\": 4.4,\"feedback\": \"Heel goed gedaan\"}";
    /* If all checks succeed, group feedback is updated succesfully */
    /* If all fields are filled, both are updated */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PUT)).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(argThat(
        json -> json.getScore() == 4.4F && json.getFeedback().equals("Heel goed gedaan")), eq(groupFeedbackEntity.getProjectId())))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity)).thenReturn(groupFeedbackJson);
    groupFeedbackJson.setFeedback("Heel goed gedaan");
    groupFeedbackJson.setScore(4.4F);
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(groupFeedbackJson)));
    assertEquals("Heel goed gedaan", groupFeedbackEntity.getFeedback());
    assertEquals(4.4F, groupFeedbackEntity.getScore());
    verify(groupFeedbackRepository, times(1)).save(groupFeedbackEntity);

    /* If an exception is thrown, return internal server error */
    doThrow(new RuntimeException()).when(groupFeedbackRepository).save(any());
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isInternalServerError());

    /* If json check fails, return corresponding status code */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PUT)).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());

    /* If group feedback check fails, return corresponding status code */
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PUT)).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isIAmATeapot());
  }

  @Test
  public void testDeleteGroupScore() throws Exception {
    String url = ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", ""+groupFeedbackEntity.getGroupId())
        .replace("{projectid}", ""+groupFeedbackEntity.getProjectId());
    /* If user can delete group feedback, delete it */
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(), HttpMethod.DELETE))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isOk());
    verify(groupFeedbackRepository, times(1)).delete(groupFeedbackEntity);

    /* If an exception is thrown, return internal server error */
    doThrow(new RuntimeException()).when(groupFeedbackRepository).delete(any());
    mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(status().isInternalServerError());

    /* If the groupfeedback can't be deleted by the user, return corresponding status code */
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isIAmATeapot());
  }



  @Test
  public void testAddGroupScore() throws Exception {
    String url = ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", ""+groupFeedbackEntity.getGroupId())
        .replace("{projectid}", ""+groupFeedbackEntity.getProjectId());
    String request = "{\"score\": " + groupFeedbackEntity.getScore() + ",\"feedback\": \"" + groupFeedbackEntity.getFeedback() + "\"}";
    /* If all checks succeed, group feedback is added succesfully */
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(), HttpMethod.POST))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(argThat(
        json -> Objects.equals(json.getScore(), groupFeedbackEntity.getScore()) && json.getFeedback().equals(groupFeedbackEntity.getFeedback())), eq(groupFeedbackEntity.getProjectId())))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupFeedbackRepository.save(any())).thenReturn(groupFeedbackEntity);
    when(entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity)).thenReturn(groupFeedbackJson);
    mockMvc.perform(MockMvcRequestBuilders.post(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(groupFeedbackJson)));
    verify(groupFeedbackRepository, times(1)).save(argThat(
        groupFeedback -> Objects.equals(groupFeedback.getScore(), groupFeedbackEntity.getScore()) &&
        groupFeedback.getFeedback().equals(groupFeedbackEntity.getFeedback()) &&
        groupFeedback.getGroupId() == groupFeedbackEntity.getGroupId() &&
        groupFeedback.getProjectId() == groupFeedbackEntity.getProjectId()));

    /* If an exception is thrown, return internal server error */
    reset(groupFeedbackRepository);
    when(groupFeedbackRepository.save(any())).thenThrow(new RuntimeException());
    mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isInternalServerError());

    /* If json check fails, return corresponding status code */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());

    /* If user can't add group feedback, return corresponding status code */
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any())).thenReturn(
        new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
    mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testGetGroupScore() throws Exception {
    String url = ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", ""+groupFeedbackEntity.getGroupId())
        .replace("{projectid}", ""+groupFeedbackEntity.getProjectId());
    /* If all checks succeed, group feedback is returned */
    when(groupFeedbackUtil.checkGroupFeedback(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity)).thenReturn(groupFeedbackJson);
    when(groupUtil.canGetProjectGroupData(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupFeedbackUtil.getGroupFeedbackIfExists(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity)).thenReturn(groupFeedbackJson);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(groupFeedbackJson)));

    /* If feedback doesn't exist, return not found */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedback(anyLong(), anyLong()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupFeedbackUtil.getGroupFeedbackIfExists(anyLong(), anyLong()))
        .thenReturn(new CheckResult<>(HttpStatus.NOT_FOUND, "", null));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isNotFound());

    /* User can't get project group data, return forbidden */
    reset(groupUtil);
    when(groupUtil.canGetProjectGroupData(anyLong(), anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isForbidden());

    /* If check fails, return corresponding status code */
    when(groupFeedbackUtil.checkGroupFeedback(anyLong(), anyLong()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isIAmATeapot());
  }

  @Test
  public void testGetCourseGrades() throws Exception {
    CourseEntity courseEntity = new CourseEntity("Test course", "TestCourseDescription", 2013);
    courseEntity.setId(99L);
    ProjectEntity project1 = new ProjectEntity(courseEntity.getId(),"Test project", "TestProjectDescription", 1L, 11L, true, 44, OffsetDateTime.now());
    ProjectEntity project2 = new ProjectEntity(courseEntity.getId(),"Test project", "TestProjectDescription", 2L, 11L, true, 44, OffsetDateTime.now());
    project2.setId(1L);
    project1.setId(2L);
    long project1GroupId = 4L;
    long project2GroupId = 5L;
    GroupFeedbackJsonWithProject groupFeedbackJsonWithProject1 = new GroupFeedbackJsonWithProject(project1.getName(), "UrlOfProject1", project1.getId(), groupFeedbackJson, project1.getMaxScore());
    GroupFeedbackJsonWithProject groupFeedbackJsonWithProject2 = new GroupFeedbackJsonWithProject(project2.getName(), "UrlOfProject2", project2.getId(), null, project2.getMaxScore());
    List<GroupFeedbackJsonWithProject> groupFeedbackJsonWithProjects = List.of(groupFeedbackJsonWithProject1, groupFeedbackJsonWithProject2);
    String url = ApiRoutes.COURSE_BASE_PATH + "/" + courseEntity.getId() + "/grades";
    /* If all checks succeed, course grades are returned */
    when(courseUtil.getCourseIfUserInCourse(courseEntity.getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, CourseRelation.enrolled)));
    when(projectRepository.findByCourseId(courseEntity.getId()))
        .thenReturn(List.of(project1, project2));
    when(groupRepository.groupIdByProjectAndUser(project1.getId(), getMockUser().getId()))
        .thenReturn(project1GroupId);
    when(groupRepository.groupIdByProjectAndUser(project2.getId(), getMockUser().getId()))
        .thenReturn(project2GroupId);
    when(groupFeedbackUtil.getGroupFeedbackIfExists(project1GroupId, project1.getId()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.getGroupFeedbackIfExists(project2GroupId, project2.getId()))
        .thenReturn(new CheckResult<>(HttpStatus.NOT_FOUND, "", null));
    when(entityToJsonConverter.groupFeedbackEntityToJsonWithProject(groupFeedbackEntity, project1))
        .thenReturn(groupFeedbackJsonWithProject1);
    when(entityToJsonConverter.groupFeedbackEntityToJsonWithProject(null, project2))
        .thenReturn(groupFeedbackJsonWithProject2);

    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(groupFeedbackJsonWithProjects)));

    /* If project is not visible, filter it out */
    project2.setVisible(false);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(groupFeedbackJsonWithProject1))));
    project2.setVisible(true);

    /* If user is not yet in group also have null as group feedback */
    when(groupRepository.groupIdByProjectAndUser(project1.getId(), getMockUser().getId()))
        .thenReturn(null);
    GroupFeedbackJsonWithProject project1NoGroup = new GroupFeedbackJsonWithProject(project1.getName(), "UrlOfProject1", project1.getId(), null, project1.getMaxScore());
    groupFeedbackJsonWithProjects = List.of(project1NoGroup, groupFeedbackJsonWithProject2);
    when(entityToJsonConverter.groupFeedbackEntityToJsonWithProject(null, project1))
        .thenReturn(project1NoGroup);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(groupFeedbackJsonWithProjects)));

    /* If user isn't enrolled in the course, return BAD REQUEST */
    when(courseUtil.getCourseIfUserInCourse(courseEntity.getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, CourseRelation.course_admin)));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isBadRequest());

    /* If course check fails, return corresponding status code */
    when(courseUtil.getCourseIfUserInCourse(courseEntity.getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isIAmATeapot());
  }
}
