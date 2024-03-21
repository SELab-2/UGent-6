package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.controllers.TestController;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.ProjectUpdateDTO;

import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
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
    private TestRepository testRepository;

    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;

    @Autowired
    private SubmissionController filesubmissiontestController;
    @Autowired
    private TestController testController;


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

    public boolean accesToProject(long projectId, UserEntity user) {
        boolean studentof = projectRepository.userPartOfProject(projectId, user.getId());
        boolean isAdmin = (user.getRole() == UserRole.admin) || (projectRepository.adminOfProject(projectId, user.getId()));
        return  studentof || isAdmin;
    }
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getProjectById(@PathVariable Long projectId, Auth auth) {
        return projectRepository.findById(projectId)
                .map(project -> {
                    long userId = auth.getUserEntity().getId();
                    if (!accesToProject(projectId, auth.getUserEntity())) {
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
                project.setDeadline(updateDTO.getDeadline());
            }
            System.out.println(project.getName());
            projectRepository.save(project);
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> deleteProjectById(@PathVariable long projectId, Auth auth) {
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()) {


            ProjectEntity projectEntity = projectOptional.get();


            groupFeedbackRepository.deleteAll(groupFeedbackRepository.findByProjectId(projectId));

            for (SubmissionEntity submissionEntity : submissionRepository.findByProjectId(projectId)) {
                filesubmissiontestController.deleteSubmissionById(submissionEntity.getId(), auth);
            }

            projectRepository.delete(projectEntity);

            testController.deleteTestById(projectEntity.getTestId(),auth);



            return ResponseEntity.ok().build();

        } else {
            return ResponseEntity.notFound().build();
        }


    }
}
