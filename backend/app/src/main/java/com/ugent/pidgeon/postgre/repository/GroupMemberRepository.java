package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface GroupMemberRepository extends JpaRepository<GroupFeedbackEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM group_users WHERE group_id = ?1 AND user_id = ?2", nativeQuery = true)
    int removeMemberFromGroup(long groupId, long memberId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO group_users (group_id, user_id) VALUES (?1, ?2)", nativeQuery = true)
    int addMemberToGroup(long groupId, long memberId);

    @Query(value = "SELECT u FROM UserEntity u JOIN GroupUserEntity gu ON u.id = gu.userId WHERE gu.groupId = ?1")
    List<UserEntity> findAllMembersByGroupId(long groupId);


}
