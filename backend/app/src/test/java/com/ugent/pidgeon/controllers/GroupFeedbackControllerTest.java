package com.ugent.pidgeon.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.GroupFeedbackUtil;
import com.ugent.pidgeon.util.GroupUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    mockMvc = MockMvcBuilders.standaloneSetup(groupFeedbackController)
        .defaultRequest(MockMvcRequestBuilders.get("/**")
            .with(request -> {
              request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
              return request;
            }))
        .build();
    groupFeedbackEntity = new GroupFeedbackEntity(1L, 1L, 0F, "good job.... NOT!");
  }

  @Test
  public void testUpdateGroupScore() throws Exception {
    String request = "{\"score\": null,\"feedback\": null}";
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());

    when(groupFeedbackRepository.save(any())).thenThrow(new RuntimeException());
    mockMvc.perform(MockMvcRequestBuilders.patch(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isInternalServerError());

    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isIAmATeapot());

    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any())).thenReturn(
        new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isForbidden());
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
  public void testUpdateGroupScorePut() throws Exception {
    String request = "{\"score\": 4.4,\"feedback\": \"Heel goed gedaan\"}";
    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupFeedbackEntity));
    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.put(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());

    when(groupFeedbackUtil.checkGroupFeedbackUpdateJson(any(), anyLong())).thenReturn(
        new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.put(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());

    when(groupFeedbackUtil.checkGroupFeedbackUpdate(anyLong(), anyLong(), any(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.put(
                ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
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

