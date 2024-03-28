package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {
    List<SubmissionEntity> findByGroupIdAndProjectId(long groupId, long projectId);



    @Query("SELECT s FROM SubmissionEntity s WHERE s.projectId = :projectId")
    List<SubmissionEntity> findAllByProjectId(long projectId);

    List<SubmissionEntity> findByProjectId(long projectId);


    @Query(value = """
    SELECT s.id
    FROM SubmissionEntity s
    WHERE s.groupId = :groupId
    AND s.submissionTime = (
        SELECT MAX(s2.submissionTime)
        FROM SubmissionEntity s2
        WHERE s2.groupId = :groupId
        AND s2.projectId = :projectId
    ) ORDER BY s.id DESC LIMIT 1
    """)
    Long findLatestsSubmissionIdsByProjectAndGroupId(long projectId, long groupId);

    List<SubmissionEntity> findByProjectIdAndGroupId(long projectid, long groupid);
}
