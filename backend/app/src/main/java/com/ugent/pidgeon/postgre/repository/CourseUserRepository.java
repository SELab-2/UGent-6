package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseUserRepository extends JpaRepository<CourseUserEntity, CourseUserId> {

    @Query("SELECT cu FROM CourseUserEntity cu WHERE cu.courseId = :courseId")
    List<CourseUserEntity> findAllUsersByCourseId(long courseId);
}
