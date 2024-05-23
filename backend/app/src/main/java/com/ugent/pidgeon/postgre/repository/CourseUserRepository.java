package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseUserRepository extends JpaRepository<CourseUserEntity, CourseUserId> {

    @Query(value = """
          SELECT cue FROM CourseUserEntity cue
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


    Optional<CourseUserEntity> findByCourseIdAndUserId(long courseId, long userId);

    @Query(value = """
        SELECT COUNT(*) AS entry_count
        FROM CourseUserEntity cu
        WHERE cu.courseId = :courseId
    """)
    int countUsersInCourse(long courseId);

}
