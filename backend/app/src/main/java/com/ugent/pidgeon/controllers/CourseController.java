package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.CourseMemberRequestJson;
import com.ugent.pidgeon.model.json.UserIdJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private CourseUserRepository courseUserRepository;

    @Autowired
    private UserRepository userRepository;

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
    public ResponseEntity<?> joinCourse(Auth auth, @PathVariable Long courseId) {
        if(courseRepository.existsById(courseId)){
            courseUserRepository.save(new CourseUserEntity(courseId, auth.getUserEntity().getId(), CourseRelation.enrolled));
            return ResponseEntity.status(HttpStatus.CREATED).build(); // Successfully added
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No course with given id");
        }
    }

    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.admin})
    public ResponseEntity<?> removeCourseMember(Auth auth, @PathVariable Long courseId, @RequestBody UserIdJson userId) {
        CourseRelation coursePermissions = courseUserRepository.getCourseRelation(courseId, auth.getUserEntity().getId());
        if(hasCourseRights(courseId, auth.getUserEntity())){
            CourseRelation userRelation = courseUserRepository.getReferenceById(new CourseUserId(courseId, userId.getUserId())).getRelation();
            if(userRelation == CourseRelation.creator){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot remove course creator");
            }else if(userRelation == CourseRelation.course_admin){
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.admin})
    public ResponseEntity<?> addCourseMember(Auth auth, @PathVariable Long courseId, @RequestBody CourseMemberRequestJson request) {
        if(request.getRelation() == CourseRelation.course_admin){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot add a creator");
        }
        // Only teacher and admin can add different users to a course.
        if(hasCourseRights(courseId, auth.getUserEntity())){
            if(auth.getUserEntity().getRole() != UserRole.admin && request.getRelation() != CourseRelation.enrolled && courseUserRepository.getCourseRelation(courseId,auth.getUserEntity().getId()) != CourseRelation.creator){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only the creator can create more course-admins");
            }
            courseUserRepository.save(new CourseUserEntity(courseId, request.getUserId(), request.getRelation()));
            return ResponseEntity.status(HttpStatus.CREATED).build(); // Successfully added
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PatchMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.admin})
    public ResponseEntity<?> updateCourseMember(Auth auth, @PathVariable Long courseId, @RequestBody CourseMemberRequestJson request) {
        if(auth.getUserEntity().getId() == request.getUserId()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot change your own role.");
        }

        if(request.getRelation() == CourseRelation.course_admin){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot change role to creator");
        }
        // Only teacher and admin can add different users to a course.
        if(hasCourseRights(courseId, auth.getUserEntity())){
            if(auth.getUserEntity().getRole() != UserRole.admin && request.getRelation() != CourseRelation.enrolled && courseUserRepository.getCourseRelation(courseId,auth.getUserEntity().getId()) != CourseRelation.creator){
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.student}) // student is allowed to see people in its class
    public ResponseEntity<?> getCourseMembers(Auth auth, @PathVariable Long courseId) {
        if(courseRepository.existsById(courseId)){
            UserEntity ue = auth.getUserEntity();
            if(ue.getRole() == UserRole.admin || courseUserRepository.isCourseMember(courseId, ue.getId())){
                List<CourseUserEntity> members = courseUserRepository.findAllMembers(courseId);
                List<Map<String, Object>> memberJson = members.stream().map(cue -> {
                    Map<String, Object> json = new HashMap<>();
                    // Assuming `CourseUserEntity` has some properties like `id`, `name`, etc.
                    json.put("userId", ApiRoutes.USER_BASE_PATH + "/" + cue.getUserId());
                    json.put("relation", cue.getRelation());
                    UserEntity user = userRepository.getReferenceById(cue.getUserId());
                    json.put("name", user.getName()); // should exist since relation still exists
                    json.put("surname", user.getSurname());
                    // Add more properties as needed
                    return json;
                }).toList();
                Map<String, Object> resultJson = new HashMap<>();
                resultJson.put("members", memberJson);
                resultJson.put("courses", ApiRoutes.COURSE_BASE_PATH + "/" + courseId);
                return ResponseEntity.ok(resultJson); // Successfully retrieved members
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not a member of the course");
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No course with given id");
        }
    }


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
