package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.json.GroupClusterCreateJson;
import com.ugent.pidgeon.model.json.GroupClusterUpdateJson;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ClusterUtil {
    @Autowired
    private GroupClusterRepository groupClusterRepository;
    @Autowired
    private CourseUtil courseUtil;

    /**
     * Check if a cluster is an individual cluster. This means that it only contains one group
     * @param cluster cluster to check
     * @return true if the cluster is an individual cluster
     */
    public boolean isIndividualCluster(GroupClusterEntity cluster) {
        return cluster != null && cluster.getGroupAmount() <= 1;
    }

    /**
     * Check if a user can delete a cluster
     * @param clusterId id of the cluster
     * @param user user that wants to delete the cluster
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> canDeleteCluster(long clusterId, UserEntity user) {
        CheckResult<GroupClusterEntity> clusterCheck = getGroupClusterEntityIfAdminAndNotIndividual(clusterId, user);
        if (!clusterCheck.getStatus().equals(HttpStatus.OK)) {
            return new CheckResult<>(clusterCheck.getStatus(), clusterCheck.getMessage(), null);
        }
        if (groupClusterRepository.usedInProject(clusterId)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Cluster is used in a project", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }


    /**
     * Get group cluster entity if it is not an individual cluster and user has access to the course
     * @param clusterId id of the cluster
     * @param user user that wants to get the cluster
     * @return CheckResult with the status of the check and the group cluster entity
     */
    public CheckResult<GroupClusterEntity> getGroupClusterEntityIfNotIndividual(long clusterId, UserEntity user) {
        GroupClusterEntity groupCluster = groupClusterRepository.findById(clusterId).orElse(null);
        if (groupCluster == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group cluster does not exist", null);
        }
        CheckResult<Pair<CourseEntity, CourseRelation>> courseCheck = courseUtil.getCourseIfUserInCourse(groupCluster.getCourseId(), user);
        if (!courseCheck.getStatus().equals(HttpStatus.OK)) {
            return new CheckResult<>(courseCheck.getStatus(), courseCheck.getMessage(), null);
        }

        if (isIndividualCluster(groupCluster)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Individual clusters cannot be accesed", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", groupCluster);
    }


    /**
     * Get group cluster entity if user is admin of the course and the cluster is not an individual cluster
     * @param clusterId id of the cluster
     * @param user user that wants to get the cluster
     * @return CheckResult with the status of the check and the group cluster entity
     */
    public CheckResult<GroupClusterEntity> getGroupClusterEntityIfAdminAndNotIndividual(long clusterId, UserEntity user) {
        CheckResult<GroupClusterEntity> groupClusterCheck = getGroupClusterEntityIfNotIndividual(clusterId, user);
        if (!groupClusterCheck.getStatus().equals(HttpStatus.OK)) {
            return groupClusterCheck;
        }
        GroupClusterEntity groupCluster = groupClusterCheck.getData();
        CheckResult<?> adminCheck = courseUtil.getCourseIfAdmin(groupCluster.getCourseId(), user);
        if (!adminCheck.getStatus().equals(HttpStatus.OK)) {
            return new CheckResult<>(adminCheck.getStatus(), adminCheck.getMessage(), null);
        }

        return new CheckResult<>(HttpStatus.OK, "", groupCluster);
    }


    /**
     * Check if a cluster is an individual cluster. This means that it only contains one group
     * @param clusterId id of the cluster
     * @return true if the cluster is an individual cluster
     */
    public boolean isIndividualCluster(long clusterId) {
        GroupClusterEntity cluster = groupClusterRepository.findById(clusterId).orElse(null);
        return isIndividualCluster(cluster);
    }

    /**
     * Check if a cluster is part of a course
     * @param clusterId id of the cluster
     * @param courseId id of the course
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> partOfCourse(long clusterId, long courseId) {
        // Check of de GroupCluster deel is van het vak
        GroupClusterEntity groupCluster = groupClusterRepository.findById(clusterId).orElse(null);
        if (groupCluster == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group cluster does not exist", null);
        }
        if (groupCluster.getCourseId() != courseId) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Group cluster isn't linked to this course", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }

    /**
     * Get a group cluster entity if it exists
     * @param clusterId id of the cluster
     * @return CheckResult with the status of the check and the group cluster entity
     */
    public CheckResult<GroupClusterEntity> getClusterIfExists(long clusterId) {
        GroupClusterEntity groupCluster = groupClusterRepository.findById(clusterId).orElse(null);
        if (groupCluster == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group cluster does not exist", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", groupCluster);
    }

    /**
     * Check if a group cluster update json is valid
     * @param clusterJson json to check
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> checkGroupClusterUpdateJson(GroupClusterUpdateJson clusterJson) {
        if (clusterJson.getCapacity() == null || clusterJson.getName() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "capacity and name must be provided", null);
        }
        if (clusterJson.getName().isBlank()) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Name cannot be empty", null);
        }
        if (clusterJson.getCapacity() <= 1) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Capacity must be greater than 1", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }

    /**
     * Check if a group cluster create json is valid
     * @param clusterJson json to check
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> checkGroupClusterCreateJson(GroupClusterCreateJson clusterJson) {
        if (clusterJson.capacity() == null || clusterJson.name() == null || clusterJson.groupCount() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "capacity, name and groupCount must be provided", null);
        }

        if (clusterJson.name().isBlank()) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Name cannot be empty", null);
        }

        if (clusterJson.capacity() <= 1) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Capacity must be greater than 1", null);
        }

        if (clusterJson.groupCount() < 0) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Group count must be 0 or greater", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", null);
    }
}
