package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GroupClusterController {

    @Autowired
    private GroupClusterRepository groupClusterRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseUserRepository courseUserRepository;

    // Functie om GroepClusters te verwijderen, voorlopig nog niet gelinkt aan een api url.
    public ResponseEntity<String> deleteGroupCluster(long clusterId, Auth auth, Boolean verwijderFeedback) {
        try {
            // Check of de user een admin of creator is van het vak
            UserEntity user = auth.getUserEntity();
            long userId = user.getId();

            // de group_cluster vinden
            GroupClusterEntity groupCluster = groupClusterRepository.findById(clusterId).orElseThrow();

            // het vak selecteren om toegangschecks uit te voeren.
            CourseEntity courseEntity = courseRepository.findById(groupCluster.getCourseId()).orElseThrow();

            // check of de user admin of lesgever is van het vak
            CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseEntity.getId(), userId)).orElseThrow();
            if(courseUserEntity.getRelation() == CourseRelation.enrolled){
                throw new IllegalAccessException("Alleen maker of administrator van vak mogen het vak verwijderen");
            }


            //voor elke groep
            for(GroupEntity group: groupRepository.findAllByClusterId(groupCluster.getId())){
                for(GroupRepository.UserReference userReference : groupRepository.findGroupUsersReferencesByGroupId(group.getId())){
                    // verwijdert elke GroupUserEntity
                    groupMemberRepository.removeMemberFromGroup(group.getId(), userReference.getUserId());
                }
                // Verwijder de groepfeedback als het moet, soms mag dit niet omdat die al eerder verwijderd is.
                if(verwijderFeedback){
                    groupFeedbackRepository.deleteAll(groupFeedbackRepository.findGroupFeedbackEntitiesByGroupId(group.getId()));
                }
                // Groep verwijderen
                groupRepository.delete(group);
            }
            //groepcluster verwijderen
            groupClusterRepository.delete(groupCluster);
            return ResponseEntity.ok("Groepcluster succesvol verwijderd");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Probleem bij verwijderen groepCluster: " + e.getMessage());
        }
    }
}
