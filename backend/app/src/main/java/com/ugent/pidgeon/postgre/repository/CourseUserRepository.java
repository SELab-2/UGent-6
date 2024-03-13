package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseUserRepository extends JpaRepository<CourseUserEntity, CourseUserId> {

    @Query(value = """
          SELECT cue FROM CourseUserEntity cue
            JOIN UserEntity ue ON ue.id = cue.userId
            JOIN GroupEntity ge on ge.id = cue.courseId
            """)
    List<CourseUserEntity> findAllMembers(Long courseId);
}