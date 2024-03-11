package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.ProjectUpdateDTO;
import com.ugent.pidgeon.postgre.models.DeadlineEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.DeadlineRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
public class ProjectController {
    //repos
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private DeadlineRepository deadlineRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private TestRepository testRepository;

    //controllers
    @Autowired
    private DeadlineController deadlineController;
    @Autowired
    private FilesubmissiontestController filesubmissiontestController;


    @GetMapping(ApiRoutes.PROJECT_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getProjects(Auth auth) {
        long userid = auth.getUserEntity().getId();
        List<ProjectEntity> allProjects = projectRepository.findProjectsByUserId(userid);
        List<Map<String, String>> projectsWithUrls = new ArrayList<>();

        for (ProjectEntity project : allProjects) {
            Map<String, String> projectInfo = new HashMap<>();
            projectInfo.put("name", project.getName());
            projectInfo.put("url", "/api/projects/" + project.getId());
            projectsWithUrls.add(projectInfo);
        }

        return ResponseEntity.ok().body(projectsWithUrls);
    }

    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getProjectById(@PathVariable Long projectId, Auth auth) {
        return projectRepository.findById(projectId)
                .map(project -> {
                    long userId = auth.getUserEntity().getId();
                    if (!projectRepository.userPartOfProject(projectId, userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    } else {
                        return ResponseEntity.ok().body(project);
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> putProjectById(@PathVariable Long projectId, @RequestBody ProjectUpdateDTO updateDTO, Auth auth) {
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isPresent()) {
            ProjectEntity project = projectOptional.get();
            if (updateDTO.getName() != null) project.setName(updateDTO.getName());
            if (updateDTO.getDescription() != null) project.setDescription(updateDTO.getDescription());

            if (updateDTO.getDeadline() != null) {
                DeadlineEntity deadlineEntity = new DeadlineEntity(projectId, updateDTO.getDeadline());
                deadlineRepository.save(deadlineEntity);
            }
            System.out.println(project.getName());
            projectRepository.save(project);
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId")
    @Roles({UserRole.teacher, UserRole.admin})
    public ResponseEntity<?> deleteProjectById(@PathVariable long projectId, Auth auth){
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()){
            ProjectEntity projectEntity = projectOptional.get();
            //TODO: also remove submissions
            for(SubmissionEntity submissionEntity:  submissionRepository.findByProjectId(projectId) ){
                filesubmissiontestController.deleteSubmissionById(submissionEntity.getId(),auth);
            }
            // delete all the deadlines associated with the project
            for(DeadlineEntity deadlineEntity: projectEntity.getDeadlines()){
                deadlineController.deleteDeadlineById(deadlineEntity.getDeadlineId(), auth);
            }
            // delete the project after all its dependant children are deleted
            projectRepository.delete(projectEntity);
            return ResponseEntity.ok(projectEntity);

        }else {
            return ResponseEntity.notFound().build();
        }


    }
}
