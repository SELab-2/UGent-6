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

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {


    public interface CourseWithRelation {
        CourseEntity getCourse();
        CourseRelation getRelation();
    }

    public interface CourseIdWithRelation {
        Long getCourseId();
        CourseRelation getRelation();
    }

    @Query(value = "SELECT c.id as courseId, cu.relation as relation FROM CourseEntity c JOIN CourseUserEntity cu ON c.id = cu.courseId WHERE cu.userId = ?1")
    List<CourseIdWithRelation> findCourseIdsByUserId(long id);

    /* The 'as' is important here, as it is used to map the result to the CourseWithRelation interface */
    @Query(value = "SELECT c as course, cu.relation as relation FROM CourseEntity c JOIN CourseUserEntity cu ON c.id = cu.courseId WHERE cu.userId = ?1")
    List<CourseWithRelation> findCoursesByUserId(long id);

    @Query(value = "SELECT * FROM users WHERE azure_id = ?1", nativeQuery = true)
    public UserEntity findUserByAzureId(String id);

}
