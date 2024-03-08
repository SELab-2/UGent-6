package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourseController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRepository testRepository;

    @GetMapping("/api/courses/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<List<ProjectEntity>> getProjectByCourseId(@PathVariable Long courseId) {
        List<ProjectEntity> projects = projectRepository.findByCourseId(courseId);
        if (projects.isEmpty()) {
            return ResponseEntity.notFound().build(); // Or return an empty list, based on your preference
        }
        return ResponseEntity.ok(projects);
    }

}
