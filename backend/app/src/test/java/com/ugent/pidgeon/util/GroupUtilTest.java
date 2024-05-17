package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
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
import org.mockito.Spy;
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
  @Mock
  private UserUtil userUtil;

  @Spy
  @InjectMocks
  private GroupUtil groupUtil;

  private GroupEntity group;
  private UserEntity mockUser;
  private GroupClusterEntity groupCluster;
  private ProjectEntity project;

  @BeforeEach
  public void setup() {
    group = new GroupEntity("Groupname", 12L);
    group.setId(54L);
    mockUser = new UserEntity("name", "surname", "email", UserRole.student, "azureid");
    mockUser.setId(10L);
    groupCluster = new GroupClusterEntity(9L, 5, "cluster test", 20);
    groupCluster.setId(12L);
    project = new ProjectEntity(9L, "name", "description", 12L, null, true, 20, OffsetDateTime.now());
    project.setId(88L);
  }

  @Test
  public void testGetGroupIfExists() {
    /* Group exists */
    when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
    CheckResult<GroupEntity> result = groupUtil.getGroupIfExists(group.getId());
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(group, result.getData());

    /* Group does not exist */
    when(groupRepository.findById(2L)).thenReturn(Optional.empty());
    result = groupUtil.getGroupIfExists(2L);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }

  @Test
  public void testCanGetGroup() {
    /* User has access to group */
    when(groupRepository.userAccessToGroup(mockUser.getId(), group.getId())).thenReturn(true);
    CheckResult<Void> result = groupUtil.canGetGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* User doesn't have access to group */
    when(groupRepository.userAccessToGroup(mockUser.getId(), group.getId())).thenReturn(false);
    result = groupUtil.canGetGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* User has no acces but is admin */
    mockUser.setRole(UserRole.admin);
    result = groupUtil.canGetGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());
  }

  @Test
  public void testIsAdminOfGroup() {
    /* User is admin of group */
    when(groupRepository.isAdminOfGroup(mockUser.getId(), group.getId())).thenReturn(true);
    CheckResult<Void> result = groupUtil.isAdminOfGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* User is not admin of group */
    when(groupRepository.isAdminOfGroup(mockUser.getId(), group.getId())).thenReturn(false);
    result = groupUtil.isAdminOfGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* User is admin */
    mockUser.setRole(UserRole.admin);
    result = groupUtil.isAdminOfGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());
  }

  @Test
  public void testCanUpdateGroup() {
    /* All checks succeed */
    doReturn(new CheckResult<>(HttpStatus.OK, "", group)).when(groupUtil).getGroupIfExists(group.getId());
    doReturn(new CheckResult<>(HttpStatus.OK, "", null)).when(groupUtil).isAdminOfGroup(group.getId(), mockUser);
    when(clusterUtil.isIndividualCluster(group.getClusterId())).thenReturn(false);
    CheckResult<GroupEntity> result = groupUtil.canUpdateGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(group, result.getData());

    /* Group is individual cluster */
    when(clusterUtil.isIndividualCluster(group.getClusterId())).thenReturn(true);
    result = groupUtil.canUpdateGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* User is not admin of group */
    doReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "User is not an admin of this group", null)).when(groupUtil).isAdminOfGroup(group.getId(), mockUser);
    result = groupUtil.canUpdateGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Group does not exist */
    doReturn(new CheckResult<>(HttpStatus.NOT_FOUND, "Group not found", null)).when(groupUtil).getGroupIfExists(group.getId());
    result = groupUtil.canUpdateGroup(group.getId(), mockUser);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }

  @Test
  public void TestCanAddUserToGroup() {
    long otherUserId = 5L;
    UserEntity otherUser = new UserEntity("othername", "othersurname", "otheremail", UserRole.student, "otherazureid");
    /* All checks succeed */
    /* Trying to add yourself to the group */
    when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
    when(groupRepository.userAccessToGroup(mockUser.getId(), group.getId())).thenReturn(true);
    when(groupClusterRepository.inArchivedCourse(group.getClusterId())).thenReturn(false);
    when(userUtil.getUserIfExists(mockUser.getId())).thenReturn(mockUser);
    when(groupClusterRepository.userInGroupForCluster(group.getClusterId(), mockUser.getId())).thenReturn(false);
    when(groupRepository.userInGroup(group.getId(), mockUser.getId())).thenReturn(false);
    when(clusterUtil.getClusterIfExists(group.getClusterId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupCluster));
    when(groupRepository.countUsersInGroup(group.getId())).thenReturn(groupCluster.getMaxSize() - 1);
    when(clusterUtil.isIndividualCluster(group.getClusterId())).thenReturn(false);
    doReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null)).
        when(groupUtil).isAdminOfGroup(group.getId(), mockUser);

    CheckResult<Void> result = groupUtil.canAddUserToGroup(group.getId(), mockUser.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Trying to add someone else as admin */
    doReturn(new CheckResult<>(HttpStatus.OK, "", null)).
        when(groupUtil).isAdminOfGroup(group.getId(), mockUser);
    when(userUtil.getUserIfExists(otherUserId)).thenReturn(otherUser);
    when(groupClusterRepository.userInGroupForCluster(group.getClusterId(), otherUserId)).thenReturn(false);
    when(groupRepository.userInGroup(group.getId(), otherUserId)).thenReturn(false);
    doReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null)).
        when(groupUtil).isAdminOfGroup(group.getId(), otherUser);
    result = groupUtil.canAddUserToGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Group is already full but it's an admin adding someone else */
    when(groupRepository.countUsersInGroup(group.getId())).thenReturn(groupCluster.getMaxSize());
    result = groupUtil.canAddUserToGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());
    when(groupRepository.countUsersInGroup(group.getId())).thenReturn(groupCluster.getMaxSize()-1);

    /* User trying to add is admin */
    doReturn(new CheckResult<>(HttpStatus.OK, "", null)).
        when(groupUtil).isAdminOfGroup(group.getId(), otherUser);
    result = groupUtil.canAddUserToGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Cluster is individual */
    when(clusterUtil.isIndividualCluster(group.getClusterId())).thenReturn(true);
    result = groupUtil.canAddUserToGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Group is already full */
    when(groupRepository.countUsersInGroup(group.getId())).thenReturn(groupCluster.getMaxSize());
    result = groupUtil.canAddUserToGroup(group.getId(), mockUser.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* ClusterEntity is not found */
    when(clusterUtil.getClusterIfExists(group.getClusterId())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    result = groupUtil.canAddUserToGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());

    /* User is already in that group */
    when(groupRepository.userInGroup(group.getId(), otherUserId)).thenReturn(true);
    result = groupUtil.canAddUserToGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* User is already in group for cluster */
    when(groupClusterRepository.userInGroupForCluster(group.getClusterId(), otherUserId)).thenReturn(true);
    result = groupUtil.canAddUserToGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* User to add doesn't exist */
    when(userUtil.getUserIfExists(otherUserId)).thenReturn(null);
    result = groupUtil.canAddUserToGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

    /* User trying to add a different user while not being admin */
    doReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null)).
        when(groupUtil).isAdminOfGroup(group.getId(), mockUser);
    result = groupUtil.canAddUserToGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* User trying to join group in archived course */
    when(groupClusterRepository.inArchivedCourse(group.getClusterId())).thenReturn(true);
    result = groupUtil.canAddUserToGroup(group.getId(), mockUser.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* User trying to join group they don't have acces too */
    when(groupRepository.userAccessToGroup(mockUser.getId(), group.getId())).thenReturn(false);
    result = groupUtil.canAddUserToGroup(group.getId(), mockUser.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Group not found */
    when(groupRepository.findById(group.getId())).thenReturn(Optional.empty());
    result = groupUtil.canAddUserToGroup(group.getId(), mockUser.getId(), mockUser);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }

  @Test
  public void testCanRemoveUserFromGroup() throws Exception {
    /* All checks succeed */
    /* Trying to leave group */
    when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
    when(groupClusterRepository.inArchivedCourse(group.getClusterId())).thenReturn(false);
    when(groupRepository.userInGroup(group.getId(), mockUser.getId())).thenReturn(true);
    when(clusterUtil.isIndividualCluster(group.getClusterId())).thenReturn(false);

    CheckResult<Void> result = groupUtil.canRemoveUserFromGroup(group.getId(), mockUser.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Trying to remove someone else */
    long otherUserId = 5L;
    doReturn(new CheckResult<>(HttpStatus.OK, "", null)).
        when(groupUtil).isAdminOfGroup(group.getId(), mockUser);
    when(groupRepository.userInGroup(group.getId(), otherUserId)).thenReturn(true);

    result = groupUtil.canRemoveUserFromGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Individual cluster */
    when(clusterUtil.isIndividualCluster(group.getClusterId())).thenReturn(true);
    result = groupUtil.canRemoveUserFromGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* User is not in group */
    when(groupRepository.userInGroup(group.getId(), otherUserId)).thenReturn(false);
    result = groupUtil.canRemoveUserFromGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

    /* Trying to leave group in archived course */
    when(groupClusterRepository.inArchivedCourse(group.getClusterId())).thenReturn(true);
    result = groupUtil.canRemoveUserFromGroup(group.getId(), mockUser.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Trying to add someone else while not admin */
    doReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null)).
        when(groupUtil).isAdminOfGroup(group.getId(), mockUser);
    result = groupUtil.canRemoveUserFromGroup(group.getId(), otherUserId, mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Group not found */
    when(groupRepository.findById(group.getId())).thenReturn(Optional.empty());
    result = groupUtil.canRemoveUserFromGroup(group.getId(), mockUser.getId(), mockUser);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }

  @Test
  public void testCanGetProjectGroupData() throws Exception {
    /* All checks succeed */
    when(projectUtil.getProjectIfExists(project.getId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", project));
    when(groupRepository.findByIdAndClusterId(group.getId(), project.getGroupClusterId())).thenReturn(Optional.of(group));

    /* User in the group */
    when(groupRepository.userInGroup(group.getId(), mockUser.getId())).thenReturn(true);
    when(projectUtil.isProjectAdmin(project.getId(), mockUser)).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    CheckResult<Void> result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* User not in group but project admin */
    when(groupRepository.userInGroup(group.getId(), mockUser.getId())).thenReturn(false);
    when(projectUtil.isProjectAdmin(project.getId(), mockUser)).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* User not in group but general admin */
    when(groupRepository.userInGroup(group.getId(), mockUser.getId())).thenReturn(false);
    when(projectUtil.isProjectAdmin(project.getId(), mockUser)).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
    mockUser.setRole(UserRole.admin);
    result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* User not in group and not admin */
    mockUser.setRole(UserRole.student);
    result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Group not part of the project */
    when(groupRepository.findByIdAndClusterId(group.getId(), project.getGroupClusterId())).thenReturn(Optional.empty());
    result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(), mockUser);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

    /* Project not found */
    when(projectUtil.getProjectIfExists(project.getId())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    result = groupUtil.canGetProjectGroupData(group.getId(), project.getId(), mockUser);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* Check if groupId is null (eg: adminsubmission) */
    /* User is admin of project */
    when(projectUtil.getProjectIfExists(project.getId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", project));
    when(projectUtil.isProjectAdmin(project.getId(), mockUser)).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    result = groupUtil.canGetProjectGroupData(null, project.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* User is not admin of project */
    when(projectUtil.isProjectAdmin(project.getId(), mockUser)).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
    result = groupUtil.canGetProjectGroupData(null, project.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
  }


 }
