package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.json.CourseJoinInformationJson;
import com.ugent.pidgeon.json.CourseJson;
import com.ugent.pidgeon.json.CourseMemberRequestJson;
import com.ugent.pidgeon.json.CourseWithRelationJson;
import com.ugent.pidgeon.json.RelationRequest;
import com.ugent.pidgeon.json.UserReferenceWithRelation;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.CommonDatabaseActions;
import com.ugent.pidgeon.util.CourseUtil;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.Pair;
import com.ugent.pidgeon.util.UserUtil;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private CourseUserRepository courseUserRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupClusterRepository groupClusterRepository;

    @Autowired
    private UserUtil userUtil;
    @Autowired
    private CourseUtil courseUtil;
    @Autowired
    private CommonDatabaseActions commonDatabaseActions;
    @Autowired
    private EntityToJsonConverter entityToJsonConverter;


    /**
     * Function to retrieve all courses of a user
     *
     * @param auth authentication object of the requesting user
     * @return ResponseEntity with a JSON string containing the courses of the user
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6091747">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getUserCourses(Auth auth, @RequestParam(value = "archived", required = false) Boolean archived) {
        long userID = auth.getUserEntity().getId();
        try {
            List<UserRepository.CourseIdWithRelation> userCourses = new ArrayList<>();
            if (archived == null || !archived) {
                userCourses.addAll(userRepository.findCourseIdsByUserId(userID));
            }
            if (archived == null || archived) {
                userCourses.addAll(userRepository.findArchivedCoursesByUserId(userID));
            }

            // Retrieve course entities based on user courses
            List<CourseWithRelationJson> courseJSONObjects = userCourses.stream()
                    .map(courseWithRelation -> {
                                CourseEntity course = courseRepository.findById(courseWithRelation.getCourseId()).orElse(null);
                                if (course == null) {
                                    return null;
                                }
                                return entityToJsonConverter.courseEntityToCourseWithRelation(course, courseWithRelation.getRelation());
                            }
                    )
                    .filter(Objects::nonNull)
                    .toList();
            for (CourseWithRelationJson courseJson: courseJSONObjects) {
                Logger.getGlobal().info("UserCourses: " + courseJson);

            }
            // Return the JSON string in ResponseEntity
            return ResponseEntity.ok(courseJSONObjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve courses");
        }
    }

    /**
     * Function to create a new course
     *
     * @param courseJson JSON object containing the course name and description
     * @param auth       authentication object of the requesting user
     * @return ResponseEntity with the created course entity
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723791">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher
     * @ApiPath /api/courses
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH)
    @Roles({UserRole.teacher})
    public ResponseEntity<?> createCourse(@RequestBody CourseJson courseJson, Auth auth) {
        try {
            UserEntity user = auth.getUserEntity();
            long userId = user.getId();

            CheckResult<Void> courseJsonCheck = courseUtil.checkCourseJson(courseJson, user, null);
            if (courseJsonCheck.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(courseJsonCheck.getStatus()).body(courseJsonCheck.getMessage());
            }

            // Create new course
            CourseEntity courseEntity = new CourseEntity(courseJson.getName(), courseJson.getDescription(), courseJson.getYear());
            // Get current time and convert to SQL Timestamp
            OffsetDateTime currentTimestamp = OffsetDateTime.now();
            courseEntity.setCreatedAt(currentTimestamp);
            courseEntity.setJoinKey(UUID.randomUUID().toString());
            // Save course
            courseEntity = courseRepository.save(courseEntity);

            // Add user as course creator
            CourseUserEntity courseUserEntity = new CourseUserEntity(courseEntity.getId(), userId, CourseRelation.creator);
            courseUserRepository.save(courseUserEntity);

            // Create new cluster with size 1 for projects without groups
            GroupClusterEntity groupClusterEntity = new GroupClusterEntity(courseEntity.getId(), 1, "Students", 0);
            groupClusterEntity.setCreatedAt(currentTimestamp);
            groupClusterRepository.save(groupClusterEntity);

            return ResponseEntity.ok(entityToJsonConverter.courseEntityToCourseWithInfo(courseEntity, courseUtil.getJoinLink(courseEntity.getJoinKey(), "" + courseEntity.getId()), false));
        } catch (Exception e) {
            Logger.getLogger("CourseController").severe("Error while creating course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    private ResponseEntity<?> doCourseUpdate(CourseEntity courseEntity, CourseJson courseJson, UserEntity user) {
        CheckResult<Void> courseJsonCheck = courseUtil.checkCourseJson(courseJson, user, courseEntity.getId());
        if (courseJsonCheck.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(courseJsonCheck.getStatus()).body(courseJsonCheck.getMessage());
        }
        courseEntity.setName(courseJson.getName());
        courseEntity.setDescription(courseJson.getDescription());
        courseEntity.setCourseYear(courseJson.getYear());
        if (courseJson.getArchived() != null) {
            courseEntity.setArchivedAt(courseJson.getArchived() ? OffsetDateTime.now() : null);
        }
        courseEntity = courseRepository.save(courseEntity);
        return ResponseEntity.ok(entityToJsonConverter.courseEntityToCourseWithInfo(courseEntity, courseUtil.getJoinLink(courseEntity.getJoinKey(), "" + courseEntity.getId()), false));
    }

    /**
     * Function to update a course
     *
     * @param courseJson JSON object containing the course name and description
     * @param courseId   ID of the course to update
     * @param auth       authentication object of the requesting user
     * @return ResponseEntity with the updated course entity
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723806">apiDog documentation</a>
     * @HttpMethod PUT
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}
     */
    @PutMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateCourse(@RequestBody CourseJson courseJson, @PathVariable long courseId, Auth auth) {
        try {
            UserEntity user = auth.getUserEntity();

            CheckResult<CourseEntity> checkResult = courseUtil.getCourseIfAdmin(courseId, user);
            if (checkResult.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }
            CourseEntity course = checkResult.getData();

            return doCourseUpdate(course, courseJson, user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Function to update a course
     *
     * @param courseJson JSON object containing the course name and description
     * @param courseId   ID of the course to update
     * @param auth       authentication object of the requesting user
     * @return ResponseEntity with the updated course entity
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6678309">apiDog documentation</a>
     * @HttpMethod PATCH
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}
     */
    @PatchMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> patchCourse(@RequestBody CourseJson courseJson, @PathVariable long courseId, Auth auth) {
        try {
            UserEntity user = auth.getUserEntity();

            CheckResult<CourseEntity> checkResult = courseUtil.getCourseIfAdmin(courseId, user);
            if (checkResult.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }

            CourseEntity courseEntity = checkResult.getData();
            if (courseJson.getName() == null) {
                courseJson.setName(courseEntity.getName());
            }
            if (courseJson.getDescription() == null) {
                courseJson.setDescription(courseEntity.getDescription());
            }
            if (courseJson.getYear() == null) {
                courseJson.setYear(courseEntity.getCourseYear());
            }

            return doCourseUpdate(courseEntity, courseJson, user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Function to retrieve a course by its ID
     *
     * @param courseId ID of the course to retrieve
     * @param auth     authentication object of the requesting user
     * @return ResponseEntity with the course entity
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723783">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getCourseByCourseId(@PathVariable long courseId, Auth auth) {
        CheckResult<Pair<CourseEntity, CourseRelation>> checkResult = courseUtil.getCourseIfUserInCourse(courseId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        CourseEntity course = checkResult.getData().getFirst();
        CourseRelation relation = checkResult.getData().getSecond();

        return ResponseEntity.ok(entityToJsonConverter.courseEntityToCourseWithInfo(course, courseUtil.getJoinLink(course.getJoinKey(), "" + course.getId()), relation.equals(CourseRelation.enrolled)));
    }


    /**
     * Function to delete a course by its ID
     *
     * @param courseId ID of the course to delete
     * @param auth     authentication object of the requesting user
     * @return ResponseEntity with a message indicating the result of the deletion
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723808">apiDog documentation</a>
     * @HttpMethod DELETE
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}
     */
    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    @Transactional
    public ResponseEntity<?> deleteCourse(@PathVariable long courseId, Auth auth) {
        try {
            CheckResult<Pair<CourseEntity, CourseRelation>> checkResult = courseUtil.getCourseIfUserInCourse(courseId, auth.getUserEntity());
            if (checkResult.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }
            if (!checkResult.getData().getSecond().equals(CourseRelation.creator)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the creator of a course can delete it");
            }

            // Delete all projects linked to the course
            for (ProjectEntity project : courseRepository.findAllProjectsByCourseId(courseId)) {
                CheckResult<Void> deleteResult = commonDatabaseActions.deleteProject(project.getId());
                if (deleteResult.getStatus() != HttpStatus.OK) {
                    return ResponseEntity.status(deleteResult.getStatus()).body(deleteResult.getMessage());
                }
            }

            List<GroupClusterEntity> clusters = groupClusterRepository.findByCourseId(courseId);

            // Delete all groupclusters linked to the course
            for (GroupClusterEntity groupCluster : clusters) {
                // We don't delete groupfeedback as these have been deleted with the projects
                CheckResult<Void> deleteResult = commonDatabaseActions.deleteClusterById(groupCluster.getId());
                if (deleteResult.getStatus() != HttpStatus.OK) {
                    return ResponseEntity.status(deleteResult.getStatus()).body(deleteResult.getMessage());
                }
            }

            Iterable<CourseUserEntity> courseUsers = courseUserRepository.findAllUsersByCourseId(courseId);
            // Delete all courseusers linked to the course
            courseUserRepository.deleteAll(courseUsers);

            // Delete the course
            courseRepository.deleteById(courseId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error while deleting course");
        }
    }

    /**
     * Function to retrieve all projects of a course
     *
     * @param courseId ID of the course to retrieve the projects from
     * @param auth     authentication object of the requesting user
     * @return ResponseEntity with a JSON string containing the projects of the course
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723840">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/projects
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/projects")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getProjectsByCourseId(@PathVariable Long courseId, Auth auth) {
        UserEntity user = auth.getUserEntity();

        CheckResult<Pair<CourseEntity, CourseRelation>> checkResult = courseUtil.getCourseIfUserInCourse(courseId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        CourseEntity course = checkResult.getData().getFirst();
        CourseRelation relation = checkResult.getData().getSecond();

        List<ProjectEntity> projects = projectRepository.findByCourseId(courseId);
        if (relation.equals(CourseRelation.enrolled)) {
            projects = projects.stream().filter(ProjectEntity::isVisible).toList();
        }
        List<ProjectResponseJson> projectResponseJsons = projects.stream().map(projectEntity ->
                entityToJsonConverter.projectEntityToProjectResponseJson(projectEntity, course, user)
        ).toList();


        return ResponseEntity.ok(projectResponseJsons);
    }

    private ResponseEntity<?> getJoinLinkPostResponseEntity(long courseId, String courseKey, UserEntity user) {
        CheckResult<CourseEntity> checkResult = courseUtil.checkJoinLink(courseId, courseKey, user);
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        CourseEntity course = checkResult.getData();
        if (course.getArchivedAt() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Course is archived");
        }
        if (!commonDatabaseActions.createNewIndividualClusterGroup(courseId, user)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add user to individual group, contact admin.");
        }
        courseUserRepository.save(new CourseUserEntity(courseId, user.getId(), CourseRelation.enrolled));
        return ResponseEntity.ok(entityToJsonConverter.courseEntityToCourseWithInfo(course, courseUtil.getJoinLink(course.getJoinKey(), "" + course.getId()), false));
    }

    private ResponseEntity<?> getJoinLinkGetResponseEntity(long courseId, String courseKey, UserEntity user) {
        CheckResult<CourseEntity> checkResult = courseUtil.checkJoinLink(courseId, courseKey, user);
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        CourseEntity course = checkResult.getData();
        if (course.getArchivedAt() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Course is archived");
        }
        CourseJoinInformationJson courseJson = new CourseJoinInformationJson(course.getName(), course.getDescription());
        return ResponseEntity.ok(courseJson);
    }

    /**
     * Function to join course with key
     *
     * @param auth      authentication object of the requesting user
     * @param courseId  ID of the course to join
     * @param courseKey key of the course to join
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6698810">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/join/{courseKey}
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join/{courseKey}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> joinCourse(Auth auth, @PathVariable Long courseId, @PathVariable String courseKey) {
        return getJoinLinkPostResponseEntity(courseId, courseKey, auth.getUserEntity());
    }

    /**
     * Function to get course information for joining course with key
     *
     * @param auth      authentication object of the requesting user
     * @param courseId  ID of the course to get the join key from
     * @param courseKey key of the course to get the join key from
     * @return ResponseEntity with a statuscode and a JSON object containing the course information
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6698818">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/join/{courseKey}
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join/{courseKey}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getCourseJoinInformation(Auth auth, @PathVariable Long courseId, @PathVariable String courseKey) {
        return getJoinLinkGetResponseEntity(courseId, courseKey, auth.getUserEntity());
    }

    /**
     * Function to join course without key
     *
     * @param auth     authentication object of the requesting user
     * @param courseId ID of the course to join
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6698821">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/join
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> joinCourse(Auth auth, @PathVariable Long courseId) {
        return getJoinLinkPostResponseEntity(courseId, null, auth.getUserEntity());
    }

    /**
     * Function to get course information for joining course without key
     *
     * @param auth     authentication object of the requesting user
     * @param courseId ID of the course to get the join key from
     * @return ResponseEntity with a statuscode and a JSON object containing the course information
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6768820">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/join
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getCourseJoinInformation(Auth auth, @PathVariable Long courseId) {
        return getJoinLinkGetResponseEntity(courseId, null, auth.getUserEntity());
    }

    /**
     * Function to leave a course
     *
     * @param courseId ID of the course to leave
     * @param auth     authentication object of the requesting user
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6698775">apiDog documentation</a>
     * @HttpMethod DELETE
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/leave
     */
    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/leave")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> leaveCourse(@PathVariable long courseId, Auth auth) {
        try {
            long userId = auth.getUserEntity().getId();
            CheckResult<CourseRelation> checkResult = courseUtil.canLeaveCourse(courseId, auth.getUserEntity());
            return doRemoveFromCourse(courseId, userId, checkResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Function to remove a different user from a course
     *
     * @param auth     authentication object of the requesting user
     * @param courseId ID of the course to leave
     * @param userId   JSON object containing the user id
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883724">apiDog documentation</a>
     * @HttpMethod DELETE
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/members/{userId}
     */
    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members/{userId}")
    @Roles({UserRole.teacher, UserRole.admin, UserRole.student})
    public ResponseEntity<?> removeCourseMember(Auth auth, @PathVariable Long courseId, @PathVariable Long userId) {
        CheckResult<CourseRelation> checkResult = courseUtil.canDeleteUser(courseId, userId, auth.getUserEntity());
        return doRemoveFromCourse(courseId, userId, checkResult);
    }

    @NotNull
    private ResponseEntity<?> doRemoveFromCourse(
        Long courseId,
        Long userId,
        CheckResult<CourseRelation> checkResult) {
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        CourseRelation userRelation = checkResult.getData();

        courseUserRepository.deleteById(new CourseUserId(courseId, userId));
        if (userRelation.equals(CourseRelation.enrolled)) {
            if (!commonDatabaseActions.removeIndividualClusterGroup(courseId, userId)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove user from individual group, contact admin.");
            }
        }
        return ResponseEntity.ok().build(); // Successfully removed
    }

    /**
     * Function to add a different user to a course
     *
     * @param auth     authentication object of the requesting user
     * @param courseId ID of the course to add the user to
     * @param request  JSON object containing the user id and relation
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6697093">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher, admin, student
     * @ApiPath /api/courses/{courseId}/members
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.admin, UserRole.student})
    @Transactional
    public ResponseEntity<?> addCourseMember(Auth auth, @PathVariable Long courseId, @RequestBody CourseMemberRequestJson request) {
        CheckResult<CourseUserEntity> checkResult = courseUtil.canUpdateUserInCourse(courseId, request, auth.getUserEntity(), HttpMethod.POST);
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        courseUserRepository.save(new CourseUserEntity(courseId, request.getUserId(), request.getRelationAsEnum()));
        if (request.getRelationAsEnum().equals(CourseRelation.enrolled)) {
            UserEntity user = userUtil.getUserIfExists(request.getUserId());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            boolean succesful = commonDatabaseActions.createNewIndividualClusterGroup(courseId, user);
            if (!succesful) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add user to individual group, contact admin.");
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).build(); // Successfully added
    }

    /**
     * Function to update the relation of a user in a course
     *
     * @param auth     authentication object of the requesting user
     * @param courseId ID of the course to update the user in
     * @param request  JSON object containing the user id and relation
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883731">apiDog documentation</a>
     * @HttpMethod PATCH
     * @AllowedRoles teacher, admin
     * @ApiPath /api/courses/{courseId}/members/{userId}
     */
    @PatchMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members/{userId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateCourseMember(Auth auth, @PathVariable Long courseId, @RequestBody RelationRequest request, @PathVariable long userId) {
        CourseMemberRequestJson requestwithid = new CourseMemberRequestJson();
        requestwithid.setUserId(userId);
        requestwithid.setRelation(request.getRelation());

        CheckResult<CourseUserEntity> checkResult = courseUtil.canUpdateUserInCourse(courseId, requestwithid, auth.getUserEntity(), HttpMethod.PATCH);
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        CourseUserEntity courseUserEntity = checkResult.getData();
        if (courseUserEntity.getRelation().equals(request.getRelationAsEnum())) {
            return ResponseEntity.ok().build();
        }

        if (request.getRelationAsEnum().equals(CourseRelation.enrolled)) {
            UserEntity user = userUtil.getUserIfExists(requestwithid.getUserId());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            if (!commonDatabaseActions.createNewIndividualClusterGroup(courseId, user)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add user to individual group, contact admin.");
            }
        } else if (courseUserEntity.getRelation().equals(CourseRelation.enrolled)){
            if (!commonDatabaseActions.removeIndividualClusterGroup(courseId, requestwithid.getUserId())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove user from individual group, contact admin.");
            }
        }

        courseUserEntity.setRelation(request.getRelationAsEnum());
        courseUserRepository.save(courseUserEntity);

        return ResponseEntity.ok().build();
    }

    /**
     * Function to get all members of a course
     *
     * @param auth     authentication object of the requesting user
     * @param courseId ID of the course to get the members from
     * @return ResponseEntity with a JSON object containing the members of the course
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5724006">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/members
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.student}) // student is allowed to see people in its class
    public ResponseEntity<?> getCourseMembers(Auth auth, @PathVariable Long courseId) {
        CheckResult<Pair<CourseEntity, CourseRelation>> checkResult = courseUtil.getCourseIfUserInCourse(courseId, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        boolean hideStudentNumber = checkResult.getData().getSecond().equals(CourseRelation.enrolled);

        List<CourseUserEntity> members = courseUserRepository.findAllMembers(courseId);
        List<UserReferenceWithRelation> memberJson = members.stream().
                map(cue -> {
                    UserEntity user = userUtil.getUserIfExists(cue.getUserId());
                    if (user == null) {
                        return null;
                    }
                    return entityToJsonConverter.userEntityToUserReferenceWithRelation(user, cue.getRelation(), hideStudentNumber);
                }).
                filter(Objects::nonNull).toList();

        return ResponseEntity.status(HttpStatus.OK).body(memberJson);
    }

    /**
     * Function to get the join link of a course
     *
     * @param auth     authentication object of the requesting user
     * @param courseId ID of the course to get the join link from
     * @return ResponseEntity with the join link of the course
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6698763">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/joinLink
     */
    @Roles({UserRole.teacher, UserRole.student})
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/joinKey")
    public ResponseEntity<String> getCourseKey(Auth auth, @PathVariable Long courseId) {
        CheckResult<CourseEntity> checkResult = courseUtil.getCourseIfAdmin(courseId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        return ResponseEntity.ok(checkResult.getData().getJoinKey());
    }

    // Function for invalidating the previous key and generating a new one, can be useful when starting a new year.

    /**
     * Function to generate a new join link for a course
     *
     * @param auth     authentication object of the requesting user
     * @param courseId ID of the course to generate the join link for
     * @return ResponseEntity with the new join link of the course
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6691656">apiDog documentation</a>
     * @HttpMethod PUT
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/joinLink
     */
    @Roles({UserRole.teacher, UserRole.student})
    @PutMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/joinKey")
    public ResponseEntity<?> getAndGenerateCourseKey(Auth auth, @PathVariable Long courseId) {
        CheckResult<CourseEntity> checkResult = courseUtil.getCourseIfAdmin(courseId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        CourseEntity course = checkResult.getData();

        String key = UUID.randomUUID().toString();
        course.setJoinKey(key);
        courseRepository.save(course);
        return ResponseEntity.ok(key);
    }

    /**
     * Function to remove the joinKey from the joinLink of a course
     *
     * @param auth     authentication object of the requesting user
     * @param courseId ID of the course to remove the join link from
     * @return ResponseEntity with the new join link of the course (without the key)
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6698823">apiDog documentation</a>
     * @HttpMethod DELETE
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/joinLink
     */
    @Roles({UserRole.teacher, UserRole.student})
    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/joinKey")
    public ResponseEntity<String> deleteCourseKey(Auth auth, @PathVariable Long courseId) {
        CheckResult<CourseEntity> checkResult = courseUtil.getCourseIfAdmin(courseId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        CourseEntity course = checkResult.getData();
        course.setJoinKey(null);
        courseRepository.save(course);
        return ResponseEntity.ok("");
    }

    /**
     * Function to copy a course
     *
     * @param courseId ID of the course to copy
     * @param auth     authentication object of the requesting user
     * @return ResponseEntity with the copied course entity
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-7254402">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher
     * @ApiPath /api/courses/{courseId}/copy
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/copy")
    @Roles({UserRole.teacher})
    @Transactional
    public ResponseEntity<?> copyCourse(@PathVariable long courseId, Auth auth) {
        try {
            CheckResult<Pair<CourseEntity, CourseRelation>> checkResult = courseUtil.getCourseIfUserInCourse(courseId, auth.getUserEntity());
            if (checkResult.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }
            if (!checkResult.getData().getSecond().equals(CourseRelation.creator)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the creator of a course can copy it");
            }

            CourseEntity course = checkResult.getData().getFirst();

            CheckResult<CourseEntity> copyCheckRes = commonDatabaseActions.copyCourse(course, auth.getUserEntity().getId());
            if (copyCheckRes.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(copyCheckRes.getStatus()).body(copyCheckRes.getMessage());
            }
            CourseEntity newCourse = copyCheckRes.getData();

            return ResponseEntity.status(HttpStatus.CREATED).body(entityToJsonConverter.courseEntityToCourseWithInfo(newCourse, courseUtil.getJoinLink(newCourse.getJoinKey(), "" + newCourse.getId()), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

}
