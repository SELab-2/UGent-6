package com.ugent.pidgeon.util;


import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.json.GroupClusterJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntityToJsonConverter {

    @Autowired
    private GroupClusterRepository groupClusterRepository;
    @Autowired
    private GroupRepository groupRepository;


    public GroupJson groupEntityToJson(GroupEntity groupEntity) {
        GroupJson group = new GroupJson(groupEntity.getId(), groupEntity.getName(), ApiRoutes.CLUSTER_BASE_PATH + "/" + groupEntity.getClusterId());
        GroupClusterEntity cluster = groupClusterRepository.findById(groupEntity.getClusterId()).orElse(null);
        if (cluster != null && cluster.getGroupAmount() > 1){
            group.setGroupClusterUrl(ApiRoutes.CLUSTER_BASE_PATH + "/" + cluster.getId());
        } else {
            group.setGroupClusterUrl(null);
        }
        // Get the members of the group
        List<UserReferenceJson> members = groupRepository.findGroupUsersReferencesByGroupId(groupEntity.getId()).stream().map(user ->
                new UserReferenceJson(user.getName(), user.getEmail(), user.getUserId())
        ).toList();

        // Return the group with its members
        group.setMembers(members);
        return group;
    }

    public GroupClusterJson clusterEntityToClusterJson(GroupClusterEntity cluster) {
        List<GroupJson> groups = groupRepository.findAllByClusterId(cluster.getId()).stream().map(
                this::groupEntityToJson
        ).toList();
        return new GroupClusterJson(
                cluster.getId(),
                cluster.getName(),
                cluster.getMaxSize(),
                cluster.getGroupAmount(),
                cluster.getCreatedAt(),
                groups,
                ApiRoutes.COURSE_BASE_PATH + "/" + cluster.getCourseId()
        );
    }
}
