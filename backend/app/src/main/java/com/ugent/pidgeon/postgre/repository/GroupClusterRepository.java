package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface GroupClusterRepository extends JpaRepository<GroupClusterEntity, Long> {
    List<GroupClusterEntity> findByCourseId(long courseId);

    List<GroupClusterEntity> findClustersByCourseId(Long courseid);

    @Query(value = """
    SELECT CASE WHEN EXISTS (
    	SELECT p.id FROM ProjectEntity p WHERE p.groupClusterId = ?1
    ) THEN true ELSE false END
""")
    Boolean usedInProject(long clusterId);

}
