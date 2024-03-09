package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.UserJson;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import com.ugent.pidgeon.postgre.repository.GroupMemberRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupFeedbackController {

    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;

    @Autowired
    private GroupRepository groupRepository;


    @PatchMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher})
    public ResponseEntity<String> updateGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestParam("score") int score, Auth auth){
        UserEntity user = auth.getUserEntity();
        if(user.getRole() == UserRole.student){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can't update the score");
        }
        if(!groupRepository.userInGroup(groupId, user.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not in the group");
        }

        if(groupFeedbackRepository.updateGroupScore(score, groupId, projectId) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }
        return null;
    }

    @PostMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher})
    public ResponseEntity<String> addGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestParam("score") float score, Auth auth){
        UserEntity user = auth.getUserEntity();


        if(!groupRepository.userInGroup(groupId, user.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not in the group");
        }

        if(groupFeedbackRepository.addGroupScore(score, groupId, projectId) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }
        return ResponseEntity.ok("Score added successfully");
    }

    @GetMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> getGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, Auth auth){
        UserEntity user = auth.getUserEntity();
        if(user.getRole() == UserRole.student && !groupRepository.userInGroup(groupId, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not in this group");
        }

         GroupFeedbackEntity groupFeedbackEntity = groupFeedbackRepository.getGroupFeedback(groupId, projectId);
        if(groupFeedbackEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group feedback not found");
        }
        return ResponseEntity.ok(groupFeedbackEntity);
    }

}
