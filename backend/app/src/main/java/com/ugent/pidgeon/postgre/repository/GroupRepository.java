package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupRepository extends JpaRepository<GroupEntity, Long>{
    @Transactional
    @Modifying
    @Query("DELETE FROM GroupUserEntity gu WHERE gu.groupId = ?1")
    void deleteGroupUsersByGroupId(Long groupId);

    @Transactional
    @Modifying
    @Query("DELETE FROM SubmissionEntity gs WHERE gs.groupId = ?1")
    void deleteSubmissionsByGroupId(Long groupId);

    @Transactional
    @Modifying
    @Query("DELETE FROM GroupFeedbackEntity gf WHERE gf.groupId = ?1")
    void deleteGroupFeedbacksByGroupId(Long groupId);

    @Query(value= "SELECT u FROM UserEntity u JOIN GroupUserEntity gu ON u.id = gu.userId WHERE gu.groupId = ?1")
    List<UserEntity> findGroupUsersByGroupId(long id);

    public interface UserReference {
        Long getUserId();
        String getName();
    }
    @Query(value= """
            SELECT gu.userId as userId, u.name, CONCAT(u.name, ' ', u.surname) as name
            FROM GroupUserEntity gu JOIN UserEntity u ON u.id = gu.userId
            WHERE gu.groupId = ?1""")
    List<UserReference> findGroupUsersReferencesByGroupId(long id);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT gu
                FROM GroupUserEntity gu
                WHERE gu.userId = ?2 and gu.groupId = ?1
            ) THEN true ELSE false END""")
    Boolean userInGroup(long groupId, long userId);

    // Having access to a group means the user should be in the course
    @Query(value = """
        SELECT CASE WHEN EXISTS (
            SELECT g FROM GroupEntity g
            JOIN GroupClusterEntity gc ON g.clusterId = gc.id
            JOIN ProjectEntity p ON p.groupClusterId = gc.id
            JOIN CourseEntity c ON p.courseId = c.id
            JOIN CourseUserEntity cu ON cu.courseId = c.id
            WHERE cu.userId = ?1 AND g.id = ?2
        ) THEN true ELSE false END
""")
    Boolean userAccessToGroup(long userId, long groupId);


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

    List<GroupEntity> findAllByClusterId(long CusterId);

}
