package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.DeadlineEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class JpaProjectController {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRepository testRepository;

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
