package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class GroupUtilTest {

  @Mock
  private GroupRepository groupRepository;
  @Mock
  private GroupClusterRepository groupClusterRepository;
  @Mock
  private ClusterUtil clusterUtil;
  @Mock
  private ProjectUtil projectUtil;

  @InjectMocks
  private GroupUtil groupUtil;

  private GroupEntity group;
  private UserEntity user;
  private GroupClusterEntity groupCluster;
  private ProjectEntity project;

  @BeforeEach
  public void setup() {
    group = new GroupEntity("Groupname", 1L);
    group.setId(1L);
    user = new UserEntity("name", "surname", "email", UserRole.student, "azureid");
    user.setId(1L);
    groupCluster = new GroupClusterEntity(1L, 5, "cluster test", 20);
    groupCluster.setId(1L);
    project = new ProjectEntity(1L, "name", "description", 1L, 1L, true, 20, OffsetDateTime.now());
    project.setId(1L);
  }

  @Test
  public void testGetGroupIfExists() throws Exception {
    when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
    CheckResult<GroupEntity> result = groupUtil.getGroupIfExists(1L);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(group, result.getData());

    result = groupUtil.getGroupIfExists(2L);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
    assertEquals("Group not found", result.getMessage());
  }

  @Test
  public void testCanGetGroup() throws Exception {
    when(groupRepository.userAccessToGroup(1L, 1L)).thenReturn(true);
    CheckResult<Void> result = groupUtil.canGetGroup(1L, user);
    assertEquals(HttpStatus.OK, result.getStatus());

    result = groupUtil.canGetGroup(2L, user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    assertEquals("User does not have access to this group", result.getMessage());
  }

  @Test
  public void testIsAdminOfGroup() throws Exception {
    when(groupRepository.isAdminOfGroup(1L, user.getId())).thenReturn(true);
    CheckResult<Void> result = groupUtil.isAdminOfGroup(1L, user);
    assertEquals(HttpStatus.OK, result.getStatus());

    result = groupUtil.isAdminOfGroup(2L, user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    assertEquals("User is not an admin of this group", result.getMessage());
  }

  @Test
  public void testCanUpdateGroup() throws Exception {
    when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
    when(groupRepository.isAdminOfGroup(1L, user.getId())).thenReturn(true);
    when(clusterUtil.isIndividualCluster(1L)).thenReturn(false);
    CheckResult<GroupEntity> result = groupUtil.canUpdateGroup(group.getId(), user);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(group, result.getData());

    when(clusterUtil.isIndividualCluster(1L)).thenReturn(true);
    result = groupUtil.canUpdateGroup(group.getId(), user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    assertEquals("Cannot update individual group", result.getMessage());

    when(groupRepository.isAdminOfGroup(1L, user.getId())).thenReturn(false);
    result = groupUtil.canUpdateGroup(group.getId(), user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    assertEquals("User is not an admin of this group", result.getMessage());

    when(groupRepository.findById(1L)).thenReturn(Optional.empty());
    result = groupUtil.canUpdateGroup(group.getId(), user);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
    assertEquals("Group not found", result.getMessage());
  }

  @Test
  public void TestCanAddUserToGroup() throws Exception {
    when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
    when(groupRepository.isAdminOfGroup(1L, user.getId())).thenReturn(true);
    when(groupClusterRepository.userInGroupForCluster(anyLong(), anyLong())).thenReturn(false);
    when(groupRepository.userInGroup(anyLong(), anyLong())).thenReturn(false);
    when(clusterUtil.getClusterIfExists(group.getClusterId()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupCluster));
    when(groupRepository.countUsersInGroup(group.getId())).thenReturn(1);
    when(clusterUtil.isIndividualCluster(groupCluster.getId())).thenReturn(false);

    CheckResult<Void> result = groupUtil.canAddUserToGroup(group.getId(), 2L, user);
    assertEquals(HttpStatus.OK, result.getStatus());
  }

  @Test
  public void testCanRemoveUserFromGroup() throws Exception {
    when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
    when(groupRepository.isAdminOfGroup(1L, user.getId())).thenReturn(true);
    when(groupRepository.userInGroup(1L, 2L)).thenReturn(true);
    when(clusterUtil.isIndividualCluster(groupCluster.getId())).thenReturn(false);
    CheckResult<Void> result = groupUtil.canRemoveUserFromGroup(group.getId(), 2L, user);
    assertEquals(HttpStatus.OK, result.getStatus());

    when(clusterUtil.isIndividualCluster(groupCluster.getId())).thenReturn(true);
    result = groupUtil.canRemoveUserFromGroup(group.getId(), 2L, user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    assertEquals("Cannot remove user from individual group", result.getMessage());

    when(groupRepository.userInGroup(1L, 2L)).thenReturn(false);
    result = groupUtil.canRemoveUserFromGroup(group.getId(), 2L, user);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
    assertEquals("User is not in the group", result.getMessage());

    when(groupRepository.isAdminOfGroup(1L, user.getId())).thenReturn(false);
    result = groupUtil.canRemoveUserFromGroup(group.getId(), 2L, user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    assertEquals("User is not an admin of this group", result.getMessage());

    when(groupRepository.findById(1L)).thenReturn(Optional.empty());
    result = groupUtil.canRemoveUserFromGroup(group.getId(), 2L, user);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
    assertEquals("Group not found", result.getMessage());
  }

  @Test
  public void testCanGetProjectGroupData() throws Exception {
    when(projectUtil.getProjectIfExists(project.getId()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", project));
    when(groupRepository.findByIdAndClusterId(group.getId(), project.getGroupClusterId()))
        .thenReturn(Optional.of(group));
    when(groupRepository.userInGroup(group.getId(), user.getId())).thenReturn(true);
    when(projectUtil.isProjectAdmin(project.getId(), user))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    CheckResult<Void> result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(),
        user);
    assertEquals(HttpStatus.OK, result.getStatus());

    when(groupRepository.userInGroup(group.getId(), user.getId())).thenReturn(false);
    when(projectUtil.isProjectAdmin(project.getId(), user))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(), user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    assertEquals("User does not have access to the submissions of the group", result.getMessage());

    when(groupRepository.findByIdAndClusterId(group.getId(), project.getGroupClusterId()))
        .thenReturn(Optional.empty());
    result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(), user);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
    assertEquals("Group not part of the project", result.getMessage());

    when(projectUtil.getProjectIfExists(project.getId()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", project));
    result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(), user);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());
    assertEquals("", result.getMessage());
  }
}
