package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {
    List<SubmissionEntity> findByGroupIdAndProjectId(long groupId, long projectId);

    @Query("SELECT s FROM SubmissionEntity s WHERE s.projectId = :projectId")
    List<SubmissionEntity> findAllByProjectId(long projectId);
}
