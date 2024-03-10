package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GroupController {
    @Autowired
    private GroupRepository groupRepository;

    @GetMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<GroupJson> getGroupById(@PathVariable("groupid") Long groupid, Auth auth) {
        // Get userId
        long userId = auth.getUserEntity().getId();

        // Get the group, return 404 if it does not exist
        GroupJson group = groupRepository.findById(groupid).map(
                groupEntity -> new GroupJson(groupEntity.getName(), ApiRoutes.CLUSTER_BASE_PATH + "/" + groupEntity.getClusterId())
        ).orElse(null);
        if (group == null) {
            return ResponseEntity.notFound().build();
        }

        // Return 403 if the user does not have access to the group
        if(!groupRepository.userAccesToGroup(userId, groupid)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get the members of the group
        List<UserReferenceJson> members = groupRepository.findGroupUsersReferencesByGroupId(groupid).stream().map(user ->
            new UserReferenceJson(user.getName(), ApiRoutes.USER_BASE_PATH + "/" + user.getUserId())
        ).toList();

        // Return the group with its members
        group.setMembers(members);
        return ResponseEntity.ok(group);
    }

}
