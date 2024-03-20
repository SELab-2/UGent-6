package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    @Query("SELECT p FROM ProjectEntity p WHERE p.courseId = :courseId")
    List<ProjectEntity> findAllProjectsByCourseId(long courseId);

    public interface UserWithRelation {
        UserEntity getUser();
        String getRelation();
    }
    /* The 'as' is important here, as it is used to map the result to the CourseWithRelation interface */
    @Query(value = "SELECT u as user, cu.relation as relation FROM UserEntity u JOIN CourseUserEntity cu ON u.id = cu.userId WHERE cu.courseId = ?1")
    List<UserWithRelation> findUsersByCourseId(long id);

    @Query(value = """
        SELECT CASE WHEN EXISTS (
            SELECT cu FROM CourseUserEntity cu
            JOIN CourseEntity c ON cu.courseId  = c.id
            WHERE (
                cu.relation = :#{T(com.ugent.pidgeon.postgre.models.types.CourseRelation).course_admin.toString()}
                OR cu.relation = :#{T(com.ugent.pidgeon.postgre.models.types.CourseRelation).creator.toString()})
            AND cu.userId = ?2 AND c.id = ?1
        ) THEN true ELSE false END
    """)
    Boolean adminOfCourse(long courseId, long userId);
}
