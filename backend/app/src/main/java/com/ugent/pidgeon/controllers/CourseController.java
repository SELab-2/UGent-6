package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRepository testRepository;

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
    public ResponseEntity<List<ProjectEntity>> getProjectByCourseId(@PathVariable Long courseId) {
        List<ProjectEntity> projects = projectRepository.findByCourseId(courseId);
        if (projects.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(projects);
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

            // Return success response with project ID
            return ResponseEntity.ok("Project created with ID: " + savedProject.getId());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating project: " + e.getMessage());
        }
    }

}
