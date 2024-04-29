package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.json.*;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import java.util.logging.Logger;
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


    /**
     * Get a course if the user is an admin of the course
     * @param courseId id of the course
     * @param user user that wants to get the course
     * @return CheckResult with the status of the check and the course entity
     */
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

    /**
     * Get a course if the user is part of the course
     * @param courseId id of the course
     * @param user user that wants to get the course
     * @return CheckResult with the status of the check and pair of the course entity and the relation of the user with the course
     */
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


    /**
     * Get a course if it exists
     * @param courseId id of the course
     * @return CheckResult with the status of the check and the course entity
     */
    public CheckResult<CourseEntity> getCourseIfExists(long courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
        if (courseEntity == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Course not found", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", courseEntity);
    }


    /**
     * Check if a user can update/create the relation of a user with a course
     * @param courseId id of the course
     * @param request json with the new course data
     * @param user user that wants to update/create the course
     * @param method http method used to update/create the course
     * @return CheckResult with the status of the check and the course entity
     */
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

    /**
     * Check if a user can leave a course
     * @param courseId id of the course
     * @param user user that wants to leave the course
     * @return CheckResult with the status of the check and the relation of the user with the course
     */
    public CheckResult<CourseRelation> canLeaveCourse(long courseId, UserEntity user) {
        CheckResult<Pair<CourseEntity, CourseRelation>> courseCheck = getCourseIfUserInCourse(courseId, user);
        if (courseCheck.getStatus() != HttpStatus.OK) {
            return new CheckResult<>(courseCheck.getStatus(), courseCheck.getMessage(), null);
        }
        CourseEntity course = courseCheck.getData().getFirst();
        CourseRelation relation = courseCheck.getData().getSecond();
        if (relation.equals(CourseRelation.creator)) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Cannot leave a course you created", null);
        }
        if (course.getArchivedAt() != null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Cannot leave an archived course", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", relation);
    }

    /**
     * Check if a user can delete another user from a course
     * @param courseId id of the course
     * @param userId id of the user to delete
     * @param user user that wants to delete the other user
     * @return CheckResult with the status of the check and the relation of the user with the course
     */
    public CheckResult<CourseRelation> canDeleteUser(long courseId, long userId, UserEntity user) {

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


    /**
     * Get the join link of a course
     * @param courseKey key to join the course (null if course doesn't use a key)
     * @param courseId id of the course
     * @return join link of the course
     */
    public String getJoinLink(String courseKey, String courseId) {
        if (courseKey != null) {
            return ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join/{courseKey}".replace("{courseId}", courseId).replace("{courseKey}", courseKey);
        } else {
            return ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join".replace("{courseId}", courseId);
        }
    }

    /**
     * Check if a join link is valid
     * @param courseId id of the course
     * @param courseKey key to join the course (null if course doesn't use a key)
     * @param user user that wants to join the course
     * @return CheckResult with the status of the check and the course entity
     */
    public CheckResult<CourseEntity> checkJoinLink(long courseId, String courseKey, UserEntity user) {
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "Course not found", null);
        }

        String correctJoinLink = getJoinLink("{courseKey}",""+courseId);
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


    /**
     * Check if a course json is valid
     * @param courseJson json with the course data
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> checkCourseJson(CourseJson courseJson, UserEntity user, Long courseId) {
        // If the courseId is null we are creating a course
        if (courseId != null) {
            CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, user.getId())).orElse(null);
            if (courseUserEntity == null) {
                return new CheckResult<>(HttpStatus.FORBIDDEN, "User is not part of the course", null);
            }

            if (courseJson.getArchived() != null && !courseUserEntity.getRelation().equals(CourseRelation.creator)) {
                return new CheckResult<>(HttpStatus.FORBIDDEN, "Only the course creator can (un)archive the course", null);
            }
        }


        if (courseJson.getName() == null || courseJson.getDescription() == null || courseJson.getYear() == null) {
            Logger.getGlobal().info(""+ courseJson.getYear());
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "name, description and year are required", null);
        }

        if (courseJson.getName().isBlank()) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Name cannot be empty", null);
        }
        return new CheckResult<>(HttpStatus.OK, "", null);
    }


}
