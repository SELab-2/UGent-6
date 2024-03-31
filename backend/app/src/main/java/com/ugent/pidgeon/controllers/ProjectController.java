package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.ProjectJson;
import com.ugent.pidgeon.model.json.ProjectUpdateDTO;

import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;


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
    private CourseRepository courseRepository;
    @Autowired
    private CourseUserRepository courseUserRepository;
    @Autowired
    private GroupClusterRepository groupClusterRepository;

    //controllers
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
                    if (!accesToProject(projectId, auth.getUserEntity())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to view this project");
                    } else {
                        return ResponseEntity.ok().body(project);
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* Function to add a new project to an existing course */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/projects")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> createProject(
            @PathVariable long courseId, @RequestBody ProjectJson projectJson, Auth auth) {
        try {
            // De user vinden
            UserEntity user = auth.getUserEntity();
            long userId = user.getId();

            // het vak selecteren
            CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
            if (courseEntity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
            }

            // check of de user admin of lesgever is van het vak
            CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, userId)).
                    orElse(null);
            if (courseUserEntity == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not part of the course");
            }
            if(courseUserEntity.getRelation() == CourseRelation.enrolled){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not allowed to create new projects");
            }

            // Check of de GroupCluster deel is van het vak
            GroupClusterEntity groupCluster = groupClusterRepository.findById(projectJson.getGroupClusterId()).orElse(null);
            if(groupCluster == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group cluster does not exist");
            }
            if(groupCluster.getCourseId() != courseId){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Group cluster isn't linked to this course");
            }

            // Check of de test bestaat
            if(projectJson.getTestId() != null && !testRepository.existsById(projectJson.getTestId())){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No test with this id exists");
            }

            // Check of de dealine bestaat en in de toekomst ligt.
            Timestamp deadline = projectJson.getDeadline();
            if(deadline == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No deadline given");
            }
            if(deadline.before(Timestamp.valueOf(LocalDateTime.now()))){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Deadline is in the past");
            }

            // Create a new ProjectEntity instance
            ProjectEntity project = new ProjectEntity(courseId, projectJson.getName(), projectJson.getDescription(),
                    projectJson.getGroupClusterId(), projectJson.getTestId(), projectJson.isVisible(),
                    projectJson.getMaxScore(), projectJson.getDeadline());

            // Save the project entity
            ProjectEntity savedProject = projectRepository.save(project);

            return ResponseEntity.ok(savedProject);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating project: " + e.getMessage());
        }
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
