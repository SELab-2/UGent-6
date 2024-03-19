package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseUserRepository extends JpaRepository<CourseUserEntity, CourseUserId> {

    @Query(value = """
          SELECT cue FROM CourseUserEntity cue
            JOIN UserEntity ue ON ue.id = cue.userId
            JOIN GroupEntity ge on ge.id = cue.courseId
            WHERE cue.courseId = ?1
            """)
    List<CourseUserEntity> findAllMembers(Long courseId);
    @Query(value = """
        SELECT CASE WHEN EXISTS (
            SELECT cu FROM CourseUserEntity cu
            WHERE (
                (cu.relation = :#{T(com.ugent.pidgeon.postgre.models.types.CourseRelation).course_admin.toString()}
                OR cu.relation = :#{T(com.ugent.pidgeon.postgre.models.types.CourseRelation).creator.toString()})
            AND cu.userId = ?2 AND cu.courseId = ?1)
        ) THEN true ELSE false END
    """)
    Boolean isCourseAdmin(long courseId, long userId);

    @Query(value = """
            SELECT cu.relation FROM CourseUserEntity cu
            WHERE cu.userId = ?2 AND cu.courseId = ?1
    """)
    CourseRelation getCourseRelation(long courseId, long userId);
    @Query(value = """
        SELECT CASE WHEN EXISTS (
            SELECT cu FROM CourseUserEntity cu
            WHERE (cu.userId = ?2 AND cu.courseId = ?1)
        ) THEN true ELSE false END
    """)
    Boolean isCourseMember(long courseId, long userId);

    @Query("SELECT cu FROM CourseUserEntity cu WHERE cu.courseId = :courseId")
    List<CourseUserEntity> findAllUsersByCourseId(long courseId);
}