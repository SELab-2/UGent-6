package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    List<ProjectEntity> findByCourseId(long courseId);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT gu
                FROM GroupUserEntity gu
                INNER JOIN GroupEntity g ON gu.groupId = g.id
                INNER JOIN GroupClusterEntity gc ON g.clusterId = gc.id
                INNER JOIN ProjectEntity p ON p.groupClusterId = gc.id
                WHERE gu.userId = ?1 and p.id = ?2
            ) THEN true ELSE false END""")
    Boolean userPartOfProject(long projectId, long userId);
}
