package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {
    List<SubmissionEntity> findByGroupIdAndProjectId(long groupId, long projectId);



    @Query("SELECT s FROM SubmissionEntity s WHERE s.projectId = :projectId")
    List<SubmissionEntity> findAllByProjectId(long projectId);

    List<SubmissionEntity> findByProjectId(long projectId);


    @Query(value = """
    SELECT s
    FROM SubmissionEntity s
    WHERE s.groupId = :groupId
    AND s.submissionTime = (
        SELECT MAX(s2.submissionTime)
        FROM SubmissionEntity s2
        WHERE s2.groupId = :groupId
        AND s2.projectId = :projectId
        AND s2.dockerTestState != :#{T(com.ugent.pidgeon.postgre.models.types.DockerTestState).running.toString()}
    ) ORDER BY s.id DESC LIMIT 1
    """)
    Optional<SubmissionEntity> findLatestsSubmissionIdsByProjectAndGroupId(long projectId, long groupId);

    @Query(value = """
      SELECT s FROM SubmissionEntity s
      WHERE s.projectId = :projectId
      AND s.groupId IS NULL
      """)
    List<SubmissionEntity> findAdminSubmissionsByProjectId(long projectId);

    List<SubmissionEntity> findByProjectIdAndGroupId(long projectid, long groupid);
}
