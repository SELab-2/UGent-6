package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.json.MemberIdRequest;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.UserJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupMemberRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.UserRepository;
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
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    ProjectRepository projectController;

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
        if (user.getRole() == UserRole.student && memberid != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can't remove other students from the group");
        } else if (user.getRole() == UserRole.teacher && !groupRepository.userAccessToGroup(user.getId(), groupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to the group");
        } else if (!groupRepository.userInGroup(groupId, memberid)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not in the group");
        }


        if (groupMemberRepository.removeMemberFromGroup(groupId, memberid) == 0)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove member to group");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User removed from group");

    }


    /**
     * Function to remove a member from a group
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
        long memberid = user.getId();

        if (!groupRepository.userInGroup(groupId, memberid)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not in the group");
        }

        if (groupMemberRepository.removeMemberFromGroup(groupId, memberid) == 0)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove member to group");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User removed from group");

    }

    /**
     * Function to add a member to a group
     *
     * @param groupId ID of the group to add the member to
     * @param req     MemberIdRequest object containing the ID of the member to be added
     * @param auth    authentication object of the requesting user
     * @return ResponseEntity with a list of UserJson objects containing the members of the group
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883807">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}/members
     */
    @PostMapping(ApiRoutes.GROUP_MEMBER_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> addMemberToGroup(@PathVariable("groupid") long groupId, @RequestBody MemberIdRequest req, Auth auth) {
        UserEntity user = auth.getUserEntity();

        if (req.getMemberId() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MemberId is required");

        if (user.getRole() == UserRole.student && req.getMemberId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can't add other students to the group");
        }

        if (!userRepository.existsById(req.getMemberId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
      /*  } else if(user.getRole() == UserRole.teacher && !groupRepository.userAccessToGroup(user.getId(),groupId) && req.getMemberId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to the group"); */
        } else if (groupRepository.userInGroup(groupId, req.getMemberId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is already in the group");
        }

        try {
            groupMemberRepository.addMemberToGroup(groupId, req.getMemberId());
            List<UserEntity> members = groupMemberRepository.findAllMembersByGroupId(groupId);
            List<UserJson> response = members.stream().map(UserJson::new).toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Logger.getGlobal().severe(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }


    /**
     * Function to add a member to a group
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


        if (!userRepository.existsById(user.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
      /*  } else if(user.getRole() == UserRole.teacher && !groupRepository.userAccessToGroup(user.getId(),groupId) && req.getMemberId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to the group"); */
        } else if (groupRepository.userInGroup(groupId, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is already in the group");
        }

        try {
            groupMemberRepository.addMemberToGroup(groupId,user.getId());
            List<UserEntity> members = groupMemberRepository.findAllMembersByGroupId(groupId);
            List<UserJson> response = members.stream().map(UserJson::new).toList();
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
        if(!groupRepository.userInGroup(groupId, auth.getUserEntity().getId()) && auth.getUserEntity().getRole() != UserRole.teacher) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to the group");
        }
        List<UserEntity> members = groupMemberRepository.findAllMembersByGroupId(groupId);
        List<UserReferenceJson> response = members.stream().map((UserEntity e) -> new UserReferenceJson(e.getName(), ApiRoutes.USER_BASE_PATH + "/" + e.getId())).toList();

        return ResponseEntity.ok(response);
    }
}
