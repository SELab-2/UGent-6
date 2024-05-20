package com.ugent.pidgeon.controllers;


import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.*;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.*;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class ClusterController {

    @Autowired
    GroupClusterRepository groupClusterRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    GroupMemberRepository groupMemberRepository;
    @Autowired
    CourseUserRepository courseUserRepository;


    @Autowired
    private ClusterUtil clusterUtil;
    @Autowired
    private EntityToJsonConverter entityToJsonConverter;
    @Autowired
    private CourseUtil courseUtil;
    @Autowired
    private CommonDatabaseActions commonDatabaseActions;

    /**
     * Returns all clusters for a course
     *
     * @param courseid identifier of a course
     * @param auth authentication object of the requesting user
     * @return ResponseEntity<?>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883051">...</a>
     * @HttpMethod GET
     * @ApiPath /api/courses/{courseid}/clusters
     * @AllowedRoles student, teacher
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseid}/clusters")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getClustersForCourse(@PathVariable("courseid") Long courseid, Auth auth) {
        CheckResult<Pair<CourseEntity, CourseRelation>> checkResult = courseUtil.getCourseIfUserInCourse(courseid, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        CourseRelation courseRelation = checkResult.getData().getSecond();
        boolean hideStudentNumber = courseRelation.equals(CourseRelation.enrolled);

        // Get the clusters for the course
        List<GroupClusterEntity> clusters = groupClusterRepository.findClustersWithoutInvidualByCourseId(courseid);
        List<GroupClusterJson> clusterJsons = clusters.stream().map(
            g -> entityToJsonConverter.clusterEntityToClusterJson(g, hideStudentNumber)
        ).toList();
        // Return the clusters
        return ResponseEntity.ok(clusterJsons);
    }

    /**
     * Creates a new cluster for a course
     *
     * @param courseid identifier of a course
     * @param auth authentication object of the requesting user
     * @param clusterJson ClusterJson object containing the cluster data
     * @return ResponseEntity<?>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883393">apiDog documentation</a>
     * @HttpMethod POST
     * @ApiPath /api/courses/{courseid}/clusters
     * @AllowedRoles student, teacher
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseid}/clusters")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> createClusterForCourse(@PathVariable("courseid") Long courseid, Auth auth, @RequestBody GroupClusterCreateJson clusterJson) {

        CheckResult<CourseEntity> checkResult = courseUtil.getCourseIfAdmin(courseid, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        CheckResult<Void> jsonCheckResult = clusterUtil.checkGroupClusterCreateJson(clusterJson);
        if (jsonCheckResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(jsonCheckResult.getStatus()).body(jsonCheckResult.getMessage());
        }

        // Create the cluster
        GroupClusterEntity cluster = new GroupClusterEntity(
                courseid,
                clusterJson.capacity(),
                clusterJson.name(),
                clusterJson.groupCount()
        );
        cluster.setCreatedAt(OffsetDateTime.now());
        cluster.setLockGroupsAfter(clusterJson.lockGroupsAfter());
        GroupClusterEntity clusterEntity = groupClusterRepository.save(cluster);

        for (int i = 0; i < clusterJson.groupCount(); i++) {
            groupRepository.save(new GroupEntity("Group " + (i + 1), cluster.getId()));
        }

        GroupClusterJson clusterJsonResponse = entityToJsonConverter.clusterEntityToClusterJson(clusterEntity, false);

        // Return the cluster
        return ResponseEntity.status(HttpStatus.CREATED).body(clusterJsonResponse);
    }

    /**
     * Returns all groups for a cluster
     *
     * @param clusterid identifier of a cluster
     * @param auth authentication object of the requesting user
     * @return ResponseEntity<?>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883478">apiDog documentation</a>
     * @HttpMethod GET
     * @ApiPath /api/clusters/{clusterid}
     * @AllowedRoles student, teacher
     */
    @GetMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}") // Returns a cluster
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getCluster(@PathVariable("clusterid") Long clusterid, Auth auth) {
        CheckResult<GroupClusterEntity> checkResult = clusterUtil.getGroupClusterEntityIfNotIndividual(clusterid, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        GroupClusterEntity cluster = checkResult.getData();

        CheckResult<CourseEntity> courseAdmin = courseUtil.getCourseIfAdmin(cluster.getCourseId(), auth.getUserEntity());
        boolean hideStudentNumber = !courseAdmin.getStatus().equals(HttpStatus.OK);
        // Return the cluster
        return ResponseEntity.ok(entityToJsonConverter.clusterEntityToClusterJson(cluster, hideStudentNumber));
    }


    /**
     * Updates a cluster
     *
     * @param clusterid  identifier of a cluster
     * @param auth authentication object of the requesting user
     * @param clusterJson ClusterJson object containing the cluster data
     * @return ResponseEntity<?>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883519">apiDog documentation</a>
     * @HttpMethod PUT
     * @ApiPath /api/clusters/{clusterid}
     * @AllowedRoles student, teacher
     */
    @PutMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateCluster(@PathVariable("clusterid") Long clusterid, Auth auth, @RequestBody GroupClusterUpdateJson clusterJson) {
        CheckResult<GroupClusterEntity> checkResult = clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(clusterid, auth.getUserEntity());

        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        return doGroupClusterUpdate(checkResult.getData(), clusterJson);
    }

    public ResponseEntity<?> doGroupClusterUpdate(GroupClusterEntity clusterEntity, GroupClusterUpdateJson clusterJson) {
        CheckResult<Void> checkResult = clusterUtil.checkGroupClusterUpdateJson(clusterJson);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        clusterEntity.setMaxSize(clusterJson.getCapacity());
        clusterEntity.setName(clusterJson.getName());
        clusterEntity.setLockGroupsAfter(clusterJson.getLockGroupsAfter());
        clusterEntity = groupClusterRepository.save(clusterEntity);
    return ResponseEntity.ok(entityToJsonConverter.clusterEntityToClusterJson(clusterEntity, false));
    }

    /**
     * Fills up the groups in a cluster by providing a map of groupids with lists of userids
     *
     * @param clusterid  identifier of a cluster
     * @param auth authentication object of the requesting user
     * @param clusterFillMap Map object containing a map of all groups and their
     *                        members of that cluster
     * @return ResponseEntity<?>
     * @HttpMethod PUT
     * @ApiPath /api/clusters/{clusterid}/fill
     * @AllowedRoles student, teacher
     */
    @PutMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}/fill")
    @Transactional
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> fillCluster(@PathVariable("clusterid") Long clusterid, Auth auth, @RequestBody Map<String, Long[]> clusterFillMap) {
        ClusterFillJson clusterFillJson = new ClusterFillJson(clusterFillMap);
        try{
            CheckResult<GroupClusterEntity> checkResult = clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(clusterid, auth.getUserEntity());

            if (checkResult.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }

            GroupClusterEntity groupCluster = checkResult.getData();

            List<GroupEntity> groups = groupRepository.findAllByClusterId(clusterid);

            CheckResult<Void> jsonCheckRes = clusterUtil.checkFillClusterJson(clusterFillJson, groupCluster);
            if (jsonCheckRes.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(jsonCheckRes.getStatus()).body(jsonCheckRes.getMessage());
            }

            for(GroupEntity group: groups){
                commonDatabaseActions.removeGroup(group.getId());
            }

            for(String groupName: clusterFillJson.getClusterGroupMembers().keySet()){
                Long[] users = clusterFillJson.getClusterGroupMembers().get(groupName);
                GroupEntity groupEntity = new GroupEntity(groupName, clusterid);
                groupEntity = groupRepository.save(groupEntity);
                for(Long userid: users){
                    groupMemberRepository.addMemberToGroup(groupEntity.getId(), userid);
                }
            }

            groupCluster.setGroupAmount(clusterFillJson.getClusterGroupMembers().size());
            groupClusterRepository.save(groupCluster);
            return ResponseEntity.status(HttpStatus.OK).body(entityToJsonConverter.clusterEntityToClusterJson(groupCluster, false));
        } catch (Exception e) {
            Logger.getGlobal().severe(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }


    @PatchMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> patchCluster(@PathVariable("clusterid") Long clusterid, Auth auth, @RequestBody GroupClusterUpdateJson clusterJson) {
        CheckResult<GroupClusterEntity> checkResult = clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(clusterid, auth.getUserEntity());

        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        GroupClusterEntity cluster = checkResult.getData();

        if (clusterJson.getCapacity() == null) {
            clusterJson.setCapacity(cluster.getMaxSize());
        }

        if (clusterJson.getName() == null) {
            clusterJson.setName(cluster.getName());
        }

        if (clusterJson.getLockGroupsAfter() == null) {
            clusterJson.setLockGroupsAfter(cluster.getLockGroupsAfter());
        }

        return doGroupClusterUpdate(cluster, clusterJson);
    }

    /**
     * Deletes a cluster
     *
     * @param clusterid identifier of a cluster
     * @param auth authentication object of the requesting user
     * @return ResponseEntity<?>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883520">apiDog documentation</a>
     * @HttpMethod DELETE
     * @ApiPath /api/clusters/{clusterid}
     * @AllowedRoles student, teacher
     */
    @DeleteMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}")
    @Roles({UserRole.teacher, UserRole.student})
    @Transactional
    public ResponseEntity<?> deleteCluster(@PathVariable("clusterid") Long clusterid, Auth auth) {
        CheckResult<Void> checkResult = clusterUtil.canDeleteCluster(clusterid, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        CheckResult<Void> deleteResult = commonDatabaseActions.deleteClusterById(clusterid);
        if (deleteResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(deleteResult.getStatus()).body(deleteResult.getMessage());
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Creates a new group for a cluster
     *
     * @param clusterid  identifier of a cluster
     * @param auth     authentication object of the requesting user
     * @param groupJson  GroupCreateJson object containing the group data
     * @return ResponseEntity<?>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723980">apiDog documentation</a>
     * @HttpMethod POST
     * @ApiPath /api/clusters/{clusterid}/groups
     * @AllowedRoles student, teacher
     */
    @PostMapping(ApiRoutes.CLUSTER_BASE_PATH + "/{clusterid}/groups")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> createGroupForCluster(@PathVariable("clusterid") Long clusterid, Auth auth, @RequestBody GroupCreateJson groupJson) {
        if (groupJson.name() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("name must be provided");
        }

        if (groupJson.name().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name cannot be empty");
        }

        CheckResult<GroupClusterEntity> clusterCheck = clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(clusterid, auth.getUserEntity());
        if (clusterCheck.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(clusterCheck.getStatus()).body(clusterCheck.getMessage());
        }
        GroupClusterEntity cluster = clusterCheck.getData();
        GroupEntity group = new GroupEntity(groupJson.name(), clusterid);
        group = groupRepository.save(group);

        cluster.setGroupAmount(cluster.getGroupAmount() + 1);
        groupClusterRepository.save(cluster);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityToJsonConverter.groupEntityToJson(group, false));
    }
}
