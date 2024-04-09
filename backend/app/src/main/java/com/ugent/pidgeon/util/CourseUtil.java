package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.json.*;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


@Component
public class CourseUtil {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseUserRepository courseUserRepository;


    @Autowired
    private UserUtil userUtil;


    public CheckResult<CourseEntity> getCourseIfAdmin(long courseId, UserEntity user) {
        CheckResult<Pair<CourseEntity, CourseRelation>> courseCheck = getCourseIfUserInCourse(courseId, user);
        if (courseCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(courseCheck.getStatus(), courseCheck.getMessage(), null);
        }
        CourseRelation relation = courseCheck.getData().getSecond();
        CourseEntity courseEntity = courseCheck.getData().getFirst();

        if(relation.equals(CourseRelation.enrolled) && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not an admin of the course", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", courseEntity);
    }

    public CheckResult<Pair<CourseEntity, CourseRelation>> getCourseIfUserInCourse(long courseId, UserEntity user) {
        CheckResult<CourseEntity> courseCheck = getCourseIfExists(courseId);
        if (courseCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(courseCheck.getStatus(), courseCheck.getMessage(), null);
        }
        CourseEntity courseEntity = courseCheck.getData();
        CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, user.getId())).orElse(null);
        if (courseUserEntity == null && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not part of the course", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, courseUserEntity.getRelation()));
    }

    public CheckResult<CourseEntity> getCourseIfExists(long courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
        if (courseEntity == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Course not found", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", courseEntity);
    }

    public CheckResult<CourseUserEntity> canUpdateUserInCourse(long courseId, CourseMemberRequestJson request, UserEntity user, HttpMethod method) {
        CheckResult<Pair<CourseEntity, CourseRelation>> courseCheck = getCourseIfUserInCourse(courseId, user);
        if (courseCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(courseCheck.getStatus(), courseCheck.getMessage(), null);
        }

        CourseRelation userRelation = courseCheck.getData().getSecond();
        if (userRelation.equals(CourseRelation.enrolled) && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not an admin of the course", null);
        }

        if (request.getUserId() == null || request.getRelation() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "User id and relation are required", null);
        }

        if (request.getRelationAsEnum() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Invalid relation: must be 'enrolled' or 'course_admin'", null);
        }

        CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, request.getUserId())).orElse(null);
        boolean courseMember = courseUserEntity != null;
        if (method.equals(HttpMethod.POST)) {
            if (courseMember) {
                return new CheckResult<>(HttpStatus.BAD_REQUEST, "User is already part of the course", null);
            }
            if (!userUtil.userExists(request.getUserId())) {
                return new CheckResult<>(HttpStatus.BAD_REQUEST, "User does not exist", null);
            }
        } else {
            if (!courseMember) {
                return new CheckResult<>(HttpStatus.BAD_REQUEST, "User is not part of the course", null);
            }
        }

        if (user.getId() == request.getUserId()) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Cannot change your own relation with this course", null);
        }
        if (request.getRelationAsEnum().equals(CourseRelation.creator)) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Cannot change the creator of the course", null);
        }

        boolean isAdmin = user.getRole().equals(UserRole.admin);
        boolean isCreator = userRelation.equals(CourseRelation.creator);
        boolean creatingAdmin = request.getRelationAsEnum().equals(CourseRelation.course_admin);
        if (creatingAdmin && !isAdmin && !isCreator) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Only the course creator can create course admins", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", courseUserEntity);
    }

    public CheckResult<CourseRelation> canLeaveCourse(long courseId, UserEntity user) {
        CheckResult<Pair<CourseEntity, CourseRelation>> courseCheck = getCourseIfUserInCourse(courseId, user);
        if (courseCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(courseCheck.getStatus(), courseCheck.getMessage(), null);
        }
        CourseRelation relation = courseCheck.getData().getSecond();
        if (relation.equals(CourseRelation.creator)) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Cannot leave a course you created", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", relation);
    }

    public CheckResult<CourseRelation> canDeleteUser(long courseId, UserIdJson userIdJson, UserEntity user) {
        if (userIdJson == null || userIdJson.getUserId() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "userid is required", null);
        }

        long userId = userIdJson.getUserId();

        CheckResult<Pair<CourseEntity, CourseRelation>> courseCheck = getCourseIfUserInCourse(courseId, user);
        if (courseCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(courseCheck.getStatus(), courseCheck.getMessage(), null);
        }

        CourseRelation userRelation = courseCheck.getData().getSecond();
        if (userRelation.equals(CourseRelation.enrolled) && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not an admin of the course", null);
        }

        CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, userId)).orElse(null);
        if (courseUserEntity == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "User is not part of the course", null);
        }

        if (user.getId() == userId) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Cannot delete yourself from the course", null);
        }

        if (courseUserEntity.getRelation().equals(CourseRelation.creator)) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Cannot delete the creator of the course", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", courseUserEntity.getRelation());
    }


    public String getJoinLink(String courseKey, String courseId) {
        if (courseKey != null) {
            return ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join/{courseKey}".replace("{courseId}", courseId).replace("{courseKey}", courseKey);
        } else {
            return ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join".replace("{courseId}", courseId);
        }
    }

    public CheckResult<CourseEntity> checkJoinLink(long courseId, String courseKey, UserEntity user) {
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Course not found", null);
        }

        String correctJoinLink = getJoinLink(course.getJoinKey(), "{courseKey}");
        if (courseKey == null && course.getJoinKey() != null) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Course requires a join key. Use " + correctJoinLink, null);
        }
        if (course.getJoinKey() == null && courseKey != null) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Course does not require a join key. Use " + correctJoinLink, null);
        }
        if (course.getJoinKey() != null && !course.getJoinKey().equals(courseKey)) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "Invalid join key", null);
        }
        CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, user.getId())).orElse(null);
        if (courseUserEntity != null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "User is already part of the course", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", course);
    }



    public CheckResult<Void> checkCourseJson(CourseJson courseJson) {
        if (courseJson.getName() == null || courseJson.getDescription() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "name and description are required", null);
        }

        if (courseJson.getName().isBlank()) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Name cannot be empty", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }


}
