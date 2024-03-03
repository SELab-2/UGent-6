package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupClusterRepository extends JpaRepository<GroupClusterEntity, Long> {
    List<GroupClusterEntity> findByCourseId(long courseId);
}
