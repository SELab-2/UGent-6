package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.*;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.time.OffsetDateTime;

@RestController
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProjectController projectController;


    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CourseUserRepository courseUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupClusterRepository groupClusterRepository;

    @Autowired
    private ClusterController groupClusterController;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupUserRepository groupUserRepository;
    @Autowired
    private GroupController groupController;


    public UserReferenceJson userEntityToUserReference(UserEntity user) {
        return new UserReferenceJson(user.getName() + " " + user.getSurname(), ApiRoutes.USER_BASE_PATH + "/" + user.getId());
    }
    public CourseWithInfoJson courseEntityToCourseWithInfo(CourseEntity course) {
        UserEntity teacher = courseRepository.findTeacherByCourseId(course.getId());
        UserReferenceJson teacherJson = userEntityToUserReference(teacher);

        List<UserEntity> assistants = courseRepository.findAssistantsByCourseId(course.getId());
        List<UserReferenceJson> assistantsJson = assistants.stream().map(this::userEntityToUserReference).toList();

        return new CourseWithInfoJson(
                course.getId(),
                course.getName(),
                course.getDescription(),
                teacherJson,
                assistantsJson,
                ApiRoutes.COURSE_BASE_PATH + "/" + course.getId() + "/members",
                getJoinLink(course.getJoinKey(), "" + course.getId())
        );
    }

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
    public ResponseEntity<String> getUserCourses(Auth auth) {
        long userID = auth.getUserEntity().getId();
        try {
            List<UserRepository.CourseIdWithRelation> userCourses = userRepository.findCourseIdsByUserId(userID);

            // Retrieve course entities based on user courses
            List<CourseJSONObject> courseJSONObjects = userCourses.stream()
                    .map(courseWithRelation -> {
                        CourseEntity course = courseRepository.findById(courseWithRelation.getCourseId()).orElse(null);
                        if (course == null) {
                            return null;
                        }
                        return new CourseJSONObject(
                                course.getId(),
                                course.getName(),
                                ApiRoutes.COURSE_BASE_PATH + "/" + course.getId(),
                                courseWithRelation.getRelation()
                        );
                    }
                    )
                    .filter(Objects::nonNull)
                    .toList();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(courseJSONObjects);


            // Return the JSON string in ResponseEntity
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve courses");
        }
    }

    // Hulpobject voor de getmapping mooi in JSON te kunnen zetten.
    private record CourseJSONObject(long courseId, String name, String url, CourseRelation relation) {
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
    public ResponseEntity<CourseWithInfoJson> createCourse(@RequestBody CourseJson courseJson, Auth auth) {
        try {
            UserEntity user = auth.getUserEntity();
            long userId = user.getId();

            // nieuw vak aanmaken
            CourseEntity courseEntity = new CourseEntity(courseJson.getName(), courseJson.getDescription());
            // Get current time and convert to SQL Timestamp
            OffsetDateTime currentTimestamp = OffsetDateTime.now();
            courseEntity.setCreatedAt(currentTimestamp);
            // vak opslaan
            courseRepository.save(courseEntity);

            // leerkracht course relation opslaan
            CourseUserEntity courseUserEntity = new CourseUserEntity(courseEntity.getId(), userId, CourseRelation.creator);
            courseUserRepository.save(courseUserEntity);

            // Create new cluster with size 1 for projects without groups
            GroupClusterEntity groupClusterEntity = new GroupClusterEntity(courseEntity.getId(), 1, "Students", 0);
            groupClusterEntity.setCreatedAt(currentTimestamp);
            groupClusterRepository.save(groupClusterEntity);

            return ResponseEntity.ok(courseEntityToCourseWithInfo(courseEntity));
        } catch (Exception e) {
            Logger.getLogger("CourseController").severe("Error while creating course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private CheckResult getCourseUpdateCheckResult(long courseId, UserEntity user) {
        // het vak selecteren
        CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
        if (courseEntity == null) {
            return new CheckResult(HttpStatus.NOT_FOUND, "Course not found");
        }

        if (user.getRole() != UserRole.admin) {
            // check of de user admin of lesgever is van het vak
            Optional<CourseUserEntity> courseUserEntityOptional = courseUserRepository.findById(new CourseUserId(courseId, user.getId()));
            if (courseUserEntityOptional.isEmpty()) {
                return new CheckResult(HttpStatus.FORBIDDEN, "User is not part of the course");
            }
            CourseUserEntity courseUserEntity = courseUserEntityOptional.get();
            if (courseUserEntity.getRelation() == CourseRelation.enrolled) {
                return new CheckResult(HttpStatus.FORBIDDEN, "User is not allowed to update the course");
            }
        }

        return new CheckResult(HttpStatus.OK, null);
    }

    private ResponseEntity<?> doCourseUpdate(CourseEntity courseEntity, CourseJson courseJson) {
        courseEntity.setName(courseJson.getName());
        courseEntity.setDescription(courseJson.getDescription());
        courseRepository.save(courseEntity);
        return ResponseEntity.ok(courseEntityToCourseWithInfo(courseEntity));
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
            long userId = user.getId();

            CheckResult checkResult = getCourseUpdateCheckResult(courseId, user);
            if (checkResult.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }

            if (courseJson.getName() == null || courseJson.getDescription() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name and description are required");
            }

            return doCourseUpdate(courseRepository.findById(courseId).get(), courseJson);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
public ResponseEntity<?> patchCourse(@RequestBody CourseJson courseJson, @PathVariable long courseId, Auth auth) {
        try {
            UserEntity user = auth.getUserEntity();
            long userId = user.getId();

            CheckResult checkResult = getCourseUpdateCheckResult(courseId, user);
            if (checkResult.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }

            if (courseJson.getName() == null && courseJson.getDescription() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name or description is required");
            }

            CourseEntity courseEntity = courseRepository.findById(courseId).get();
            if (courseJson.getName() == null) {
                courseJson.setName(courseEntity.getName());
            }
            if (courseJson.getDescription() == null) {
                courseJson.setDescription(courseEntity.getDescription());
            }

            return doCourseUpdate(courseEntity, courseJson);
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
    public ResponseEntity<?> getCourseByCourseId(@PathVariable Long courseId, Auth auth) {
        Optional<CourseEntity> courseopt = courseRepository.findById(courseId);
        if (courseopt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");

        }
        CourseEntity course = courseopt.get();
        if (courseUserRepository.findByCourseIdAndUserId(courseId, auth.getUserEntity().getId()).isEmpty() && auth.getUserEntity().getRole() != UserRole.admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not allowed to acces this course");
        }

        return ResponseEntity.ok(courseEntityToCourseWithInfo(course));
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
    public ResponseEntity<?> deleteCourse(@PathVariable long courseId, Auth auth) {
        try {
            // De user vinden
            UserEntity user = auth.getUserEntity();
            long userId = user.getId();

            // het vak selecteren
            CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
            if (courseEntity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
            }

            if (user.getRole() != UserRole.admin) {
                // check of de user admin of lesgever is van het vak
                CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, userId)).orElse(null);
                if (courseUserEntity == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not part of the course");
                }
                if (courseUserEntity.getRelation() != CourseRelation.creator) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not allowed to delete the course");
                }
            }

            //voor elk project
            for (ProjectEntity project : courseRepository.findAllProjectsByCourseId(courseId)) {
                projectController.deleteProjectById(project.getId(), auth);
            }

            //voor elke groepcluster
            for (GroupClusterEntity groupCluster : groupClusterRepository.findByCourseId(courseId)) {
                // We verwijderen de groepfeedback niet omdat die al verwijderd werd wanneer het project verwijderd werd
                groupClusterController.deleteCluster(groupCluster.getId(), auth);
            }

            // Alle CourseUsers verwijderen
            courseUserRepository.deleteAll(courseUserRepository.findAllUsersByCourseId(courseId));

            //vak verwijderen
            courseRepository.delete(courseEntity);
            return ResponseEntity.ok("Vak verwijderd");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Probleem bij vak verwijderen: " + e.getMessage());
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
    public ResponseEntity<?> getProjectByCourseId(@PathVariable Long courseId, Auth auth) {
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
        if (courseUserRepository.findByCourseIdAndUserId(auth.getUserEntity().getId(), courseId).isEmpty() && auth.getUserEntity().getRole() != UserRole.admin){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not allowed access this project");
        }
        UserEntity user = auth.getUserEntity();
        List<ProjectEntity> projects = projectRepository.findByCourseId(courseId);
        List<ProjectResponseJson> projectResponseJsons =  projects.stream().map(projectEntity ->
            projectController.projectEntityToProjectResponseJson(projectEntity, course, user)
        ).toList();

        return ResponseEntity.ok(projectResponseJsons);
    }

    Boolean hasCourseRights(long courseId, UserEntity user) {
        if (user.getRole() == UserRole.admin) {
            return true;
        } else {
            return courseUserRepository.isCourseAdmin(courseId, user.getId());
        }
    }


    private ResponseEntity<?> getJoinResponseEntity(long userId, CourseEntity course, HttpStatus successtatus, Supplier<?> bodySupplier) {
        if (course.getJoinKey() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Course requires a join key. Use " + ApiRoutes.COURSE_BASE_PATH + "/" + course.getId() + "/join/{courseKey}");
        }
        if (courseUserRepository.isCourseMember(course.getId(), userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already a member of the course");
        }
        return ResponseEntity.status(successtatus).body(bodySupplier.get());
    }

    private ResponseEntity<?> getJoinWithKeyResponseEntity(long userId, CourseEntity course, String courseKey, HttpStatus successtatus, Supplier<?> bodySupplier) {
        if (course.getJoinKey() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Course does not require a join key. Use " + ApiRoutes.COURSE_BASE_PATH + "/" + course.getId() + "/join");
        }
        if (!course.getJoinKey().equals(courseKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid join key");
        }
        if (courseUserRepository.isCourseMember(course.getId(), userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already a member of the course");
        }
        return ResponseEntity.status(successtatus).body(bodySupplier.get());
    }

    private boolean createNewIndividualClusterGroup(long courseId, long userId) {
        GroupClusterEntity groupClusterEntity = groupClusterRepository.findIndividualClusterByCourseId(courseId).orElse(null);
        if (groupClusterEntity == null) {
            return false;
        }
        // Create new group for the cluster
        GroupEntity groupEntity = new GroupEntity("", groupClusterEntity.getId());
        groupClusterEntity.setGroupAmount(groupClusterEntity.getGroupAmount() + 1);
        groupClusterRepository.save(groupClusterEntity);
        groupEntity = groupRepository.save(groupEntity);

        // Add user to the group
        GroupUserEntity groupUserEntity = new GroupUserEntity(groupEntity.getId(), userId);
        groupUserRepository.save(groupUserEntity);
        return true;
    }

    private boolean removeIndividualClusterGroup(long courseId, long userId) {
        GroupClusterEntity groupClusterEntity = groupClusterRepository.findIndividualClusterByCourseId(courseId).orElse(null);
        if (groupClusterEntity == null) {
            return false;
        }
        // Find the group of the user
        Optional<GroupEntity> groupEntityOptional = groupRepository.groupByClusterAndUser(groupClusterEntity.getId(), userId);
        return groupEntityOptional.filter(groupEntity -> groupController.removeGroup(groupEntity.getId())).isPresent();
    }

    /**
     * Function to join course with key
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to join
     * @param courseKey key of the course to join
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog TODO
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/join/{courseKey}
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join/{courseKey}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> joinCourse(Auth auth, @PathVariable Long courseId, @PathVariable String courseKey) {
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            if (!createNewIndividualClusterGroup(courseId, auth.getUserEntity().getId())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add user to individual group, contact admin.");
            }
            return getJoinWithKeyResponseEntity(auth.getUserEntity().getId(), course, courseKey, HttpStatus.CREATED, () -> {
                courseUserRepository.save(new CourseUserEntity(courseId, auth.getUserEntity().getId(), CourseRelation.enrolled));
                return null;
            });
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No course with given id");
        }
    }

    /**
     * Function to get course information for joining course with key
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to get the join key from
     * @param courseKey key of the course to get the join key from
     * @return ResponseEntity with a statuscode and a JSON object containing the course information
     * @ApiDog TODO
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/join/{courseKey}
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join/{courseKey}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getCourseJoinKey(Auth auth, @PathVariable Long courseId, @PathVariable String courseKey) {
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            return getJoinWithKeyResponseEntity(auth.getUserEntity().getId(), course, courseKey, HttpStatus.OK, () -> (
                    new CourseJson(course.getName(), course.getDescription())
            ));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No course with given id");
        }
    }

    /**
     * Function to join course without key
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to join
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog TODO
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/join
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> joinCourse(Auth auth, @PathVariable Long courseId) {
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            if (!createNewIndividualClusterGroup(courseId, auth.getUserEntity().getId())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add user to individual group, contact admin.");
            }
            return getJoinResponseEntity(auth.getUserEntity().getId(), course, HttpStatus.CREATED, () -> {
                courseUserRepository.save(new CourseUserEntity(courseId, auth.getUserEntity().getId(), CourseRelation.enrolled));
                return null;
            });
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No course with given id");
        }
    }

    /**
     * Function to get course information for joining course without key
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to get the join key from
     * @return ResponseEntity with a statuscode and a JSON object containing the course information
     * @ApiDog TODO
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/join
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getCourseJoinKey(Auth auth, @PathVariable Long courseId) {
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            return getJoinResponseEntity(auth.getUserEntity().getId(), course, HttpStatus.OK, () -> (
                    new CourseJson(course.getName(), course.getDescription())
            ));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No course with given id");
        }
    }

    /**
     * Function to leave a course
     *
     * @param courseId ID of the course to leave
     * @param auth authentication object of the requesting user
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog TODO
     * @HttpMethod DELETE
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/leave
     */
    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/leave")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> leaveCourse(@PathVariable long courseId, Auth auth) {
        try {
            UserEntity user = auth.getUserEntity();
            long userId = user.getId();

            // het vak selecteren
            CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
            if (courseEntity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
            }

            // check of de user admin of lesgever is van het vak
            Optional<CourseUserEntity> courseUserEntityOptional = courseUserRepository.findById(new CourseUserId(courseId, userId));
            if (courseUserEntityOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not part of the course");
            }
            CourseUserEntity courseUserEntity = courseUserEntityOptional.get();
            if (courseUserEntity.getRelation() == CourseRelation.creator) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not allowed to leave the course");
            }

            // Verwijder de user uit het vak
            courseUserRepository.deleteById(new CourseUserId(courseId, userId));
            if (courseUserEntity.getRelation() == CourseRelation.enrolled) {
                if (!removeIndividualClusterGroup(courseId, userId)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove user from individual group, contact admin.");
                }
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Function to remove a different user from a course
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to leave
     * @param userId JSON object containing the user id
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog TODO
     * @HttpMethod DELETE
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/members
     */
    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.admin, UserRole.student})
    public ResponseEntity<?> removeCourseMember(Auth auth, @PathVariable Long courseId, @RequestBody UserIdJson userId) {
        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
        CourseRelation coursePermissions = courseUserRepository.getCourseRelation(courseId, auth.getUserEntity().getId());
        if (hasCourseRights(courseId, auth.getUserEntity())) {
            Optional<CourseUserEntity> courseUserEntityOptional = courseUserRepository.findById(new CourseUserId(courseId, userId.getUserId()));
            if (courseUserEntityOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            CourseRelation userRelation = courseUserEntityOptional.get().getRelation();
            if (auth.getUserEntity().getId() == userId.getUserId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot remove yourself");
            }
            if (userRelation == CourseRelation.creator) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot remove course creator");
            } else if (userRelation == CourseRelation.course_admin) {
                if (coursePermissions != CourseRelation.creator) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Course admin cannot remove a course admin");
                } else {
                    courseUserRepository.deleteById(new CourseUserId(courseId, userId.getUserId()));
                    return ResponseEntity.ok().build();
                }
            }
            courseUserRepository.deleteById(new CourseUserId(courseId, userId.getUserId()));
            if (userRelation.equals(CourseRelation.enrolled)) {
                if (!removeIndividualClusterGroup(courseId, userId.getUserId())) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove user from individual group, contact admin.");
                }
            }
            return ResponseEntity.ok().build(); // Successfully removed
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No acces to course");
        }
    }

    /**
     * Function to add a different user to a course
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to add the user to
     * @param request JSON object containing the user id and relation
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog TODO
     * @HttpMethod POST
     * @AllowedRoles teacher, admin, student
     * @ApiPath /api/courses/{courseId}/members
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.admin, UserRole.student})
    public ResponseEntity<?> addCourseMember(Auth auth, @PathVariable Long courseId, @RequestBody CourseMemberRequestJson request) {
        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        // Only teacher and admin can add different users to a course.
        if (hasCourseRights(courseId, auth.getUserEntity())) {
            if (request.getRelation() == CourseRelation.creator) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot add a creator");
            }
            boolean isAdmin = auth.getUserEntity().getRole() == UserRole.admin;
            boolean isCreator = courseUserRepository.getCourseRelation(courseId, auth.getUserEntity().getId()) == CourseRelation.creator;
            boolean creatingAdmin = request.getRelation() == CourseRelation.course_admin;
            if (creatingAdmin && (!isCreator && !isAdmin)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only the creator can create more course-admins");
            }
            if (courseUserRepository.isCourseMember(courseId, request.getUserId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already a member of the course");
            }
            courseUserRepository.save(new CourseUserEntity(courseId, request.getUserId(), request.getRelation()));
            if (request.getRelation().equals(CourseRelation.enrolled)) {
                createNewIndividualClusterGroup(courseId, request.getUserId());
            }
            return ResponseEntity.status(HttpStatus.CREATED).build(); // Successfully added
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No acces to course");
        }
    }



    /**
     * Function to update the relation of a user in a course
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to update the user in
     * @param request JSON object containing the user id and relation
     * @return ResponseEntity with a statuscode and no body
     * @ApiDog TODO
     * @HttpMethod PATCH
     * @AllowedRoles teacher, admin
     * @ApiPath /api/courses/{courseId}/members
     */
    @PatchMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateCourseMember(Auth auth, @PathVariable Long courseId, @RequestBody CourseMemberRequestJson request) {
        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }

        // Only teacher and admin can add different users to a course.
        if (hasCourseRights(courseId, auth.getUserEntity())) {
            if (auth.getUserEntity().getId() == request.getUserId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot change your own role.");
            }

            if (request.getRelation() == CourseRelation.course_admin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot change role to creator");
            }

            boolean isAdmin = auth.getUserEntity().getRole() == UserRole.admin;
            boolean isCreator = courseUserRepository.getCourseRelation(courseId, auth.getUserEntity().getId()) == CourseRelation.creator;
            boolean creatingAdmin = request.getRelation() == CourseRelation.course_admin;
            if (creatingAdmin && (!isCreator && !isAdmin)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only the creator can promote to course-admin");
            }
            Optional<CourseUserEntity> ce = courseUserRepository.findById(new CourseUserId(courseId, request.getUserId()));

            if (ce.isPresent()) {
                ce.get().setRelation(request.getRelation());
                courseUserRepository.save(ce.get());
                if (request.getRelation().equals(CourseRelation.enrolled)) {
                    createNewIndividualClusterGroup(courseId, request.getUserId());
                } else {
                    if (!removeIndividualClusterGroup(courseId, request.getUserId())) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove user from individual group, contact admin.");
                    }
                }
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not part of course"); //  User is not allowed to do the action, teachers cant remove students of other users course
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No acces to course");
        }
    }

    /**
     * Function to get all members of a course
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to get the members from
     * @return ResponseEntity with a JSON object containing the members of the course
     * @ApiDog TODO
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/members
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.student}) // student is allowed to see people in its class
    public ResponseEntity<?> getCourseMembers(Auth auth, @PathVariable Long courseId) {
        if (courseRepository.existsById(courseId)) {
            UserEntity ue = auth.getUserEntity();
            if (ue.getRole() == UserRole.admin || courseUserRepository.isCourseMember(courseId, ue.getId())) {
                List<CourseUserEntity> members = courseUserRepository.findAllMembers(courseId);
                List<PublicUserDTO> memberJson = members.stream().map(cue -> {
                    Map<String, Object> json = new HashMap<>();
                    // Assuming `CourseUserEntity` has some properties like `id`, `name`, etc.
                    UserEntity user = userRepository.getReferenceById(cue.getUserId());

                    // Add more properties as needed
                    return new PublicUserDTO(user.getName(), user.getSurname(), user.getEmail());
                }).toList();
                Map<String, Object> resultJson = new HashMap<>();
                return ResponseEntity.ok(memberJson); // Successfully retrieved members
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not a member of the course");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No course with given id");
        }
    }

    public String getJoinLink(String courseKey, String courseId) {
        if (courseKey != null) {
            return ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join/{courseKey}".replace("{courseId}", courseId).replace("{courseKey}", courseKey);
        } else {
            return ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join".replace("{courseId}", courseId);
        }
    }

    /**
     * Function to get the join link of a course
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to get the join link from
     * @return ResponseEntity with the join link of the course
     * @ApiDog TODO
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/joinLink
     */
    @Roles({UserRole.teacher, UserRole.student})
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/joinLink")
    // will return a join key if there is an existing one, otherwise it will return a 404
    public ResponseEntity<String> getCourseKey(Auth auth, @PathVariable Long courseId) {
        if (auth.getUserEntity().getRole() == UserRole.admin || courseUserRepository.isCourseAdmin(courseId, auth.getUserEntity().getId())) {
            if (courseRepository.existsById(courseId)) {
                CourseEntity course = courseRepository.findById(courseId).get();
                return ResponseEntity.ok(getJoinLink(course.getJoinKey(), courseId.toString()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not a course admin, thus not allowed to request the link.");
        }
    }

    // Function for invalidating the previous key and generating a new one, can be useful when staring a new year.
    /**
     * Function to generate a new join link for a course
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to generate the join link for
     * @return ResponseEntity with the new join link of the course
     * @ApiDog TODO
     * @HttpMethod PUT
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/joinLink
     */
    @Roles({UserRole.teacher, UserRole.student})
    @PutMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/joinLink")
    public ResponseEntity<?> getAndGenerateCourseKey(Auth auth, @PathVariable Long courseId) {
        if (auth.getUserEntity().getRole() == UserRole.admin || courseUserRepository.isCourseAdmin(courseId, auth.getUserEntity().getId())) {
            if (courseRepository.existsById(courseId)) {
                CourseEntity course = courseRepository.findById(courseId).get();
                String key = UUID.randomUUID().toString();
                course.setJoinKey(key);
                courseRepository.save(course);
                return ResponseEntity.ok(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join/{courseKey}".replace("{courseId}", courseId.toString()).replace("{courseKey}", key));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not a course admin, thus not allowed to generate a new link.");
        }
    }

    /**
     * Function to remove the joinKey from the joinLink of a course
     *
     * @param auth authentication object of the requesting user
     * @param courseId ID of the course to remove the join link from
     * @return ResponseEntity with the new join link of the course (without the key)
     * @ApiDog TODO
     * @HttpMethod DELETE
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/joinLink
     */
    @Roles({UserRole.teacher, UserRole.student})
    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/joinLink")
    public ResponseEntity<String> deleteCourseKey(Auth auth, @PathVariable Long courseId) {
        if (auth.getUserEntity().getRole() == UserRole.admin || courseUserRepository.isCourseAdmin(courseId, auth.getUserEntity().getId())) {
            if (courseRepository.existsById(courseId)) {
                CourseEntity course = courseRepository.findById(courseId).get();
                course.setJoinKey(null);
                courseRepository.save(course);
                return ResponseEntity.ok(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join".replace("{courseId}", courseId.toString()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not a course admin, thus not allowed to remove the key.");
        }
    }

}
