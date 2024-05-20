package com.ugent.pidgeon.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.json.UserReferenceJson;
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
import org.springframework.http.MediaType;
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

  private ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();

  private UserEntity userEntity;
  private UserEntity userEntity2;
  private UserReferenceJson userReferenceJson;
  private UserReferenceJson userReferenceJson2;
  private final long groupId = 10L;

  @BeforeEach
  public void setup() {
    setUpController(groupMemberController);
    userEntity = new UserEntity("name", "surname", "email", UserRole.student, "azureid", "");
    userEntity.setId(5L);
    userEntity2 = new UserEntity("name2", "surname2", "email2", UserRole.student, "azureid2", "");
    userEntity2.setId(6L);
    userReferenceJson = new UserReferenceJson(userEntity.getName(), userEntity.getEmail(), userEntity.getId(), "");
    userReferenceJson2 = new UserReferenceJson(userEntity2.getName(), userEntity2.getEmail(), userEntity2.getId(), "");
  }

  @Test
  public void testRemoveMemberFromGroup() throws Exception {
    String url = ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", ""+groupId) + "/" + userEntity.getId();
    /* If all checks pass, the user is removed from the group */
    when(groupUtil.canRemoveUserFromGroup(groupId, userEntity.getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupMemberRepository.removeMemberFromGroup(groupId, userEntity.getId())).thenReturn(1);
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isNoContent());
    verify(groupMemberRepository, times(1)).removeMemberFromGroup(groupId, userEntity.getId());

    /* If something goes wrong return internal server error */
    when(groupMemberRepository.removeMemberFromGroup(groupId, userEntity.getId())).thenReturn(0);
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isInternalServerError());

    /* If use can't be removed from group return corresponding status */
    when(groupUtil.canRemoveUserFromGroup(groupId, userEntity.getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testRemoveMemberFromGroupInferred() throws Exception {
    String url = ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", ""+groupId);
    /* If all checks pass, the user is removed from the group */
    when(groupUtil.canRemoveUserFromGroup(groupId, getMockUser().getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupMemberRepository.removeMemberFromGroup(groupId, getMockUser().getId())).thenReturn(1);
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isNoContent());
    verify(groupMemberRepository, times(1)).removeMemberFromGroup(groupId, getMockUser().getId());

    /* If something goes wrong return internal server error */
    when(groupMemberRepository.removeMemberFromGroup(groupId, getMockUser().getId())).thenReturn(0);
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isInternalServerError());

    /* If use can't be removed from group return corresponding status */
    when(groupUtil.canRemoveUserFromGroup(groupId, getMockUser().getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.delete(url))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testAddMemberToGroup() throws Exception {
    String url = ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", ""+groupId) + "/" + userEntity.getId();

    /* If all checks succeed, the user is added to the group */
    when(groupUtil.canAddUserToGroup(groupId, userEntity.getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupMemberRepository.findAllMembersByGroupId(groupId))
        .thenReturn(List.of(userEntity, userEntity2));
    when(entityToJsonConverter.userEntityToUserReference(userEntity, false)).thenReturn(userReferenceJson);
    when(entityToJsonConverter.userEntityToUserReference(userEntity2, false)).thenReturn(userReferenceJson2);
    mockMvc.perform(MockMvcRequestBuilders.post(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userReferenceJson, userReferenceJson2))));
    verify(groupMemberRepository, times(1)).addMemberToGroup(groupId, userEntity.getId());

    /* If something goes wrong return internal server error */
    when(groupMemberRepository.addMemberToGroup(groupId, userEntity.getId())).thenThrow(new RuntimeException());
    mockMvc.perform(MockMvcRequestBuilders.post(url))
        .andExpect(status().isInternalServerError());

    /* If user can't be added to group return corresponding status */
    when(groupUtil.canAddUserToGroup(groupId, userEntity.getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.post(url))
        .andExpect(status().isIAmATeapot());

  }

  @Test
  public void testAddMemberToGroupInferred() throws Exception {
    String url = ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", ""+groupId);
    UserReferenceJson mockUserJson = new UserReferenceJson(getMockUser().getName(), getMockUser().getEmail(), getMockUser().getId(), getMockUser().getStudentNumber());

    /* If all checks succeed, the user is added to the group */
    when(groupUtil.canAddUserToGroup(groupId, getMockUser().getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupMemberRepository.findAllMembersByGroupId(groupId))
        .thenReturn(List.of(getMockUser(), userEntity2));
    when(entityToJsonConverter.userEntityToUserReference(getMockUser(), true)).thenReturn(mockUserJson);
    when(entityToJsonConverter.userEntityToUserReference(userEntity2, true)).thenReturn(userReferenceJson2);
    mockMvc.perform(MockMvcRequestBuilders.post(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(mockUserJson, userReferenceJson2))));
    verify(groupMemberRepository, times(1)).addMemberToGroup(groupId, getMockUser().getId());

    /* If something goes wrong return internal server error */
    when(groupMemberRepository.addMemberToGroup(groupId, getMockUser().getId())).thenThrow(new RuntimeException());
    mockMvc.perform(MockMvcRequestBuilders.post(url))
        .andExpect(status().isInternalServerError());

    /* If user can't be added to group return corresponding status */
    when(groupUtil.canAddUserToGroup(groupId, getMockUser().getId(), getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.post(url))
        .andExpect(status().isIAmATeapot());
  }

  @Test
  public void testFindAllMembersByGroupId() throws Exception {
    String url = ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", ""+groupId);
    List<UserEntity> members = List.of(userEntity, userEntity2);
    List<UserReferenceJson> userReferenceJsons = List.of(userReferenceJson, userReferenceJson2);
    when(groupMemberRepository.findAllMembersByGroupId(groupId)).thenReturn(members);
    /* User is admin of group so don't hide studentNumbers */
    when(groupUtil.isAdminOfGroup(groupId, getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(entityToJsonConverter.userEntityToUserReference(userEntity, false)).thenReturn(userReferenceJson);
    when(entityToJsonConverter.userEntityToUserReference(userEntity2, false)).thenReturn(userReferenceJson2);

    /* If user can get group return list of members */
    when(groupUtil.canGetGroup(groupId, getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(userReferenceJsons)));

    verify(entityToJsonConverter, times(1)).userEntityToUserReference(userEntity, false);
    verify(entityToJsonConverter, times(1)).userEntityToUserReference(userEntity2, false);

    /* If user isn't admin, studentNumbers should be hidden */
    when(groupUtil.isAdminOfGroup(groupId, getMockUser())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    when(entityToJsonConverter.userEntityToUserReference(userEntity, true)).thenReturn(userReferenceJson);
    when(entityToJsonConverter.userEntityToUserReference(userEntity2, true)).thenReturn(userReferenceJson2);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(userReferenceJsons)));

    verify(entityToJsonConverter, times(1)).userEntityToUserReference(userEntity, true);
    verify(entityToJsonConverter, times(1)).userEntityToUserReference(userEntity2, true);
    
    /* If use can't get group return corresponding status */
    when(groupUtil.canGetGroup(groupId, getMockUser()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isIAmATeapot());
  }
}