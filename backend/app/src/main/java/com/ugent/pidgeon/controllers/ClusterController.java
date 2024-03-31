package com.ugent.pidgeon.controllers;


import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.*;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    GroupUserRepository groupUserRepository;
    @Autowired
    GroupController groupController;

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseid}/clusters") // Returns all clusters for a course
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getClustersForCourse(@PathVariable("courseid") Long courseid, Auth auth) {
        // Get the user id
        long userId = auth.getUserEntity().getId();
        if (!courseRepository.existsById(courseid)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
        if (courseUserRepository.findByCourseIdAndUserId(courseid, userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not part of course");
        }

        // Get the clusters for the course
        List<GroupClusterEntity> clusters = groupClusterRepository.findClustersByCourseId(courseid);
        List<GroupClusterReferenceJson> clusterJsons = clusters.stream().map(
                this::clusterEntityToClusterReferenceJson).toList();
        // Return the clusters
        return ResponseEntity.ok(clusterJsons);
    }

    private GroupClusterReferenceJson clusterEntityToClusterReferenceJson(GroupClusterEntity cluster) {
        return new GroupClusterReferenceJson(
                cluster.getId(),
                cluster.getName(),
                cluster.getGroupAmount(),
                ApiRoutes.CLUSTER_BASE_PATH + "/" + cluster.getId()
        );
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
                groups,
                ApiRoutes.COURSE_BASE_PATH + "/" + cluster.getCourseId()
                );
    }

    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseid}/clusters") // Creates a new cluster for a course
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> createClusterForCourse(@PathVariable("courseid") Long courseid, Auth auth, @RequestBody GroupClusterCreateJson clusterJson) {
        // Get the user id
        long userId = auth.getUserEntity().getId();
        if (!courseRepository.existsById(courseid)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
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
            groupRepository.save(new GroupEntity( "Group " + (i+1), clusterEntity.getId()));
        }

        GroupClusterJson clusterJsonResponse = clusterEntityToClusterJson(clusterEntity);

        // Return the cluster
        return ResponseEntity.status(HttpStatus.CREATED).body(clusterJsonResponse);
    }

    @GetMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}") // Returns a cluster
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getCluster(@PathVariable("clusterid") Long clusterid, Auth auth) {
        // Get the user id
        long userId = auth.getUserEntity().getId();
        GroupClusterEntity cluster = groupClusterRepository.findById(clusterid).orElse(null);
        if (cluster == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cluster not found");
        }
        if (courseUserRepository.findByCourseIdAndUserId(cluster.getCourseId(), userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not part of course");
        }

        // Return the cluster
        return ResponseEntity.ok(clusterEntityToClusterJson(cluster));
    }

    @PutMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}") // Updates a cluster
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateCluster(@PathVariable("clusterid") Long clusterid, Auth auth, @RequestBody GroupClusterUpdateJson clusterJson) {
        // Get the user id
        long userId = auth.getUserEntity().getId();
        GroupClusterEntity cluster = groupClusterRepository.findById(clusterid).orElse(null);
        if (cluster == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cluster not found");
        }
        if (!courseRepository.adminOfCourse(cluster.getCourseId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not admin of course");
        }
        cluster.setName(clusterJson.name());
        cluster.setMaxSize(clusterJson.capacity());
        cluster = groupClusterRepository.save(cluster);
        return ResponseEntity.ok(clusterEntityToClusterJson(cluster));
    }

    @DeleteMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}") // Deletes a cluster
    @Roles({UserRole.teacher, UserRole.student})
    @Transactional
    public ResponseEntity<?> deleteCluster(@PathVariable("clusterid") Long clusterid, Auth auth) {
        // Get the user id
        long userId = auth.getUserEntity().getId();
        GroupClusterEntity cluster = groupClusterRepository.findById(clusterid).orElse(null);
        if (cluster == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cluster not found");
        }
        if (!courseRepository.adminOfCourse(cluster.getCourseId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not admin of course");
        }
        if (groupClusterRepository.usedInProject(clusterid)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cluster is being used in a project");
        }
        for (GroupEntity group : groupRepository.findAllByClusterId(clusterid)) {
            // Delete all groupUsers
            groupUserRepository.deleteAllByGroupId(group.getId());
            groupRepository.deleteById(group.getId());
        }
        groupClusterRepository.deleteById(clusterid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}/groups") // Creates a new group for a cluster
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> createGroupForCluster(@PathVariable("clusterid") Long clusterid, Auth auth, @RequestBody GroupCreateJson groupJson) {
        // Get the user id
        long userId = auth.getUserEntity().getId();
        GroupClusterEntity cluster = groupClusterRepository.findById(clusterid).orElse(null);
        if (cluster == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cluster not found");
        }
        if (!courseRepository.adminOfCourse(cluster.getCourseId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not admin of course");
        }
        GroupEntity group = new GroupEntity(groupJson.name(), clusterid);
        group = groupRepository.save(group);

        cluster.setGroupAmount(cluster.getGroupAmount() + 1);
        groupClusterRepository.save(cluster);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupController.groupEntityToJson(group));
    }
}
