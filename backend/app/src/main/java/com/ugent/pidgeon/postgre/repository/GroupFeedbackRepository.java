package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface GroupFeedbackRepository extends JpaRepository<GroupFeedbackEntity, Long> {

    @Query(value = "SELECT grade FROM group_grades WHERE group_id = ?1 AND exercise_id = ?2", nativeQuery = true)
    GroupFeedbackEntity findByGroupIdAndProjectId(long groupId, long projectId);


    @Modifying
    @Transactional
    @Query(value = "UPDATE group_grades SET grade = ?1 WHERE group_id = ?2 AND exercise_id = ?3", nativeQuery = true)
    int updateGroupScore(float grade, long groupId, long projectId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO group_grades (grade, group_id, exercise_id) VALUES (?1, ?2, ?3)", nativeQuery = true)
    int addGroupScore(float grade, long groupId, long projectId);
}
