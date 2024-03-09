package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.UserJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupMemberRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupMemberController {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;


    @DeleteMapping(ApiRoutes.GROUP_MEMBER_BASE_PATH+"/{memberid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<String> removeMemberFromGroup(@PathVariable("groupid") long groupId, @PathVariable("memberid") long memberid, Auth auth){
        UserEntity user = auth.getUserEntity();
        if(user.getRole() == UserRole.student && memberid != user.getId()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can't remove other students from the group");
        }

        if(!groupRepository.userInGroup(groupId, memberid)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not in the group");
        }

        if(groupMemberRepository.removeMemberFromGroup(groupId, memberid) == 0) return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("Something went wrong");
        return null;
    }


    @PostMapping(ApiRoutes.GROUP_MEMBER_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> addMemberToGroup(@PathVariable("groupid") long groupId, @PathVariable("memberid") long memberid, Auth auth){
        UserEntity user = auth.getUserEntity();
        if(user.getRole() == UserRole.student){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can't add other students to the group");
        }
        if(!groupRepository.userInGroup(groupId, memberid)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not in the group");
        }

        if(userRepository.findUserById(memberid) == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
        }

        groupMemberRepository.addMemberToGroup(groupId, memberid);
        List<UserEntity> members = groupMemberRepository.findAllMembersByGroupId(groupId);
        List<UserJson> response = members.stream().map(UserJson::new).toList();
        return ResponseEntity.ok(response);
    }


    @GetMapping(ApiRoutes.GROUP_MEMBER_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> findAllMembersByGroupId(@PathVariable("groupid") long groupId, Auth auth){
        UserEntity user = auth.getUserEntity();
        if(user.getRole() == UserRole.student){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can't see the members of a group");
        }

        List<UserEntity> members = groupMemberRepository.findAllMembersByGroupId(groupId);
        List<UserJson> response = members.stream().map(UserJson::new).toList();

        return ResponseEntity.ok(response);
    }
}
