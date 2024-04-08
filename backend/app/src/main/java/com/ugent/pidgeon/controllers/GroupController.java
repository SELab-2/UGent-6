package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.NameRequest;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
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
    @Autowired
    private GroupClusterRepository groupClusterRepository;

    public boolean isIndividualGroup(long groupId) {
        GroupEntity group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return false;
        }
        GroupClusterEntity cluster = groupClusterRepository.findById(group.getClusterId()).orElse(null);
        return cluster != null && cluster.getGroupAmount() <= 1;
    }

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
                new UserReferenceJson(user.getName(), ApiRoutes.USER_BASE_PATH + "/" + user.getUserId())
        ).toList();

        // Return the group with its members
        group.setMembers(members);
        return group;
    }


    /**
     * Function to add a new project to an existing course
     *
     * @param groupid identifier of a group
     * @param auth    authentication object of the requesting user
     * @return ResponseEntity<GroupJson>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723981">apiDog documentation</a>
     * @HttpMethod Get
     * @AllowedRoles student, teacher
     * @ApiPath /api/groups/{groupid}
     */
    @GetMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getGroupById(@PathVariable("groupid") Long groupid, Auth auth) {


        // Get userId
        long userId = auth.getUserEntity().getId();

        // Get the group, return 404 if it does not exist
        GroupEntity group = groupRepository.findById(groupid).orElse(null);

        if (group == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        // Return 403 if the user does not have access to the group
        if (!groupRepository.userAccessToGroup(userId, groupid) && auth.getUserEntity().getRole()!=UserRole.admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this group");
        }

        // Return the group
        GroupJson groupJson = groupEntityToJson(group);
        return ResponseEntity.ok(groupJson);
    }

    /**
     * Function to update the name of a group
     *
     * @param groupid     identifier of a group
     * @param nameRequest object containing the new name of the group
     * @param auth        authentication object of the requesting user
     * @return ResponseEntity<GroupJson>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723995">apiDog documentation</a>
     * @HttpMethod Put
     * @AllowedRoles teacher
     * @ApiPath /api/groups/{groupid}
     */
    @PutMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> updateGroupName(@PathVariable("groupid") Long groupid, @RequestBody NameRequest nameRequest, Auth auth) {
        // Get userId
        long userId = auth.getUserEntity().getId();

        // Get the group, return 404 if it does not exist
        GroupEntity group = groupRepository.findById(groupid).orElse(null);
        if (group == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");

        }

        // Return 403 if the user does not have access to the group
        if (!groupRepository.userAccessToGroup(userId, groupid) && auth.getUserEntity().getRole()!=UserRole.admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this group");
        }

        if (isIndividualGroup(groupid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot change name of individual group");
        }

        // Update the group name
        group.setName(nameRequest.getName());

        // Save the changes
        groupRepository.save(group);

        // Return the updated group
        GroupJson groupJson = groupEntityToJson(group);
        return ResponseEntity.ok(groupJson);
    }

    /**
     * Function to delete a group
     *
     * @param groupid identifier of a group
     * @param auth    authentication object of the requesting user
     * @return ResponseEntity<Void>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723998">apiDog documentation</a>
     * @HttpMethod Delete
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}
     */
    @DeleteMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> deleteGroup(@PathVariable("groupid") Long groupid, Auth auth) {
        // Get userId
        long userId = auth.getUserEntity().getId();

        // Get the group, return 404 if it does not exist
        GroupEntity group = groupRepository.findById(groupid).orElse(null);
        if (group == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        // Return 403 if the user does not have access to the group
        if (!groupRepository.userAccessToGroup(userId, groupid) && auth.getUserEntity().getRole()!=UserRole.admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this group");
        }

        if (isIndividualGroup(groupid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot delete individual group");
        }

        removeGroup(groupid);
        // Return 204
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Group deleted");
    }

    public boolean removeGroup(long groupId) {
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
    }

}
