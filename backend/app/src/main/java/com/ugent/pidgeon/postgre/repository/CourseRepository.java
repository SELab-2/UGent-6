package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    CourseEntity findById(long id);

    @Query(value = "SELECT u FROM UserEntity u JOIN CourseUserEntity cu ON u.id = cu.user_id WHERE cu.course_id = ?1")
    List<UserEntity> findUsersByCourseId(long id);
}
