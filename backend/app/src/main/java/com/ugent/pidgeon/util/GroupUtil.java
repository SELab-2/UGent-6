package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class GroupUtil {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupClusterRepository groupClusterRepository;
    @Autowired
    private ClusterUtil clusterUtil;

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

    public CheckResult<Void> isAdminOfGroup(long groupId, UserEntity user) {
        if (!groupRepository.isAdminOfGroup(groupId, user.getId()) && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not an admin of this group", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }


    public CheckResult<Void> canAddUserToGroup(long groupId, long userId, UserEntity user) {
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
            if (!groupRepository.userAccessToGroup(userId, groupId)) {
                return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not part of the course", null);
            }
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

        if (cluster.getData().getMaxSize() <= groupRepository.countUsersInGroup(groupId)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Group is full", null);
        }
        if (clusterUtil.isIndividualCluster(group.getClusterId())) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Cannot add user to individual group", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", null);
    }

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
        }
        if (!groupRepository.userInGroup(groupId, userId)) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "User is not in the group", null);
        }

        if (clusterUtil.isIndividualCluster(group.getClusterId())) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Cannot remove user from individual group", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", null);
    }


}
