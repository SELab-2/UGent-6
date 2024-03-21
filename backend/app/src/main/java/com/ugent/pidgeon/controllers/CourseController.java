package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.CourseMemberRequestJson;
import com.ugent.pidgeon.model.json.PublicUserDTO;
import com.ugent.pidgeon.model.json.UserIdJson;
import com.ugent.pidgeon.model.json.CourseJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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
    private UserRepository userRepository;

    @Autowired
    private GroupClusterRepository groupClusterRepository;

    @Autowired
    private ClusterController groupClusterController;


    @Autowired
    private CourseUserRepository courseUserRepository;


  @GetMapping(ApiRoutes.COURSE_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<String> getUserCourses(Auth auth){
        long userID = auth.getUserEntity().getId();
        try {
            List<UserRepository.CourseIdWithRelation> userCourses = userRepository.findCourseIdsByUserId(userID);

            // Retrieve course entities based on user courses
            List<CourseJSONObject> courseJSONObjects = userCourses.stream()
                    .map(courseWithRelation -> courseRepository.findById(courseWithRelation.getCourseId())
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .map(entity -> new CourseJSONObject(entity.getId(), entity.getName(), ApiRoutes.COURSE_BASE_PATH + "/" + entity.getId()))
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
    private record CourseJSONObject(long id, String name, String url){}

    @PostMapping(ApiRoutes.COURSE_BASE_PATH)
    @Roles({UserRole.teacher})
    public ResponseEntity<CourseEntity> createCourse(@RequestBody CourseJson courseJson, Auth auth){
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

            return ResponseEntity.ok(courseEntity);
        } catch (Exception e){
            Logger.getLogger("CourseController").severe("Error while creating course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateCourse(@RequestBody CourseJson courseJson, @PathVariable long courseId, Auth auth){
        try {
            UserEntity user = auth.getUserEntity();
            long userId = user.getId();

            // het vak selecteren
            CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
            if (courseEntity == null) {
                return ResponseEntity.notFound().build();
            }

            // check of de user admin of lesgever is van het vak
            Optional<CourseUserEntity> courseUserEntityOptional = courseUserRepository.findById(new CourseUserId(courseId, userId));
            if (courseUserEntityOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not part of the course");
            }
            CourseUserEntity courseUserEntity = courseUserEntityOptional.get();
            if(courseUserEntity.getRelation() == CourseRelation.enrolled){
               return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not allowed to update the course");
            }

            //update velden
            courseEntity.setName(courseJson.getName());
            courseEntity.setDescription(courseJson.getDescription());
            courseRepository.save(courseEntity);

            // Response verzenden
            return ResponseEntity.ok(courseEntity);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<CourseEntity> getCourseByCourseId(@PathVariable Long courseId) {
        Optional<CourseEntity> courseopt = courseRepository.findById(courseId);
        if (courseopt.isEmpty()) {
            return ResponseEntity.notFound().build(); // Or return an empty list, based on your preference
        }
        CourseEntity course = courseopt.get();
        return ResponseEntity.ok(course);
    }

    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<String> deleteCourse(@PathVariable long courseId, Auth auth){
        try {
            // Check of de user een admin of creator is van het vak
            UserEntity user = auth.getUserEntity();
            long userId = user.getId();

            // het vak selecteren
            CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
            if (courseEntity == null) {
                return ResponseEntity.notFound().build();
            }

            // check of de user admin of lesgever is van het vak
            CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, userId)).orElse(null);
            if (courseUserEntity == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not part of the course");
            }
            if(courseUserEntity.getRelation() != CourseRelation.creator){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not allowed to delete the course");
            }

            //voor elk project
            for(ProjectEntity project : courseRepository.findAllProjectsByCourseId(courseId)){
                projectController.deleteProjectById(project.getId(), auth);
            }

            //voor elke groepcluster
            for(GroupClusterEntity groupCluster : groupClusterRepository.findByCourseId(courseId)){
                // We verwijderen de groepfeedback niet omdat die al verwijderd werd wanneer het project verwijderd werd
                groupClusterController.deleteCluster(groupCluster.getId(), auth);
            }

            // Alle CourseUsers verwijderen
            courseUserRepository.deleteAll(courseUserRepository.findAllUsersByCourseId(courseId));

            //vak verwijderen
            courseRepository.delete(courseEntity);
            return ResponseEntity.ok("Vak verwijderd");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Probleem bij vak verwijderen: " + e.getMessage());
        }
    }

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/projects")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<List<ProjectEntity>> getProjectByCourseId(Auth auth, @PathVariable Long courseId) {
        List<ProjectEntity> projects = projectRepository.findByCourseId(courseId);
        if (projects.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(projects);
    }

    Boolean hasCourseRights(long courseId, UserEntity user){
        if(user.getRole() == UserRole.admin){
            return true;
        }else{
            return courseUserRepository.isCourseAdmin(courseId, user.getId());
        }
    }

    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/join")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> joinCourse(Auth auth, @PathVariable Long courseId) {
        if(courseRepository.existsById(courseId)){
            if(courseUserRepository.isCourseMember(courseId, auth.getUserEntity().getId())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already a member of the course");
            }
            courseUserRepository.save(new CourseUserEntity(courseId, auth.getUserEntity().getId(), CourseRelation.enrolled));
            return ResponseEntity.status(HttpStatus.CREATED).build(); // Successfully added
        }else{
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
        if(hasCourseRights(courseId, auth.getUserEntity())){
            Optional<CourseUserEntity> courseUserEntityOptional = courseUserRepository.findById(new CourseUserId(courseId, userId.getUserId()));
            if (courseUserEntityOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            CourseRelation userRelation = courseUserEntityOptional.get().getRelation();
            if (auth.getUserEntity().getId() == userId.getUserId()){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot remove yourself");
            }
            if(userRelation == CourseRelation.creator){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot remove course creator");
            } else if (userRelation == CourseRelation.course_admin){
                if(coursePermissions != CourseRelation.creator){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Course admin cannot remove a course admin");
                }else{
                    courseUserRepository.deleteById(new CourseUserId(courseId, userId.getUserId()));
                    return ResponseEntity.ok().build();
                }
            }
            courseUserRepository.deleteById(new CourseUserId(courseId, userId.getUserId()));
            return ResponseEntity.ok().build(); // Successfully removed
        }else{
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
        if(hasCourseRights(courseId, auth.getUserEntity())){
            if(request.getRelation() == CourseRelation.creator){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot add a creator");
            }
            boolean isAdmin = auth.getUserEntity().getRole() == UserRole.admin;
            boolean isCreator = courseUserRepository.getCourseRelation(courseId,auth.getUserEntity().getId()) == CourseRelation.creator;
            boolean creatingAdmin = request.getRelation() == CourseRelation.course_admin;
            if(creatingAdmin && (!isCreator  && !isAdmin)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only the creator can create more course-admins");
            }
            if (courseUserRepository.isCourseMember(courseId, request.getUserId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already a member of the course");
            }
            courseUserRepository.save(new CourseUserEntity(courseId, request.getUserId(), request.getRelation()));
            return ResponseEntity.status(HttpStatus.CREATED).build(); // Successfully added
        }else{
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
        if(hasCourseRights(courseId, auth.getUserEntity())){
            if(auth.getUserEntity().getId() == request.getUserId()){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot change your own role.");
            }

            if(request.getRelation() == CourseRelation.course_admin){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot change role to creator");
            }

            boolean isAdmin = auth.getUserEntity().getRole() == UserRole.admin;
            boolean isCreator = courseUserRepository.getCourseRelation(courseId,auth.getUserEntity().getId()) == CourseRelation.creator;
            boolean creatingAdmin = request.getRelation() == CourseRelation.course_admin;
            if(creatingAdmin && (!isCreator  && !isAdmin)){
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
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No acces to course");
        }
    }

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.student}) // student is allowed to see people in its class
    public ResponseEntity<?> getCourseMembers(Auth auth, @PathVariable Long courseId) {
        if(courseRepository.existsById(courseId)){
            UserEntity ue = auth.getUserEntity();
            if(ue.getRole() == UserRole.admin || courseUserRepository.isCourseMember(courseId, ue.getId())){
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
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No course with given id");
        }
    }


    /* Function to add a new project to an existing course */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/projects")
    @Roles({UserRole.teacher})
    public ResponseEntity<String> createProject(
            @PathVariable long courseId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam long groupClusterId,
            @RequestParam long testId,
            @RequestParam boolean projectType,
            @RequestParam Integer maxScore) {
        try {
            // Create a new ProjectEntity instance
            ProjectEntity project = new ProjectEntity(courseId, name, description, groupClusterId, testId, projectType, maxScore);

            // Save the project entity
            ProjectEntity savedProject = projectRepository.save(project);


            // Prepare response JSON
            Map<String, Object> response = createJSONPostResponse(savedProject);

            // Convert response map to JSON string
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonResponse = objectMapper.writeValueAsString(response);
            // Return success response with JSON string
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            System.out.println("Error while creating project: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating project: " + e.getMessage());
        }
    }

    /* Help function to create a JSON response when creating a new project */
    private static Map<String, Object> createJSONPostResponse(ProjectEntity savedProject) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedProject.getId());
        response.put("name", savedProject.getName());
        response.put("description", savedProject.getDescription());
        response.put("course", String.valueOf(savedProject.getCourseId()));
        response.put("deadline", 0); // Placeholder for deadline
            /* Optional timestamp
            if (savedProject.getTimestamp() != null) {
                response.put("timestamp", savedProject.getTimestamp().toString());
            }*/
        response.put("tests_url", ApiRoutes.PROJECT_BASE_PATH + "/" + savedProject.getId() + "/tests");
        response.put("submission_url", ApiRoutes.PROJECT_BASE_PATH + "/" + savedProject.getId() + "/sumbmissions");
        return response;
    }

}
