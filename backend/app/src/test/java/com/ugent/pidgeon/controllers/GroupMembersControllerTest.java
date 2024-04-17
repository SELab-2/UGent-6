package com.ugent.pidgeon.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupMemberRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.GroupUtil;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class GroupMembersControllerTest extends ControllerTest {

  @Mock
  private GroupMemberRepository groupMemberRepository;
  @Mock
  private GroupUtil groupUtil;
  @Mock
  private EntityToJsonConverter entityToJsonConverter;

  @InjectMocks
  private GroupMemberController groupMemberController;

  private UserEntity userEntity;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(groupMemberController)
        .defaultRequest(MockMvcRequestBuilders.get("/**")
            .with(request -> {
              request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
              return request;
            }))
        .build();
    userEntity = new UserEntity("name", "surname", "email", UserRole.student, "azureid");
    userEntity.setId(1L);
  }

  @Test
  public void testRemoveMemberFromGroup() throws Exception {
    when(groupUtil.canRemoveUserFromGroup(anyLong(), anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupMemberRepository.removeMemberFromGroup(anyLong(), anyLong())).thenReturn(1);
    mockMvc.perform(MockMvcRequestBuilders.delete(
            ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1") + "/1"))
        .andExpect(status().isNoContent());

    when(groupMemberRepository.removeMemberFromGroup(anyLong(), anyLong())).thenReturn(0);
    mockMvc.perform(MockMvcRequestBuilders.delete(
            ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1") + "/1"))
        .andExpect(status().isInternalServerError());

    when(groupUtil.canRemoveUserFromGroup(anyLong(), anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.delete(
            ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1") + "/1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testRemoveMemberFromGroupInferred() throws Exception {
    when(groupUtil.canRemoveUserFromGroup(anyLong(), anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupMemberRepository.removeMemberFromGroup(anyLong(), anyLong())).thenReturn(1);
    mockMvc.perform(
            MockMvcRequestBuilders.delete(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1")))
        .andExpect(status().isNoContent());

    when(groupMemberRepository.removeMemberFromGroup(anyLong(), anyLong())).thenReturn(0);
    mockMvc.perform(
            MockMvcRequestBuilders.delete(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1")))
        .andExpect(status().isInternalServerError());

    when(groupUtil.canRemoveUserFromGroup(anyLong(), anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(
            MockMvcRequestBuilders.delete(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1")))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testAddMemberToGroup() throws Exception {
    when(groupUtil.canAddUserToGroup(anyLong(), anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupMemberRepository.findAllMembersByGroupId(anyLong()))
        .thenReturn(List.of(userEntity));
    mockMvc.perform(MockMvcRequestBuilders.post(
            ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1") + "/1"))
        .andExpect(status().isOk());

    when(groupMemberRepository.findAllMembersByGroupId(anyLong())).thenThrow(
        new RuntimeException());
    mockMvc.perform(MockMvcRequestBuilders.post(
            ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1") + "/1"))
        .andExpect(status().isInternalServerError());

    when(groupUtil.canAddUserToGroup(anyLong(), anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.post(
            ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1") + "/1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testAddMemberToGroupInferred() throws Exception {
    when(groupUtil.canAddUserToGroup(anyLong(), anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupMemberRepository.findAllMembersByGroupId(anyLong()))
        .thenReturn(List.of(userEntity));
    mockMvc.perform(
            MockMvcRequestBuilders.post(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1")))
        .andExpect(status().isOk());

    when(groupMemberRepository.findAllMembersByGroupId(anyLong())).thenThrow(
        new RuntimeException());
    mockMvc.perform(
            MockMvcRequestBuilders.post(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1")))
        .andExpect(status().isInternalServerError());

    when(groupUtil.canAddUserToGroup(anyLong(), anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(
            MockMvcRequestBuilders.post(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1")))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testFindAllMembersByGroupId() throws Exception {
    when(groupUtil.canGetGroup(anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupMemberRepository.findAllMembersByGroupId(anyLong()))
        .thenReturn(List.of(userEntity));
    mockMvc.perform(
            MockMvcRequestBuilders.get(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1")))
        .andExpect(status().isOk());

    when(groupUtil.canGetGroup(anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(
            MockMvcRequestBuilders.get(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1")))
        .andExpect(status().isBadRequest());
  }
}