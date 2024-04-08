package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.*;

import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.ClusterUtil;
import com.ugent.pidgeon.util.CourseUtil;
import com.ugent.pidgeon.util.ProjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Logger;



@RestController
public class ProjectController {

    //repos
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;
    @Autowired
    private CourseRepository courseRepository;
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


    //util
    @Autowired
    private ProjectUtil projectUtil;
    @Autowired
    private ClusterUtil clusterUtil;
    @Autowired
    private CourseUtil courseUtil;

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
        CheckResult<ProjectEntity> checkResult = projectUtil.canGetProject(projectId, user);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        ProjectEntity project = checkResult.getData();

        CheckResult<CourseEntity> courceCheck = courseUtil.getCourseIfExists(project.getCourseId());
        if (courceCheck.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(courceCheck.getStatus()).body(courceCheck.getMessage());
        }
        CourseEntity course = courceCheck.getData();

        return ResponseEntity.ok().body(projectUtil.projectEntityToProjectResponseJson(project, course, user));
    }

    /**
     * Function to create a new project
     * @param courseId ID of the course to create the project in
     * @param projectJson ProjectJson object containing the new project's information
     * @param auth authentication object of the requesting user
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723877">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/projects
     * @return ResponseEntity with the created project
     */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/projects")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> createProject(
            @PathVariable long courseId, @RequestBody ProjectJson projectJson, Auth auth) {
        try {
            // De user vinden
            UserEntity user = auth.getUserEntity();
            CheckResult<CourseEntity> checkAcces = courseUtil.checkCourseAcces(courseId, user);
            if (checkAcces.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkAcces.getStatus()).body(checkAcces.getMessage());
            }

            if (projectJson.getGroupClusterId() == null) {
                GroupClusterEntity groupCluster = groupClusterRepository.findIndividualClusterByCourseId(courseId).orElse(null);
                if (groupCluster == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error while creating project without group, contact an administrator");
                }
                projectJson.setGroupClusterId(groupCluster.getId());
            }

            CheckResult<Void> checkResult = projectUtil.checkProjectJson(projectJson, courseId);
            if (checkResult.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }

            // Create a new ProjectEntity instance
            ProjectEntity project = new ProjectEntity(courseId, projectJson.getName(), projectJson.getDescription(),
                    projectJson.getGroupClusterId(), null, projectJson.isVisible(),
                    projectJson.getMaxScore(), projectJson.getDeadline());

            // Save the project entity
            ProjectEntity savedProject = projectRepository.save(project);
            CourseEntity courseEntity = checkAcces.getData();
            return ResponseEntity.ok(projectUtil.projectEntityToProjectResponseJson(savedProject, courseEntity, user));
        } catch (Exception e){
            Logger.getGlobal().severe("Error while creating project: " + Arrays.toString(e.getStackTrace()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating project: " + e.getMessage());
        }
    }



    private ResponseEntity<?> doProjectUpdate(ProjectEntity project, ProjectJson projectJson, UserEntity user) {
        project.setName(projectJson.getName());
        project.setDescription(projectJson.getDescription());
        project.setGroupClusterId(projectJson.getGroupClusterId());
        project.setDeadline(projectJson.getDeadline());
        project.setMaxScore(projectJson.getMaxScore());
        project.setVisible(projectJson.isVisible());
        projectRepository.save(project);
        return ResponseEntity.ok(projectUtil.projectEntityToProjectResponseJson(project, courseRepository.findById(project.getCourseId()).get(), user));
    }

     /**
     * Function to update an existing project
     * @param projectId ID of the project to get
     * @param projectJson ProjectUpdateDTO object containing the new project's information
     * @param auth authentication object of the requesting user
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723887">apiDog documentation</a>
     * @HttpMethod Put
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectId}
     * @return ResponseEntity with the created project
     */
    @PutMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> putProjectById(@PathVariable Long projectId, @RequestBody ProjectJson projectJson, Auth auth) {
        CheckResult<ProjectEntity> checkResult = projectUtil.getProjectIfAdmin(projectId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        ProjectEntity project = checkResult.getData();

        if (projectJson.getGroupClusterId() == null) {
            GroupClusterEntity groupCluster = groupClusterRepository.findIndividualClusterByCourseId(project.getCourseId()).orElse(null);
            if (groupCluster == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error while updating project without group, contact an administrator");
            }
            projectJson.setGroupClusterId(groupCluster.getId());
        }

        CheckResult<Void> checkProject = projectUtil.checkProjectJson(projectJson, project.getCourseId());
        if (checkProject.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkProject.getStatus()).body(checkProject.getMessage());
        }

        return doProjectUpdate(project, projectJson, auth.getUserEntity());
    }

    /**
     * Function to update an existing project
     * @param projectId ID of the project to get
     * @param projectJson ProjectUpdateDTO object containing the new project's information
     * @param auth authentication object of the requesting user
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723887">apiDog documentation</a>
     * @HttpMethod Patch
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectId}
     * @return ResponseEntity with the created project
     */
    @PatchMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> patchProjectById(@PathVariable Long projectId, @RequestBody ProjectJson projectJson, Auth auth) {
        CheckResult<ProjectEntity> checkResult = projectUtil.getProjectIfAdmin(projectId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        ProjectEntity project = checkResult.getData();

        if (projectJson.getName() == null) {
            projectJson.setName(project.getName());
        }
        if (projectJson.getDescription() == null) {
            projectJson.setDescription(project.getDescription());
        }
        if (projectJson.getGroupClusterId() == null) {
            projectJson.setGroupClusterId(project.getGroupClusterId());
        }
        if (projectJson.getDeadline() == null) {
            projectJson.setDeadline(project.getDeadline());
        }
        if (projectJson.getMaxScore() == null) {
            projectJson.setMaxScore(project.getMaxScore());
        }
        if (projectJson.isVisible() == null) {
            projectJson.setVisible(project.isVisible());
        }

        CheckResult<Void> checkProject = projectUtil.checkProjectJson(projectJson, project.getCourseId());
        if (checkProject.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkProject.getStatus()).body(checkProject.getMessage());
        }

        return doProjectUpdate(project, projectJson, auth.getUserEntity());
    }

    /**
     * Function to delete a project by its ID
     * @param projectId ID of the project to delete
     * @param auth authentication object of the requesting user
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723898">apiDog documentation</a>
     * @HttpMethod DELETE
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectId}
     * @return ResponseEntity with the status, no content
     */
    @DeleteMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectId}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> deleteProjectById(@PathVariable long projectId, Auth auth) {
            CheckResult<ProjectEntity> projectCheck = projectUtil.getProjectIfAdmin(projectId, auth.getUserEntity());
            if (projectCheck.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(projectCheck.getStatus()).body(projectCheck.getMessage());
            }
            ProjectEntity projectEntity = projectCheck.getData();


            groupFeedbackRepository.deleteAll(groupFeedbackRepository.findByProjectId(projectId));

            for (SubmissionEntity submissionEntity : submissionRepository.findByProjectId(projectId)) {
                filesubmissiontestController.deleteSubmissionById(submissionEntity.getId(), auth);
            }

            projectRepository.delete(projectEntity);

            testController.deleteTestById(projectEntity.getTestId(), auth);

            return ResponseEntity.ok().build();
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
        CheckResult<ProjectEntity> projectCheck = projectUtil.getProjectIfAdmin(projectId, auth.getUserEntity());
        if (projectCheck.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(projectCheck.getStatus()).body(projectCheck.getMessage());
        }
        ProjectEntity project = projectCheck.getData();

        if (clusterUtil.isIndividualCluster(project.getGroupClusterId())) {
            String memberUrl = ApiRoutes.COURSE_BASE_PATH + "/" + project.getCourseId() + "/members";
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No groups for this project: use " + memberUrl + " to get the members of the course");
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
