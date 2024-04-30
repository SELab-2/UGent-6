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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.GroupFeedbackUtil;
import com.ugent.pidgeon.util.GroupUtil;
import java.util.Objects;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class GroupFeedbackControllerTest extends ControllerTest {

  @Mock
  private GroupFeedbackRepository groupFeedbackRepository;
  @Mock
  private GroupFeedbackUtil groupFeedbackUtil;
  @Mock
  private GroupUtil groupUtil;
  @Mock
  private EntityToJsonConverter entityToJsonConverter;

  @InjectMocks
  private GroupFeedbackController groupFeedbackController;

  private GroupFeedbackEntity groupFeedbackEntity;

  @BeforeEach
  public void setup() {
    setUpController(groupFeedbackController);
    groupFeedbackEntity = new GroupFeedbackEntity(4L, 6L, 0F, "good job.... NOT!");
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
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestAllNull))
        .andExpect(status().isOk());
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
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestScoreNull))
        .andExpect(status().isOk());
    assertEquals("Heel goed gedaan", groupFeedbackEntity.getFeedback());
    assertEquals(orginalScore, groupFeedbackEntity.getScore());
    verify(groupFeedbackRepository, times(2)).save(groupFeedbackEntity);
    groupFeedbackEntity.setFeedback(originalFeedback);
    /* If feedback is null, only score is updated */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PATCH)).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(argThat(
        json -> json.getScore() == 4.4F && json.getFeedback().equals(groupFeedbackEntity.getFeedback())), eq(groupFeedbackEntity.getProjectId())))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestFeedbackNull))
        .andExpect(status().isOk());
    assertEquals(originalFeedback, groupFeedbackEntity.getFeedback());
    assertEquals(4.4F, groupFeedbackEntity.getScore());
    verify(groupFeedbackRepository, times(3)).save(groupFeedbackEntity);
    groupFeedbackEntity.setScore(orginalScore);
    /* If all fields are filled, both are updated */
    reset(groupFeedbackUtil);
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), getMockUser(),
        HttpMethod.PATCH)).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(argThat(
        json -> json.getScore() == 4.4F && json.getFeedback().equals("Heel goed gedaan")), eq(groupFeedbackEntity.getProjectId())))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());
    assertEquals("Heel goed gedaan", groupFeedbackEntity.getFeedback());
    assertEquals(4.4F, groupFeedbackEntity.getScore());
    verify(groupFeedbackRepository, times(4)).save(groupFeedbackEntity);

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
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());
    assertEquals("Heel goed gedaan", groupFeedbackEntity.getFeedback());
    assertEquals(4.4F, groupFeedbackEntity.getScore());
    verify(groupFeedbackRepository, times(1)).save(groupFeedbackEntity);


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
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    mockMvc.perform(MockMvcRequestBuilders.delete(
            ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1")))
        .andExpect(status().isOk());

    doThrow(new RuntimeException()).when(groupFeedbackRepository).delete(any());
    mockMvc.perform(MockMvcRequestBuilders.delete(
            ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1")))
        .andExpect(status().isInternalServerError());

    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.delete(
            ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1")))
        .andExpect(status().isIAmATeapot());
  }



  @Test
  public void testAddGroupScore() throws Exception {
    String request = "{\"score\": 4.4,\"feedback\": \"Heel goed gedaan\"}";
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.post(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isCreated());

    when(groupFeedbackRepository.save(any())).thenThrow(new RuntimeException());
    mockMvc.perform(MockMvcRequestBuilders.post(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isInternalServerError());

    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.post(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());

    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any())).thenReturn(
        new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
    mockMvc.perform(MockMvcRequestBuilders.post(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testGetGroupScore() throws Exception {
    when(groupFeedbackUtil.checkGroupFeedback(anyLong(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    when(groupUtil.canGetProjectGroupData(anyLong(), anyLong(), any())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    when(groupFeedbackUtil.getGroupFeedbackIfExists(anyLong(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    mockMvc.perform(MockMvcRequestBuilders.get(
            ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1")))
        .andExpect(status().isOk());

    when(groupFeedbackUtil.getGroupFeedbackIfExists(anyLong(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.get(
            ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1")))
        .andExpect(status().isBadRequest());

    when(groupUtil.canGetProjectGroupData(anyLong(), anyLong(), any())).thenReturn(
        new CheckResult<>(HttpStatus.CONFLICT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.get(
            ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1")))
        .andExpect(status().isConflict());

    when(groupFeedbackUtil.checkGroupFeedback(anyLong(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "", null));
    mockMvc.perform(MockMvcRequestBuilders.get(
            ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1")))
        .andExpect(status().isBandwidthLimitExceeded());
  }
}
