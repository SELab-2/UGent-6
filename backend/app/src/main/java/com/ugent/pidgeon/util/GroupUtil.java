package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


@Component
public class GroupUtil {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupClusterRepository groupClusterRepository;
    @Autowired
    private ClusterUtil clusterUtil;
    @Autowired
    private ProjectUtil projectUtil;
  @Autowired
  private UserUtil userUtil;


    /**
     * Check if a group exists
     * @param groupId id of the group
     * @return CheckResult with the status of the check and the group
     */
    public CheckResult<GroupEntity> getGroupIfExists(long groupId) {
        GroupEntity group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group not found", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", group);
    }

    /**
     * Check if a user can get a group, this is equivalent to user being in the same course of the group
     * @param groupId id of the group
     * @param user user that wants to get the group
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> canGetGroup(long groupId, UserEntity user) {
        if (!groupRepository.userAccessToGroup(user.getId(), groupId) && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User does not have access to this group", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }

    /**
     * Check if a user is an admin of a group
     * @param groupId id of the group
     * @param user user that wants to get the group
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> isAdminOfGroup(long groupId, UserEntity user) {
        if (!groupRepository.isAdminOfGroup(user.getId(), groupId) && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not an admin of this group", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }

    /**
     * Check if a user can update a group
     * @param groupId id of the group
     * @param user user that wants to update the group
     * @return CheckResult with the status of the check and the group
     */
    public CheckResult<GroupEntity> canUpdateGroup(long groupId, UserEntity user) {
        CheckResult<GroupEntity> groupCheck = getGroupIfExists(groupId);
        if (groupCheck.getStatus() != HttpStatus.OK) {
            return groupCheck;
        }
        GroupEntity group = groupCheck.getData();
        CheckResult<Void> adminCheck = isAdminOfGroup(groupId, user);
        if (adminCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(adminCheck.getStatus(), adminCheck.getMessage(), null);
        }
        if (clusterUtil.isIndividualCluster(group.getClusterId())) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Cannot update individual group", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", groupCheck.getData());
    }


    /**
     * Check if a user can add a user to a group
     * @param groupId  id of the group
     * @param userId id of the user to add
     * @param user user that wants to add the user
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> canAddUserToGroup(long groupId, long userId, UserEntity user) {
        GroupEntity group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group not found", null);
        }

        boolean isAdmin = false;

        if (user.getId() != userId) {
            CheckResult<Void> admin = isAdminOfGroup(groupId, user);
            if (admin.getStatus() != HttpStatus.OK) {
                return admin;
            }
            isAdmin = true;
        } else {
            if (!groupRepository.userAccessToGroup(userId, groupId)) {
                return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not part of the course", null);
            }
            if (groupClusterRepository.inArchivedCourse(group.getClusterId())) {
                return new CheckResult<>(HttpStatus.FORBIDDEN, "Cannot join a group in an archived course", null);
            }
        }

        UserEntity userToAdd = userUtil.getUserIfExists(userId);
        if (userToAdd == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "User not found", null);
        }

        if (groupClusterRepository.userInGroupForCluster(group.getClusterId(), userId)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is already in a group for this cluster", null);
        }
        if (groupRepository.userInGroup(groupId, userId)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is already in this group", null);
        }
        CheckResult<GroupClusterEntity> cluster = clusterUtil.getClusterIfExists(group.getClusterId());
        if (cluster.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error while checking cluster", null);
        }

        if (cluster.getData().getMaxSize() <= groupRepository.countUsersInGroup(groupId) && !isAdmin) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Group is full", null);
        }
        if (clusterUtil.isIndividualCluster(group.getClusterId())) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Cannot add user to individual group", null);
        }

        OffsetDateTime lockGroupTime = cluster.getData().getLockGroupsAfter();
        if (lockGroupTime != null && lockGroupTime.isBefore(OffsetDateTime.now()) && !isAdmin) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Groups are locked", null);
        }

        if (isAdminOfGroup(groupId, userToAdd).getStatus().equals(HttpStatus.OK)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Cannot add a course admin to a group", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", null);
    }

    /**
     * Check if a user can remove a user from a group
     * @param groupId id of the group
     * @param userId id of the user to remove
     * @param user user that wants to remove the user
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> canRemoveUserFromGroup(long groupId, long userId, UserEntity user) {
        GroupEntity group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group not found", null);
        }
        if (user.getId() != userId) {
            CheckResult<Void> admin = isAdminOfGroup(groupId, user);
            if (admin.getStatus() != HttpStatus.OK) {
                return admin;
            }

        } else {
            if (groupClusterRepository.inArchivedCourse(group.getClusterId())) {
                return new CheckResult<>(HttpStatus.FORBIDDEN, "Cannot leave a group in an archived course", null);
            }
            CheckResult<GroupClusterEntity> cluster = clusterUtil.getClusterIfExists(group.getClusterId());
            if (cluster.getStatus() != HttpStatus.OK) {
                return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error while checking cluster", null);
            }
            OffsetDateTime lockGroupTime = cluster.getData().getLockGroupsAfter();
            if (lockGroupTime != null && lockGroupTime.isBefore(OffsetDateTime.now())) {
                return new CheckResult<>(HttpStatus.FORBIDDEN, "Groups are locked", null);
            }
        }
        if (!groupRepository.userInGroup(groupId, userId)) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "User is not in the group", null);
        }

        if (clusterUtil.isIndividualCluster(group.getClusterId())) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Cannot remove user from individual group", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", null);
    }

    /**
     * Check if a user can get the submissions or data of a group
     * @param groupId id of the group
     * @param projectId id of the project
     * @param user user that wants to get the submissions
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> canGetProjectGroupData(Long groupId, long projectId, UserEntity user) {
        CheckResult<ProjectEntity> projectCheck = projectUtil.getProjectIfExists(projectId);
        if (projectCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(projectCheck.getStatus(), projectCheck.getMessage(), null);
        }
        ProjectEntity project = projectCheck.getData();
        if (groupId != null && groupRepository.findByIdAndClusterId(groupId, project.getGroupClusterId()).isEmpty()) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group not part of the project", null);
        }
        boolean inGroup = groupId != null && groupRepository.userInGroup(groupId, user.getId());
        boolean isAdmin = user.getRole().equals(UserRole.admin) || projectUtil.isProjectAdmin(projectId, user).getStatus().equals(HttpStatus.OK);
        if (inGroup || isAdmin) {
            return new CheckResult<>(HttpStatus.OK, "", null);
        } else {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User does not have access to the submissions of the group", null);
        }
    }

}
