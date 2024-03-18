package com.ugent.pidgeon.controllers;


import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupClusterJson;
import com.ugent.pidgeon.model.json.GroupReferenceJson;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class ClusterController {
    @Autowired
    GroupClusterRepository groupClusterRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    CourseUserRepository courseUserRepository;
    @Autowired
    CourseRepository courseRepository;

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseid}/clusters") // Returns all clusters for a course
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getClustersForCourse(@PathVariable("courseid") Long courseid, Auth auth) {
        // Get the user id
        long userId = auth.getUserEntity().getId();
        if (courseUserRepository.findByCourseIdAndUserId(courseid, userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not part of course");
        }

        // Get the clusters for the course
        List<GroupClusterEntity> clusters = groupClusterRepository.findClustersByCourseId(courseid);
        List<GroupClusterJson> clusterJsons = clusters.stream().map(
                this::clusterEntityToClusterJson).toList();
        // Return the clusters
        return ResponseEntity.ok(clusterJsons);
    }

    private GroupClusterJson clusterEntityToClusterJson(GroupClusterEntity cluster) {
        List<GroupReferenceJson> groups = groupRepository.findAllByClusterId(cluster.getId()).stream().map(
                group -> new GroupReferenceJson(
                        group.getName(),
                        ApiRoutes.GROUP_BASE_PATH + "/" + group.getId()
                )
        ).toList();
        return new GroupClusterJson(
                cluster.getId(),
                cluster.getName(),
                cluster.getMaxSize(),
                cluster.getGroupAmount(),
                cluster.getCreatedAt(),
                groups);
    }

    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseid}/clusters") // Creates a new cluster for a course
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> createClusterForCourse(@PathVariable("courseid") Long courseid, Auth auth, @RequestBody GroupClusterJson clusterJson) {
        // Get the user id
        long userId = auth.getUserEntity().getId();
        if (!courseRepository.adminOfCourse(courseid, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not admin of course");
        }

        // Create the cluster
        GroupClusterEntity cluster = new GroupClusterEntity(
                courseid,
                clusterJson.capacity(),
                clusterJson.name(),
                clusterJson.groupCount()
        );
        cluster.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        GroupClusterEntity clusterEntity =  groupClusterRepository.save(cluster);

        for (int i = 0; i < clusterJson.groupCount(); i++) {
            groupRepository.save(new GroupEntity( "Group " + (i+1), cluster.getId()));
        }

        GroupClusterJson clusterJsonResponse = clusterEntityToClusterJson(clusterEntity);

        // Return the cluster
        return ResponseEntity.status(HttpStatus.CREATED).body(clusterJsonResponse);
    }

}
