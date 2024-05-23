package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "SELECT u FROM UserEntity u WHERE lower(u.email) = lower(?1)")
    UserEntity findByEmail(String email);

    @Query(value = "SELECT u FROM UserEntity u WHERE lower(u.name) LIKE concat('%', lower(?1), '%') AND lower(u.surname) LIKE concat('%', lower(?2), '%')")
    List<UserEntity> findByName(String name, String surname);




    interface CourseWithRelation {
        CourseEntity getCourse();
        CourseRelation getRelation();
    }

    interface CourseIdWithRelation {
        Long getCourseId();
        CourseRelation getRelation();
        String getName();
    }

    @Query(value = """
        SELECT c.id as courseId, cu.relation as relation, c.name as name
        FROM CourseEntity c JOIN CourseUserEntity cu ON c.id = cu.courseId
        WHERE cu.userId = ?1 AND c.archivedAt IS NULL
        """)
    List<CourseIdWithRelation> findCourseIdsByUserId(long id);

    @Query(value = """
        SELECT c.id as courseId, cu.relation as relation, c.name as name
        FROM CourseEntity c JOIN CourseUserEntity cu ON c.id = cu.courseId
        WHERE cu.userId = ?1 AND c.archivedAt IS NOT NULL
        """)
    List<CourseIdWithRelation> findArchivedCoursesByUserId(long id);

    /* The 'as' is important here, as it is used to map the result to the CourseWithRelation interface */
    @Query(value = "SELECT c as course, cu.relation as relation FROM CourseEntity c JOIN CourseUserEntity cu ON c.id = cu.courseId WHERE cu.userId = ?1")
    List<CourseWithRelation> findCoursesByUserId(long id);

    @Query(value = "SELECT u FROM UserEntity u WHERE u.azureId = ?1")
    Optional<UserEntity> findUserByAzureId(String id);




}
