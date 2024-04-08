package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.*;

import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.CheckResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
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
    @Autowired
    private GroupController groupController;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private ClusterController clusterController;

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
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found"));
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


    private CheckResult checkProjectJson(ProjectJson projectJson, long courseId) {
        if (projectJson.getName() == null ||
            projectJson.getDescription() == null ||
            projectJson.getMaxScore() == null ||
            projectJson.getGroupClusterId() == null ||
            projectJson.getDeadline() == null) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "name, description, maxScore and deadline are required fields", null);
        }

        if (projectJson.getName().isBlank()) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "name cannot be empty", null);
        }

        // Check of de GroupCluster deel is van het vak
        GroupClusterEntity groupCluster = groupClusterRepository.findById(projectJson.getGroupClusterId()).orElse(null);
        if (groupCluster == null) {
            return new CheckResult(HttpStatus.NOT_FOUND, "Group cluster does not exist", null);
        }
        if (groupCluster.getCourseId() != courseId) {
            return new CheckResult(HttpStatus.FORBIDDEN, "Group cluster isn't linked to this course", null);
        }

        if (projectJson.getDeadline().isBefore(OffsetDateTime.now())) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "Deadline is in the past", null);
        }

        if (projectJson.getMaxScore() < 0) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "Max score cannot be negative", null);
        }

        return new CheckResult(HttpStatus.OK, "", null);
    }

    private CheckResult checkCourseAcces(long courseId, UserEntity user) {
        long userId = user.getId();

        // het vak selecteren
        CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
        if (courseEntity == null) {
            return new CheckResult(HttpStatus.NOT_FOUND, "Course not found", null);
        }

        // check of de user admin of lesgever is van het vak
        CourseUserEntity courseUserEntity = courseUserRepository.findById(new CourseUserId(courseId, userId)).
                orElse(null);
        if (courseUserEntity == null) {
            return new CheckResult(HttpStatus.FORBIDDEN, "User is not part of the course", null);
        }
        if(courseUserEntity.getRelation() == CourseRelation.enrolled){
            return new CheckResult(HttpStatus.FORBIDDEN, "User is not an admin of the course", null);
        }
        return new CheckResult(HttpStatus.OK, "", null);
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
            CheckResult checkAcces = checkCourseAcces(courseId, user);
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

            CheckResult checkResult = checkProjectJson(projectJson, courseId);
            if (checkResult.getStatus() != HttpStatus.OK) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }

            // Create a new ProjectEntity instance
            ProjectEntity project = new ProjectEntity(courseId, projectJson.getName(), projectJson.getDescription(),
                    projectJson.getGroupClusterId(), null, projectJson.isVisible(),
                    projectJson.getMaxScore(), projectJson.getDeadline());

            // Save the project entity
            ProjectEntity savedProject = projectRepository.save(project);
            CourseEntity courseEntity = courseRepository.findById(courseId).get();
            return ResponseEntity.ok(projectEntityToProjectResponseJson(savedProject, courseEntity, user));
        } catch (Exception e){
            Logger.getGlobal().severe("Error while creating project: " + Arrays.toString(e.getStackTrace()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating project: " + e.getMessage());
        }
    }

    private CheckResult checkProjectUpdateAcces(long projectId, UserEntity user) {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return new CheckResult(HttpStatus.NOT_FOUND, "Project not found", null);
        }
        return checkCourseAcces(project.getCourseId(), user);
    }

    private ResponseEntity<?> doProjectUpdate(ProjectEntity project, ProjectJson projectJson, UserEntity user) {
        project.setName(projectJson.getName());
        project.setDescription(projectJson.getDescription());
        project.setGroupClusterId(projectJson.getGroupClusterId());
        project.setDeadline(projectJson.getDeadline());
        project.setMaxScore(projectJson.getMaxScore());
        project.setVisible(projectJson.isVisible());
        projectRepository.save(project);
        return ResponseEntity.ok(projectEntityToProjectResponseJson(project, courseRepository.findById(project.getCourseId()).get(), user));
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
        CheckResult checkResult = checkProjectUpdateAcces(projectId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        ProjectEntity project = projectRepository.findById(projectId).get();

        if (projectJson.getGroupClusterId() == null) {
            GroupClusterEntity groupCluster = groupClusterRepository.findIndividualClusterByCourseId(project.getCourseId()).orElse(null);
            if (groupCluster == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error while updating project without group, contact an administrator");
            }
            projectJson.setGroupClusterId(groupCluster.getId());
        }

        CheckResult checkProject = checkProjectJson(projectJson, project.getCourseId());
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
        CheckResult checkResult = checkProjectUpdateAcces(projectId, auth.getUserEntity());
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        ProjectEntity project = projectRepository.findById(projectId).get();

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

        CheckResult checkProject = checkProjectJson(projectJson, project.getCourseId());
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
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }

        if (clusterController.isIndividualCluster(project.getGroupClusterId())) {
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
