package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupRepository extends JpaRepository<GroupEntity, Long>{

    @Query(value= "SELECT u FROM UserEntity u JOIN GroupUserEntity gu ON u.id = gu.userId WHERE gu.groupId = ?1")
    List<UserEntity> findCourseUsersByGroupId(long id);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT gu
                FROM GroupUserEntity gu
                WHERE gu.userId = ?2 and gu.groupId = ?1
            ) THEN true ELSE false END""")
    Boolean userInGroup(long groupId, long userId);


    @Query(value = """
            SELECT p.id FROM ProjectEntity p
            JOIN GroupClusterEntity gc ON p.groupClusterId = gc.id
            JOIN GroupEntity g ON g.clusterId = gc.id
            WHERE g.id = ?1""")
    List<Long> findProjectsByGroupId(long id);


    @Query(value = """
            SELECT g.id FROM GroupEntity g
            JOIN GroupUserEntity gu ON g.id = gu.groupId
            JOIN GroupClusterEntity gc ON g.clusterId = gc.id
            JOIN ProjectEntity p ON p.groupClusterId = gc.id
            WHERE p.id = ?1 AND gu.userId = ?2""")
    Long groupIdByProjectAndUser(long projectId, long userId);



}
