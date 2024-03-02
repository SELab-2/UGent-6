package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.CourseRelation;
import com.ugent.pidgeon.postgre.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {


    UserEntity findById(long id);


    public interface CourseWithRelation {
        CourseEntity getCourse();
        CourseRelation getRelation();
    }

    /* The 'as' is important here, as it is used to map the result to the CourseWithRelation interface */
    @Query(value = "SELECT c as course, cu.relation as relation FROM CourseEntity c JOIN CourseUserEntity cu ON c.id = cu.courseId WHERE cu.userId = ?1")
    List<CourseWithRelation> findCoursesByUserId(long id);
}
