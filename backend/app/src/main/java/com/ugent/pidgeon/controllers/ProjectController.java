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

    /**
     * Function to get all projects of a user
     * @param auth authentication object of the requesting user
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883808">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects
     * @return ResponseEntity with a list of projects
     */
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
    /**
     * Function to get a project by its ID
     * @param projectId ID of the project to get
     * @param auth authentication object of the requesting user
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723844">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectId}
     * @return ResponseEntity with the project
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getProjectById(@PathVariable Long projectId, Auth auth) {
        return projectRepository.findById(projectId)
                .map(project -> {
                    long userId = auth.getUserEntity().getId();
                    if (!accesToProject(projectId, auth.getUserEntity())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to view this project");
                    } else {
                        return ResponseEntity.ok().body(project);
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Function to update an existing project
     * @param projectId ID of the project to get
     * @param updateDTO ProjectUpdateDTO object containing the new project's information
     * @param auth authentication object of the requesting user
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723887">apiDog documentation</a>
     * @HttpMethod Put
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectId}
     * @return ResponseEntity with the created project
     */
    @PutMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> putProjectById(@PathVariable Long projectId, @RequestBody ProjectUpdateDTO updateDTO, Auth auth) {
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isPresent()) {
            if (!projectRepository.adminOfProject(projectId, auth.getUserEntity().getId()) && !auth.getUserEntity().getRole().equals(UserRole.admin)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this project");
            }
            ProjectEntity project = projectOptional.get();
            if (updateDTO.getName() != null) project.setName(updateDTO.getName());
            if (updateDTO.getDescription() != null) project.setDescription(updateDTO.getDescription());

            if (updateDTO.getDeadline() != null) {
                project.setDeadline(updateDTO.getDeadline());
            }
            projectRepository.save(project);
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("project not found with id " + projectId);
        }
    }

    /**
     * Function to delete a project by its ID
     * @param projectId ID of the project to delete
     * @param auth authentication object of the requesting user
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723898">apiDog documentation</a>
     * @HttpMethod DELETE
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectId}
     * @return ResponseEntity with the deleted project
     */
    @DeleteMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> deleteProjectById(@PathVariable long projectId, Auth auth) {
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()) {
            if (!projectRepository.adminOfProject(projectId, auth.getUserEntity().getId()) && !auth.getUserEntity().getRole().equals(UserRole.admin)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this project");
            }

            ProjectEntity projectEntity = projectOptional.get();


            groupFeedbackRepository.deleteAll(groupFeedbackRepository.findByProjectId(projectId));

            for (SubmissionEntity submissionEntity : submissionRepository.findByProjectId(projectId)) {
                filesubmissiontestController.deleteSubmissionById(submissionEntity.getId(), auth);
            }

            projectRepository.delete(projectEntity);

            testController.deleteTestById(projectEntity.getTestId(),auth);



            return ResponseEntity.ok().build();

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("project not found with id " + projectId);
        }


    }
}
