package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CourseUtil {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseUserRepository courseUserRepository;

    public CheckResult<CourseEntity> checkCourseAcces(long courseId, UserEntity user) {
        long userId = user.getId();

        // het vak selecteren
        CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
        if (courseEntity == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Course not found", null);
        }

        // check of de user admin of lesgever is van het vak
        CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, userId)).
                orElse(null);
        if (courseUserEntity == null) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not part of the course", null);
        }
        if(courseUserEntity.getRelation() == CourseRelation.enrolled){
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not an admin of the course", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", courseEntity);
    }

    public CheckResult<CourseEntity> getCourseIfExists(long courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
        if (courseEntity == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Course not found", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", courseEntity);
    }
}
