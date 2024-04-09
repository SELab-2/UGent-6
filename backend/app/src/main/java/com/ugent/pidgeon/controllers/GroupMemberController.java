package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.UserJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.GroupUtil;
import com.ugent.pidgeon.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
public class GroupMemberController {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupUtil groupUtil;
    @Autowired
    private UserUtil userUtil;
    @Autowired
    private EntityToJsonConverter entityToJsonConverter;


    /**
     * Function to remove a member from a group
     *
     * @param groupId  ID of the group to remove the member from
     * @param memberid ID of the member to be removed
     * @param auth     authentication object of the requesting user
     * @return ResponseEntity with a string message about the operation result
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883809">apiDog documentation</a>
     * @HttpMethod DELETE
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}/members/{memberid}
     */
    @DeleteMapping(ApiRoutes.GROUP_MEMBER_BASE_PATH + "/{memberid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<String> removeMemberFromGroup(@PathVariable("groupid") long groupId, @PathVariable("memberid") long memberid, Auth auth) {
        UserEntity user = auth.getUserEntity();
        CheckResult<Void> check = groupUtil.canRemoveUserFromGroup(groupId, memberid, user);
        if (check.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(check.getStatus()).body(check.getMessage());
        }

        if (groupMemberRepository.removeMemberFromGroup(groupId, memberid) == 0)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove member to group");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User removed from group");

    }

    /**
     * Function to remove the logged in user from a group
     *
     * @param groupId ID of the group to remove the member from
     * @param auth    authentication object of the requesting user
     * @return ResponseEntity with a string message about the operation result
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883809">apiDog documentation</a>
     * @HttpMethod DELETE
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}/members
     */
    @DeleteMapping(ApiRoutes.GROUP_MEMBER_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<String> removeMemberFromGroupInferred(@PathVariable("groupid") long groupId, Auth auth) {
        UserEntity user = auth.getUserEntity();
        CheckResult<Void> check = groupUtil.canRemoveUserFromGroup(groupId, user.getId(), user);
        if (check.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(check.getStatus()).body(check.getMessage());
        }

        if (groupMemberRepository.removeMemberFromGroup(groupId, user.getId()) == 0)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove member to group");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User removed from group");

    }

    /**
     * Function to add a member to a group
     *
     * @param groupId ID of the group to add the member to
     * @param memberid memberid of the member to be added
     * @param auth    authentication object of the requesting user
     * @return ResponseEntity with a list of UserJson objects containing the members of the group
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883807">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}/members/{memberid}
     */
    @PostMapping(ApiRoutes.GROUP_MEMBER_BASE_PATH + "/{memberid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> addMemberToGroup(@PathVariable("groupid") long groupId, @PathVariable("memberid") long memberid, Auth auth) {
        UserEntity user = auth.getUserEntity();

        CheckResult<Void> checkResult = groupUtil.canAddUserToGroup(groupId, memberid, user);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        try {
            groupMemberRepository.addMemberToGroup(groupId, memberid);
            List<UserEntity> members = groupMemberRepository.findAllMembersByGroupId(groupId);
            List<UserReferenceJson> response = members.stream().map(entityToJsonConverter::userEntityToUserReference).toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Logger.getGlobal().severe(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }


    /**
     * Function to add the logged in user to a group
     *
     * @param groupId ID of the group to add the member to
     * @param auth    authentication object of the requesting user
     * @return ResponseEntity with a list of UserJson objects containing the members of the group
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883807">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}/members
     */
    @PostMapping(ApiRoutes.GROUP_MEMBER_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> addMemberToGroupInferred(@PathVariable("groupid") long groupId, Auth auth) {
        UserEntity user = auth.getUserEntity();
        CheckResult<Void> checkResult = groupUtil.canAddUserToGroup(groupId, user.getId(), user);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        try {
            groupMemberRepository.addMemberToGroup(groupId,user.getId());
            List<UserEntity> members = groupMemberRepository.findAllMembersByGroupId(groupId);
            List<UserReferenceJson> response = members.stream().map(entityToJsonConverter::userEntityToUserReference).toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Logger.getGlobal().severe(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }


    /**
     * Function to get all members of a group
     *
     * @param groupId ID of the group to get the members from
     * @return ResponseEntity with a list of UserReferenceJson objects containing the members of the group
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883806">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}/members
     */
    @GetMapping(ApiRoutes.GROUP_MEMBER_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> findAllMembersByGroupId(@PathVariable("groupid") long groupId,Auth auth) {
        UserEntity user = auth.getUserEntity();
        CheckResult<Void> checkResult = groupUtil.canGetGroup(groupId, user);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        List<UserEntity> members = groupMemberRepository.findAllMembersByGroupId(groupId);
        List<UserReferenceJson> response = members.stream().map((UserEntity e) -> entityToJsonConverter.userEntityToUserReference(e)).toList();
        return ResponseEntity.ok(response);
    }
}
