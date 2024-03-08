package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.DeadlineEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRepository testRepository;

    @GetMapping("/api/projects/{projectId}")
    public ResponseEntity<ProjectEntity> getProjectById(@PathVariable Long projectId) {
        return projectRepository.findById(projectId)
                .map(project -> ResponseEntity.ok().body(project)) // Return the project as JSON
                .orElseGet(() -> ResponseEntity.notFound().build()); // Or return 404 if not found
    }

    @GetMapping("/api/projects/{courseId}")
    public ResponseEntity<List<ProjectEntity>> getProjectByCourseId(@PathVariable Long courseId) {
        List<ProjectEntity> projects = projectRepository.findByCourseId(courseId);
        if (projects.isEmpty()) {
            return ResponseEntity.notFound().build(); // Or return an empty list, based on your preference
        }
        return ResponseEntity.ok(projects);
    }



    @GetMapping("/api/projects")
    public List<String> getProjects() {
        List<String> res = new ArrayList<>();
        for (ProjectEntity project : projectRepository.findAll()) {
            StringBuilder projectString = new StringBuilder(project.getName());
            Optional<TestEntity> test = testRepository.findById(project.getId());
            test.ifPresent(testEntity -> projectString.append(" with test: ").append(testEntity.getId()));
            projectString.append(" with deadlines: ");
            for (DeadlineEntity deadline : project.getDeadlines()) {
                projectString.append(deadline.getDeadline());
            }
            res.add(projectString.toString());
        }
        return res;
    }

}
