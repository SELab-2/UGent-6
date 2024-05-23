package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupClusterRepository extends JpaRepository<GroupClusterEntity, Long> {
    List<GroupClusterEntity> findByCourseId(long courseId);

    @Query(value = """
        SELECT gc FROM GroupClusterEntity gc
        WHERE gc.courseId = ?1 AND gc.maxSize > 1
    """)
    List<GroupClusterEntity> findClustersWithoutInvidualByCourseId(Long courseid);

    @Query(value = """
        SELECT gc FROM GroupClusterEntity gc
        WHERE gc.courseId = ?1 AND gc.maxSize = 1
    """)
    Optional<GroupClusterEntity> findIndividualClusterByCourseId(Long courseId);

    @Query(value = """
    SELECT CASE WHEN EXISTS (
    	SELECT p.id FROM ProjectEntity p WHERE p.groupClusterId = ?1
    ) THEN true ELSE false END
""")
    Boolean usedInProject(long clusterId);

    @Query(value = """
    SELECT CASE WHEN EXISTS (
    	SELECT gc.id FROM GroupClusterEntity gc 
    	JOIN CourseEntity c ON gc.courseId = c.id
    	WHERE gc.id = ?1 AND c.archivedAt IS NOT NULL
    ) THEN true ELSE false END
    """)
    Boolean inArchivedCourse(long clusterId);

    @Query(value = """
    SELECT CASE WHEN EXISTS (
    	SELECT g.id FROM GroupEntity g 
    	JOIN GroupUserEntity gu ON g.id = gu.groupId
    	WHERE gu.userId = ?2 AND g.clusterId = ?1
    ) THEN true ELSE false END
    """)
    Boolean userInGroupForCluster(long clusterId, long userId);

}
