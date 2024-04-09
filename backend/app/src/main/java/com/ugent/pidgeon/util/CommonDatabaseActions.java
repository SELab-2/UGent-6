package com.ugent.pidgeon.util;


import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.GroupUserEntity;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.GroupUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommonDatabaseActions {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupClusterRepository groupClusterRepository;
    @Autowired
    private GroupUserRepository groupUserRepository;

    public boolean removeGroup(long groupId) {
        try {
            // Delete the group
            groupRepository.deleteGroupUsersByGroupId(groupId);
            groupRepository.deleteSubmissionsByGroupId(groupId);
            groupRepository.deleteGroupFeedbacksByGroupId(groupId);
            groupRepository.deleteById(groupId);

            // update groupcount in cluster
            groupClusterRepository.findById(groupId).ifPresent(cluster -> {
                cluster.setGroupAmount(cluster.getGroupAmount() - 1);
                groupClusterRepository.save(cluster);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean createNewIndividualClusterGroup(long courseId, long userId) {
        GroupClusterEntity groupClusterEntity = groupClusterRepository.findIndividualClusterByCourseId(courseId).orElse(null);
        if (groupClusterEntity == null) {
            return false;
        }
        // Create new group for the cluster
        GroupEntity groupEntity = new GroupEntity("", groupClusterEntity.getId());
        groupClusterEntity.setGroupAmount(groupClusterEntity.getGroupAmount() + 1);
        groupClusterRepository.save(groupClusterEntity);
        groupEntity = groupRepository.save(groupEntity);

        // Add user to the group
        GroupUserEntity groupUserEntity = new GroupUserEntity(groupEntity.getId(), userId);
        groupUserRepository.save(groupUserEntity);
        return true;
    }

    public boolean removeIndividualClusterGroup(long courseId, long userId) {
        GroupClusterEntity groupClusterEntity = groupClusterRepository.findIndividualClusterByCourseId(courseId).orElse(null);
        if (groupClusterEntity == null) {
            return false;
        }
        // Find the group of the user
        Optional<GroupEntity> groupEntityOptional = groupRepository.groupByClusterAndUser(groupClusterEntity.getId(), userId);
        return groupEntityOptional.filter(groupEntity -> removeGroup(groupEntity.getId())).isPresent();
    }
}
