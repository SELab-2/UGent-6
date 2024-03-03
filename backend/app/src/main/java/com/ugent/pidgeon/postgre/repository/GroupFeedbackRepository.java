package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupFeedbackRepository extends JpaRepository<GroupFeedbackEntity, Long> {
    GroupFeedbackEntity findByGroupIdAndProjectId(long groupId, long projectId);
}
