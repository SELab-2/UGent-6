package com.ugent.pidgeon.controllers;


import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupClusterJson;
import com.ugent.pidgeon.model.json.GroupReferenceJson;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
public class ClusterController {
    @Autowired
    GroupClusterRepository groupClusterRepository;
    @Autowired
    GroupRepository groupRepository;

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseid}/clusters") // Returns all clusters for a course
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getClustersForCourse(@PathVariable("courseid") Long courseid, Auth auth) {
        // Get the user id
        long userId = auth.getUserEntity().getId();

        // Get the clusters for the course
        List<GroupClusterEntity> clusters = groupClusterRepository.findClustersByCourseId(courseid);
        List<GroupClusterJson> clusterJsons = clusters.stream().map(
                cluster -> {
                    List<GroupReferenceJson> groups = groupRepository.findAllByClusterId(cluster.getId()).stream().map(
                            group -> new GroupReferenceJson(
                                    group.getName(),
                                    ApiRoutes.GROUP_BASE_PATH + "/" + group.getId()
                            )
                    ).toList();
                    Logger.getLogger("ClusterController").info("Groups: " + groups);
                    return new GroupClusterJson(
                        cluster.getId(),
                        cluster.getName(),
                        cluster.getMaxSize(),
                        cluster.getGroupAmount(),
                        cluster.getCreatedAt(),
                        groups);
                }).toList();
        // Return the clusters
        return ResponseEntity.ok(clusterJsons);
    }
}
