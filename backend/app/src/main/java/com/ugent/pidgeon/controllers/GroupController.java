package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.NameRequest;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupController {
    @Autowired
    private GroupRepository groupRepository;


    private GroupJson groupEntityToJson(GroupEntity groupEntity) {
        GroupJson group = new GroupJson(groupEntity.getName(), ApiRoutes.CLUSTER_BASE_PATH + "/" + groupEntity.getClusterId());

        // Get the members of the group
        List<UserReferenceJson> members = groupRepository.findGroupUsersReferencesByGroupId(groupEntity.getId()).stream().map(user ->
                new UserReferenceJson(user.getName(), ApiRoutes.USER_BASE_PATH + "/" + user.getUserId())
        ).toList();

        // Return the group with its members
        group.setMembers(members);
        return group;
    }

    @GetMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<GroupJson> getGroupById(@PathVariable("groupid") Long groupid, Auth auth) {
        // Get userId
        long userId = auth.getUserEntity().getId();

        // Get the group, return 404 if it does not exist
        GroupEntity group = groupRepository.findById(groupid).orElse(null);
        if (group == null) {
            return ResponseEntity.notFound().build();
        }

        // Return 403 if the user does not have access to the group
        if(!groupRepository.userAccesToGroup(userId, groupid)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Return the group
        GroupJson groupJson = groupEntityToJson(group);
        return ResponseEntity.ok(groupJson);
    }


    @PutMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.teacher})
    public ResponseEntity<GroupJson> updateGroupName(@PathVariable("groupid") Long groupid, @RequestBody NameRequest nameRequest, Auth auth) {
        // Get userId
        long userId = auth.getUserEntity().getId();

        // Get the group, return 404 if it does not exist
        GroupEntity group = groupRepository.findById(groupid).orElse(null);
        if (group == null) {
            return ResponseEntity.notFound().build();
        }

        // Return 403 if the user does not have access to the group
        if(!groupRepository.userAccesToGroup(userId, groupid)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update the group name
        group.setName(nameRequest.getName());

        // Save the changes
        groupRepository.save(group);

        // Return the updated group
        GroupJson groupJson = groupEntityToJson(group);
        return ResponseEntity.ok(groupJson);
    }

    @DeleteMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.teacher})
    public ResponseEntity<Void> deleteGroup(@PathVariable("groupid") Long groupid, Auth auth) {
        // Get userId
        long userId = auth.getUserEntity().getId();

        // Get the group, return 404 if it does not exist
        GroupEntity group = groupRepository.findById(groupid).orElse(null);
        if (group == null) {
            return ResponseEntity.notFound().build();
        }

        // Return 403 if the user does not have access to the group
        if(!groupRepository.userAccesToGroup(userId, groupid)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Delete the group
        groupRepository.deleteGroupUsersByGroupId(groupid);
        groupRepository.deleteSubmissionsByGroupId(groupid);
        groupRepository.deleteGroupFeedbacksByGroupId(groupid);
        groupRepository.deleteById(groupid);

        // Return 204
        return ResponseEntity.noContent().build();
    }

}
