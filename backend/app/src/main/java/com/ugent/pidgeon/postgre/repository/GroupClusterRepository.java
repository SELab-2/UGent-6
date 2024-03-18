package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GroupClusterRepository extends JpaRepository<GroupClusterEntity, Long> {
    List<GroupClusterEntity> findByCourseId(long courseId);

    List<GroupClusterEntity> findClustersByCourseId(Long courseid);

}
