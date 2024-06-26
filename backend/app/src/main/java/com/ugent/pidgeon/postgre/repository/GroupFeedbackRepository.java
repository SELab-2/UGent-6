package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.GroupFeedbackId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface GroupFeedbackRepository extends JpaRepository<GroupFeedbackEntity, GroupFeedbackId> {

    @Query(value = "SELECT * FROM group_feedback WHERE group_id = ?1 AND project_id = ?2", nativeQuery = true)
    GroupFeedbackEntity getGroupFeedback(long groupId, long projectId);

    @Query(value= "SELECT gfb FROM GroupFeedbackEntity gfb WHERE gfb.groupId = ?1")
    List<GroupFeedbackEntity> findGroupFeedbackEntitiesByGroupId(long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE group_feedback SET grade = ?1, feedback = ?4 WHERE group_id = ?2 AND project_id = ?3", nativeQuery = true)
    int updateGroupScore(float grade, long groupId, long projectId, String feedback);
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO group_feedback (grade, group_id, project_id,feedback) VALUES (?1, ?2, ?3, ?4)", nativeQuery = true)
    int addGroupScore(float grade, long groupId, long projectId, String feedback);

    List<GroupFeedbackEntity> findByProjectId(long projectId);
}
