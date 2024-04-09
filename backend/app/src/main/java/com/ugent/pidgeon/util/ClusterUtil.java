package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.json.GroupClusterCreateJson;
import com.ugent.pidgeon.model.json.GroupClusterJson;
import com.ugent.pidgeon.model.json.GroupClusterUpdateJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClusterUtil {
    @Autowired
    private GroupClusterRepository groupClusterRepository;
    @Autowired
    private CourseUtil courseUtil;


    public boolean isIndividualCluster(GroupClusterEntity cluster) {
        return cluster != null && cluster.getGroupAmount() <= 1;
    }

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

    public CheckResult<GroupClusterEntity> getGroupClusterEntityIfNotIndividual(long clusterId, UserEntity user) {
        GroupClusterEntity groupCluster = groupClusterRepository.findById(clusterId).orElse(null);
        if (groupCluster == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group cluster does not exist", null);
        }
        if (isIndividualCluster(groupCluster)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Individual clusters cannot be accesed", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", groupCluster);
    }
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

    public boolean isIndividualCluster(long clusterId) {
        GroupClusterEntity cluster = groupClusterRepository.findById(clusterId).orElse(null);
        return isIndividualCluster(cluster);
    }

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

    public CheckResult<GroupClusterEntity> getClusterIfExists(long clusterId) {
        GroupClusterEntity groupCluster = groupClusterRepository.findById(clusterId).orElse(null);
        if (groupCluster == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Group cluster does not exist", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", groupCluster);
    }

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
