package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.*;

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
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Arrays.stream;


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
    @Autowired
    private GroupController groupController;
    @Autowired
    private GroupRepository groupRepository;

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
        UserEntity user = auth.getUserEntity();
        return projectRepository.findById(projectId)
                .map(project -> {
                    if (!accesToProject(projectId, auth.getUserEntity())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to view this project");
                    } else {
                        CourseEntity course = courseRepository.findById(project.getCourseId()).orElse(null);
                        if (course == null) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
                        }
                        return ResponseEntity.ok().body(projectEntityToProjectResponseJson(project, course, user));
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ProjectResponseJson projectEntityToProjectResponseJson(ProjectEntity project, CourseEntity course, UserEntity user) {
        // Calculate the progress of the project for all groups
        List<Long> groupIds = projectRepository.findGroupIdsByProjectId(project.getId());
        Integer total = groupIds.size();
        Integer completed = groupIds.stream().map(groupId -> {
            Long submissionId = submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(project.getId(), groupId);
            if (submissionId == null) {
                return 0;
            }
            SubmissionEntity submission = submissionRepository.findById(submissionId).orElse(null);
            if (submission == null) {
                return 0;
            }
            if (submission.getDockerAccepted() && submission.getStructureAccepted()) return 1;
            return 0;
        }).reduce(0, Integer::sum);

        // Get the submissonUrl, depends on if the user is a course_admin or enrolled
        String submissionUrl = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/submissions";
        CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(course.getId(), user.getId())).orElse(null);
        if (courseUserEntity == null) {
            return null;
        }
        if (courseUserEntity.getRelation() == CourseRelation.enrolled) {
            Long groupId = groupRepository.groupIdByProjectAndUser(project.getId(), user.getId());
            if (groupId == null) {
                return null;
            }
            submissionUrl += "/" + groupId;
        }

        return new ProjectResponseJson(
            new CourseReferenceJson(course.getName(), ApiRoutes.COURSE_BASE_PATH + "/" + course.getId(), course.getId()),
            project.getDeadline(),
            project.getDescription(),
            project.getId(),
            project.getName(),
            submissionUrl,
            ApiRoutes.TEST_BASE_PATH + "/" + project.getTestId(),
            project.getMaxScore(),
            project.isVisible(),
            new ProjectProgressJson(completed, total)
        );
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

            // Check of de dealine bestaat en in de toekomst ligt.
            OffsetDateTime deadline = projectJson.getDeadline();
            if(deadline == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No deadline given");
            }
            if(deadline.isBefore(OffsetDateTime.now())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Deadline is in the past");
            }

            // Create a new ProjectEntity instance
            ProjectEntity project = new ProjectEntity(courseId, projectJson.getName(), projectJson.getDescription(),
                    projectJson.getGroupClusterId(), null, projectJson.isVisible(),
                    projectJson.getMaxScore(), projectJson.getDeadline());

            // Save the project entity
            ProjectEntity savedProject = projectRepository.save(project);
            return ResponseEntity.ok(projectEntityToProjectResponseJson(savedProject, courseEntity, user));
        } catch (Exception e){
            Logger.getGlobal().severe("Error while creating project: " + Arrays.toString(e.getStackTrace()));
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

    /**
     * Function to get all groups of a project
     * @param projectId ID of the project to get the groups of
     * @ApiDog <a href="https://app.apidog.com/project/467959/apis/api-6343073">apiDog documentation</a>
     * @HttpMethod GET
     * @ApiPath /api/projects/{projectId}/groups
     * @return ResponseEntity with groups as specified in the apidog
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}/groups")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getGroupsOfProject(@PathVariable Long projectId, Auth auth) {
        // Check if the user is an admin of the project
        UserEntity user = auth.getUserEntity();
        if (!user.getRole().equals(UserRole.admin)) {
            if (!accesToProject(projectId, user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to view this project");
            }
        }

        List<Long> groups = projectRepository.findGroupIdsByProjectId(projectId);
        List<GroupJson> groupjsons = groups.stream()
                .map((Long id) -> {
                    GroupEntity group = groupRepository.findById(id).orElse(null);
                    return groupController.groupEntityToJson(group);
                }).toList();
        return ResponseEntity.ok(groupjsons);
    }

}
