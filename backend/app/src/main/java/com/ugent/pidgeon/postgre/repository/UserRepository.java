package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "SELECT u FROM UserEntity u WHERE lower(u.email) = lower(?1)")
    UserEntity findByEmail(String email);

    @Query(value = "SELECT u FROM UserEntity u WHERE lower(u.name) LIKE concat('%', lower(?1), '%') AND lower(u.surname) LIKE concat('%', lower(?2), '%')")
    List<UserEntity> findByName(String name, String surname);




    public interface CourseWithRelation {
        CourseEntity getCourse();
        CourseRelation getRelation();
    }

    public interface CourseIdWithRelation {
        Long getCourseId();
        CourseRelation getRelation();
        String getName();
    }

    @Query(value = "SELECT c.id as courseId, cu.relation as relation, c.name as name FROM CourseEntity c JOIN CourseUserEntity cu ON c.id = cu.courseId WHERE cu.userId = ?1")
    List<CourseIdWithRelation> findCourseIdsByUserId(long id);

    /* The 'as' is important here, as it is used to map the result to the CourseWithRelation interface */
    @Query(value = "SELECT c as course, cu.relation as relation FROM CourseEntity c JOIN CourseUserEntity cu ON c.id = cu.courseId WHERE cu.userId = ?1")
    List<CourseWithRelation> findCoursesByUserId(long id);

    @Query(value = "SELECT u FROM UserEntity u WHERE u.azureId = ?1")
    public Optional<UserEntity> findUserByAzureId(String id);




}
