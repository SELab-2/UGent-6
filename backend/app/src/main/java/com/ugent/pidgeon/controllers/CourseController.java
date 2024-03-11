package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.Filehandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupClusterRepository groupClusterRepository;

    @Autowired
    private DeadlineRepository deadlineRepository;

    @GetMapping(ApiRoutes.COURSE_BASE_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<String> getUserCourses(Auth auth){
        long userID = auth.getUserEntity().getId();
        try {
            List<UserRepository.CourseIdWithRelation> userCourses = userRepository.findCourseIdsByUserId(userID);

            // Retrieve course entities based on user courses
            List<CourseJSONObject> courseJSONObjects = userCourses.stream()
                    .map(courseWithRelation -> courseRepository.findById(courseWithRelation.getCourseId())
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .map(entity -> new CourseJSONObject(entity.getId(), entity.getName(), ApiRoutes.COURSE_BASE_PATH + "/" + entity.getId()))
                    .toList();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(courseJSONObjects);


            // Return the JSON string in ResponseEntity
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve courses");
        }
    }

    // Hulpobject voor de getmapping mooi in JSON te kunnen zetten.
    private record CourseJSONObject(long id, String name, String url){}

    @PostMapping(ApiRoutes.COURSE_BASE_PATH)
    @Roles({UserRole.teacher})
    public ResponseEntity<String> createCourse(@RequestParam("name") String name, @RequestParam("description") String description, @RequestParam("assistantIds") List<Long> assistantIds , Auth auth){
        try {
            UserEntity user = auth.getUserEntity();
            long userId = auth.getUserEntity().getId();

            // nieuw vak aanmaken
            CourseEntity courseEntity = new CourseEntity(name, description);
            // Set teacher details
            SimplePersonJSONObject leerkracht = new SimplePersonJSONObject(user.getName(), user.getSurname(), ApiRoutes.USER_BASE_PATH + "/" + userId);
            // Set assistant details
            List<SimplePersonJSONObject> assistenten = assistantIds.stream().map(id -> userRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull).map(entity -> new SimplePersonJSONObject(
                            entity.getName(), entity.getSurname(), ApiRoutes.USER_BASE_PATH + "/" + entity.getId()))
                                .toList();

            // Save the course entity
            CourseEntity savedCourse = courseRepository.save(courseEntity);

            // assistenten toevoegen aan vak
//            for(long id: assistantIds){
//                Optional<UserEntity> assistentOpt = userRepository.findById(id);
//                if(assistentOpt.isPresent()){
//                    // Assistent toevoegen aan vak
//                }
//            }


            // Construct the response JSON string
            StringBuilder assistantsBuilder = new StringBuilder();
            for (SimplePersonJSONObject assistant : assistenten) {
                assistantsBuilder.append("{\"name\":\"").append(assistant.name()).append("\",\"surname\":\"").append(assistant.surname()).append("\",\"url\":\"").append(assistant.url()).append("\"},");
            }
            if (!assistantsBuilder.isEmpty()) {
                assistantsBuilder.setLength(assistantsBuilder.length() - 1); // Remove the trailing comma
            }

            StringBuilder jsonResponseBuilder = new StringBuilder();
            jsonResponseBuilder.append("{\"id\":\"").append(savedCourse.getId()).append("\",\"name\":\"").append(name).append("\",\"description\":\"").append(description)
                    .append("\",\"teacher\":{\"name\":\"").append(leerkracht.name()).append("\",\"surname\":\"").append(leerkracht.surname()).append("\",\"url\":\"").append(leerkracht.url()).append("\"},\"assistants\":[")
                    .append(assistantsBuilder).append("],\"members_url\":\"your_members_url\"}");

            String jsonResponse = jsonResponseBuilder.toString();
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating course: " + e.getMessage());
        }
    }

    @PutMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher})
    public ResponseEntity<String> updateCourse(@RequestParam("name") String name, @RequestParam("description") String description, @RequestParam("assistantIds") List<Long> assistantIds, @PathVariable long courseId, Auth auth){
        try {
            UserEntity user = auth.getUserEntity();
            long userId = auth.getUserEntity().getId();

            // het vak selecteren
            CourseEntity courseEntity = courseRepository.findById(courseId).orElseThrow();
            // Set teacher details
            SimplePersonJSONObject leerkracht = new SimplePersonJSONObject(user.getName(), user.getSurname(), ApiRoutes.USER_BASE_PATH + "/" + userId);
            // Set assistant details
            List<SimplePersonJSONObject> assistenten = assistantIds.stream().map(id -> userRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull).map(entity -> new SimplePersonJSONObject(
                            entity.getName(), entity.getSurname(), ApiRoutes.USER_BASE_PATH + "/" + entity.getId()))
                    .toList();

            //update velden
            courseEntity.setName(name);
            courseEntity.setDescription(description);
            courseRepository.save(courseEntity);

            // assistenten toevoegen aan vak
//            for(long id: assistantIds){
//                Optional<UserEntity> assistentOpt = userRepository.findById(id);
//                if(assistentOpt.isPresent()){
//                    // Assistent toevoegen aan vak
//                }
//            }


            // Construct the response JSON string
            StringBuilder assistantsBuilder = new StringBuilder();
            for (SimplePersonJSONObject assistant : assistenten) {
                assistantsBuilder.append("{\"name\":\"").append(assistant.name()).append("\",\"surname\":\"").append(assistant.surname()).append("\",\"url\":\"").append(assistant.url()).append("\"},");
            }
            if (!assistantsBuilder.isEmpty()) {
                assistantsBuilder.setLength(assistantsBuilder.length() - 1); // Remove the trailing comma
            }

            StringBuilder jsonResponseBuilder = new StringBuilder();
            jsonResponseBuilder.append("{\"id\":\"").append(courseEntity.getId()).append("\",\"name\":\"").append(name).append("\",\"description\":\"").append(description)
                    .append("\",\"teacher\":{\"name\":\"").append(leerkracht.name()).append("\",\"surname\":\"").append(leerkracht.surname()).append("\",\"url\":\"").append(leerkracht.url()).append("\"},\"assistants\":[")
                    .append(assistantsBuilder).append("],\"members_url\":\"your_members_url\"}");

            String jsonResponse = jsonResponseBuilder.toString();
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating course: " + e.getMessage());
        }
    }
    private record SimplePersonJSONObject(String name, String surname, String url){}

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<CourseEntity> getCourseByCourseId(@PathVariable Long courseId) {
        Optional<CourseEntity> courseopt = courseRepository.findById(courseId);
        if (courseopt.isEmpty()) {
            return ResponseEntity.notFound().build(); // Or return an empty list, based on your preference
        }
        CourseEntity course = courseopt.get();
        return ResponseEntity.ok(course);
    }

    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher})
    public ResponseEntity<String> deleteCourse(@PathVariable long courseId){
        try {
            CourseEntity courseEntity = courseRepository.findById(courseId).orElseThrow();

            //voor elk project
            for(ProjectEntity project : courseRepository.findAllProjectsByCourseId(courseId)){
                //voor elke test van project test verwjideren en bestanden verwijderen
                long testId = project.getTestId();
                TestEntity test = testRepository.findById(testId).orElseThrow();
                FileEntity dockertestFileEntity = fileRepository.findById(test.getDockerTest()).orElseThrow();
                FileEntity structuretestFileEntity = fileRepository.findById(test.getStructureTestId()).orElseThrow();
                Filehandler.deleteFile(dockertestFileEntity.getPath());
                Filehandler.deleteFile(structuretestFileEntity.getPath());
                fileRepository.delete(dockertestFileEntity);
                fileRepository.delete(structuretestFileEntity);
                //elke deadline verwijderen
                List<DeadlineEntity> deadlines = project.getDeadlines();
                deadlineRepository.deleteAll(deadlines);
                //elke submission verwijderen
                List<SubmissionEntity> submissions = submissionRepository.findAllByProjectId(project.getId());
                for(SubmissionEntity submission : submissions){
                    FileEntity file = fileRepository.findById(submission.getFileId()).orElseThrow();
                    Filehandler.deleteFile(file.getPath());
                    fileRepository.delete(file);
                }
                submissionRepository.deleteAll(submissions);
                // project verwijderen
                projectRepository.delete(project);
            }

            //voor elke groepcluster
            for(GroupClusterEntity groupCluster : groupClusterRepository.findByCourseId(courseId)){
                //voor elke groep
                for(GroupEntity group: groupRepository.findAllByClusterId(groupCluster.getId())){
                    //elke groepmember verwijderen
                    for(GroupRepository.UserReference userReference : groupRepository.findGroupUsersReferencesByGroupId(group.getId())){
                        groupMemberRepository.removeMemberFromGroup(group.getId(), userReference.getUserId());
                    }
                    //elke groepfeedback verwijderen
                    groupFeedbackRepository.deleteAll(groupFeedbackRepository.findGroupFeedbackEntitiesByGroupId(group.getId()));
                    //groep verwijderen
                    groupRepository.delete(group);
                }
                //groepcluster verwijderen
                groupClusterRepository.delete(groupCluster);
            }
            //vak verwijderen
            courseRepository.delete(courseEntity);
            return ResponseEntity.ok("Vak verwijderd");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Probleem bij vak verwijderen: " + e.getMessage());
        }
    }

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/projects")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<List<ProjectEntity>> getProjectByCourseId(@PathVariable Long courseId) {
        List<ProjectEntity> projects = projectRepository.findByCourseId(courseId);
        if (projects.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(projects);
    }


    /* Function to add a new project to an existing course */
    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/projects")
    @Roles({UserRole.teacher})
    public ResponseEntity<String> createProject(
            @PathVariable long courseId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam long groupClusterId,
            @RequestParam long testId,
            @RequestParam boolean projectType,
            @RequestParam Integer maxScore) {
        try {
            // Create a new ProjectEntity instance
            ProjectEntity project = new ProjectEntity(courseId, name, description, groupClusterId, testId, projectType, maxScore);

            // Save the project entity
            ProjectEntity savedProject = projectRepository.save(project);
            // Prepare response JSON
            Map<String, Object> response = createJSONPostResponse(savedProject);

            // Convert response map to JSON string
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonResponse = objectMapper.writeValueAsString(response);
            // Return success response with JSON string
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating project: " + e.getMessage());
        }
    }

    /* Help function to create a JSON response when creating a new project */
    private static Map<String, Object> createJSONPostResponse(ProjectEntity savedProject) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedProject.getId());
        response.put("name", savedProject.getName());
        response.put("description", savedProject.getDescription());
        response.put("course", String.valueOf(savedProject.getCourseId()));
        response.put("deadline", 0); // Placeholder for deadline
            /* Optional timestamp
            if (savedProject.getTimestamp() != null) {
                response.put("timestamp", savedProject.getTimestamp().toString());
            }*/
        response.put("tests_url", ApiRoutes.PROJECT_BASE_PATH + "/" + savedProject.getId() + "/tests");
        response.put("submission_url", ApiRoutes.PROJECT_BASE_PATH + "/" + savedProject.getId() + "/sumbmissions");
        return response;
    }

}
