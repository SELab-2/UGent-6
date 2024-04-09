package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ClusterUtil {
    @Autowired
    private GroupClusterRepository groupClusterRepository;

    public boolean isIndividualCluster(long clusterId) {
        GroupClusterEntity cluster = groupClusterRepository.findById(clusterId).orElse(null);
        return cluster != null && cluster.getGroupAmount() <= 1;
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
}
