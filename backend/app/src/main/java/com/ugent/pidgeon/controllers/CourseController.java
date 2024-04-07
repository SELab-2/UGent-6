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
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestController
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProjectController projectController;


    @Autowired
    private ProjectRepository projectRepository;

    @Autowired

    private TestRepository testRepository;

    @Autowired
    private CourseUserRepository courseUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupClusterRepository groupClusterRepository;

    @Autowired
    private ClusterController groupClusterController;


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
            Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
            courseEntity.setCreatedAt(currentTimestamp);
            // vak opslaan
            courseRepository.save(courseEntity);

            // leerkracht course relation opslaan
            CourseUserEntity courseUserEntity = new CourseUserEntity(courseEntity.getId(), userId, CourseRelation.creator);
            courseUserRepository.save(courseUserEntity);

            // Create new cluster with size 1 for projects without groups
            GroupClusterEntity groupClusterEntity = new GroupClusterEntity(courseEntity.getId(), 1, "Students", 0);
            groupClusterRepository.save(groupClusterEntity);

            return ResponseEntity.ok(courseEntityToCourseWithInfo(courseEntity));
        } catch (Exception e) {
            Logger.getLogger("CourseController").severe("Error while creating course: " + e.getMessage());
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

            // het vak selecteren
            CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
            if (courseEntity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
            }

            if (user.getRole() != UserRole.admin) {
                // check of de user admin of lesgever is van het vak
                Optional<CourseUserEntity> courseUserEntityOptional = courseUserRepository.findById(new CourseUserId(courseId, userId));
                if (courseUserEntityOptional.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not part of the course");
                }
                CourseUserEntity courseUserEntity = courseUserEntityOptional.get();
                if (courseUserEntity.getRelation() == CourseRelation.enrolled) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not allowed to update the course");
                }
            }

            //update velden
            courseEntity.setName(courseJson.getName());
            courseEntity.setDescription(courseJson.getDescription());
            courseRepository.save(courseEntity);

            // Response verzenden
            return ResponseEntity.ok(courseEntityToCourseWithInfo(courseEntity));
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
        if (courseUserRepository.findByCourseIdAndUserId(auth.getUserEntity().getId(), courseId).isEmpty() && auth.getUserEntity().getRole() != UserRole.admin) {
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

    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join/{courseKey}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> joinCourse(Auth auth, @PathVariable Long courseId, @PathVariable String courseKey) {
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            return getJoinWithKeyResponseEntity(auth.getUserEntity().getId(), course, courseKey, HttpStatus.CREATED, () -> {
                courseUserRepository.save(new CourseUserEntity(courseId, auth.getUserEntity().getId(), CourseRelation.enrolled));
                return null;
            });
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No course with given id");
        }
    }

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

    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> joinCourse(Auth auth, @PathVariable Long courseId) {
        CourseEntity course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            return getJoinResponseEntity(auth.getUserEntity().getId(), course, HttpStatus.CREATED, () -> {
                courseUserRepository.save(new CourseUserEntity(courseId, auth.getUserEntity().getId(), CourseRelation.enrolled));
                return null;
            });
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No course with given id");
        }
    }

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
            return ResponseEntity.ok().build(); // Successfully removed
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No acces to course");
        }
    }

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
            return ResponseEntity.status(HttpStatus.CREATED).build(); // Successfully added
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No acces to course");
        }
    }


    @PatchMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.admin})
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
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build(); //  User is not allowed to do the action, teachers cant remove students of other users course
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No acces to course");
        }
    }

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
