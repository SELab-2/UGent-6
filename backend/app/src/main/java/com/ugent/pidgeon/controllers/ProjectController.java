package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private GroupFeedbackRepository groupFeedbackRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseUserRepository courseUserRepository;
    @Autowired
    private GroupClusterRepository groupClusterRepository;
    @Autowired
    private TestRepository testRepository;

    //controllers
    @Autowired
    private DeadlineController deadlineController;
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
                return ResponseEntity.notFound().build();
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
            if(! testRepository.existsById(projectJson.getTestId())){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No test with this id exists");
            }
            // TODO: checken of deadline bestaat en ze dan ook in het project steken

            // Create a new ProjectEntity instance
            ProjectEntity project = new ProjectEntity(courseId, projectJson.getName(), projectJson.getDescription(),
                    projectJson.getGroupClusterId(), projectJson.getTestId(), projectJson.isVisible(),
                    projectJson.getMaxScore());

            // Save the project entity
            ProjectEntity savedProject = projectRepository.save(project);

            return ResponseEntity.ok(savedProject);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating project: " + e.getMessage());
        }
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


    @DeleteMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> deleteProjectById(@PathVariable long projectId, Auth auth) {
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()) {


            ProjectEntity projectEntity = projectOptional.get();
            // delete all the deadlines associated with the project
            for (DeadlineEntity deadlineEntity : projectEntity.getDeadlines()) {
                deadlineController.deleteDeadlineById(deadlineEntity.getDeadlineId(), auth);
            }

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
