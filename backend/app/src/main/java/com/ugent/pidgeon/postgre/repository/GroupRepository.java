package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupRepository extends JpaRepository<GroupEntity, Long>{
    GroupEntity findById(long id);

    @Query(value= "SELECT u FROM UserEntity u JOIN GroupUserEntity gu ON u.id = gu.userId WHERE gu.groupId = ?1")
    List<UserEntity> findCourseUsersByGroupId(long id);
}
