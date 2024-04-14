package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    List<ProjectEntity> findByCourseId(long courseId);



    @Query(value = """
        SELECT p FROM CourseUserEntity cu
          	JOIN CourseEntity c ON cu.courseId = c.id
          	JOIN ProjectEntity p ON p.courseId = c.id
          	WHERE cu.userId = ?1""")
    List<ProjectEntity> findProjectsByUserId(long userId);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT gu
                FROM GroupUserEntity gu
                INNER JOIN GroupEntity g ON gu.groupId = g.id
                INNER JOIN GroupClusterEntity gc ON g.clusterId = gc.id
                INNER JOIN ProjectEntity p ON p.groupClusterId = gc.id
                WHERE gu.userId = ?2 and p.id = ?1
            ) THEN true ELSE false END""")
    Boolean userPartOfProject(long projectId, long userId);

    @Query(value = """
        SELECT CASE WHEN EXISTS (
            SELECT cu FROM CourseUserEntity cu
            JOIN CourseEntity c ON cu.courseId  = c.id
            JOIN ProjectEntity p ON p.courseId = c.id
            WHERE (
                cu.relation = :#{T(com.ugent.pidgeon.postgre.models.types.CourseRelation).course_admin.toString()}
                OR cu.relation = :#{T(com.ugent.pidgeon.postgre.models.types.CourseRelation).creator.toString()})
            AND cu.userId = ?2 AND p.id = ?1
        ) THEN true ELSE false END
    """)
    Boolean adminOfProject(long projectId, long userId);

    @Query(value = """
            SELECT g.id
            FROM GroupEntity g
            JOIN GroupClusterEntity gc ON g.clusterId = gc.id
            JOIN ProjectEntity p ON p.groupClusterId = gc.id
            WHERE p.id = ?1""")
    List<Long> findGroupIdsByProjectId(long projectId);


}
