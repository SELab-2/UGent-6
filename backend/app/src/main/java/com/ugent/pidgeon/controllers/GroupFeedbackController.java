package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.json.GroupFeedbackJsonWithProject;
import com.ugent.pidgeon.json.UpdateGroupScoreRequest;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.CourseUtil;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.GroupFeedbackUtil;
import com.ugent.pidgeon.util.GroupUtil;
import com.ugent.pidgeon.util.Pair;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GroupFeedbackController {

    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;
    @Autowired
    private GroupFeedbackUtil groupFeedbackUtil;
    @Autowired
    private GroupUtil groupUtil;
    @Autowired
    private EntityToJsonConverter entityToJsonConverter;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private CourseUtil courseUtil;

    /**
     * Function to update the score of a group
     *
     * @param groupId   identifier of a group
     * @param projectId identifier of a project
     * @param request   request object containing the new score
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity<String>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883691">apiDog documentation</a>
     * @HttpMethod PATCH
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/groups/{groupid}/score
     */
    @PatchMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {

        CheckResult<GroupFeedbackEntity> checkResult = groupFeedbackUtil.checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity(), HttpMethod.PATCH);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        GroupFeedbackEntity groupFeedbackEntity = checkResult.getData();

        if (request.getScore() == null) {
            request.setScore(groupFeedbackEntity.getScore());
        }

        if (request.getFeedback() == null) {
            request.setFeedback(groupFeedbackEntity.getFeedback());
        }

        CheckResult<Void> checkResultJson = groupFeedbackUtil.checkGroupFeedbackUpdateJson(request, projectId);
        if (checkResultJson.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResultJson.getStatus()).body(checkResultJson.getMessage());
        }

        return doGroupFeedbackUpdate(groupFeedbackEntity, request);
    }

    /**
     * Function to delete the score of a group
     *
     * @param groupId   identifier of a group
     * @param projectId identifier of a project
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity<String>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-7436586">apiDog documentation</a>
     * @HttpMethod Delete
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/groups/{groupid}/score
     */
    @DeleteMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> deleteGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, Auth auth) {
        CheckResult<GroupFeedbackEntity> checkResult = groupFeedbackUtil.checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity(), HttpMethod.DELETE);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        try {
            groupFeedbackRepository.delete(checkResult.getData());
            return ResponseEntity.status(HttpStatus.OK).body("Group feedback deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not delete group feedback");
        }
    }

    /**
     * Function to update the score of a group
     *
     * @param groupId   identifier of a group
     * @param projectId identifier of a project
     * @param request   request object containing the new score
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity<String>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5883690">apiDog documentation</a>
     * @HttpMethod PUT
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/groups/{groupid}/score
     */
    @PutMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateGroupScorePut(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {

        CheckResult<GroupFeedbackEntity> checkResult = groupFeedbackUtil.checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity(), HttpMethod.PUT);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        GroupFeedbackEntity groupFeedbackEntity = checkResult.getData();

        CheckResult<Void> checkResultJson = groupFeedbackUtil.checkGroupFeedbackUpdateJson(request, projectId);
        if (checkResultJson.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResultJson.getStatus()).body(checkResultJson.getMessage());
        }

        return doGroupFeedbackUpdate(groupFeedbackEntity, request);
    }

    public ResponseEntity<?> doGroupFeedbackUpdate(GroupFeedbackEntity groupFeedbackEntity, UpdateGroupScoreRequest request) {
        groupFeedbackEntity.setScore(request.getScore());
        groupFeedbackEntity.setFeedback(request.getFeedback());
        try {
            groupFeedbackRepository.save(groupFeedbackEntity);
            return ResponseEntity.status(HttpStatus.OK).body(entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not update score of group feedback");
        }
    }

    /**
     * Function to add a score to a group
     *
     * @param groupId   identifier of a group
     * @param projectId identifier of a project
     * @param request   request object containing the new score
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity<String>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6697044">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}/projects/{projectid}/feedback
     */
    @PostMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> addGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, @RequestBody UpdateGroupScoreRequest request, Auth auth) {

        CheckResult<GroupFeedbackEntity> groupFeedback = groupFeedbackUtil.checkGroupFeedbackUpdate(groupId, projectId, auth.getUserEntity(), HttpMethod.POST);
        if (groupFeedback.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(groupFeedback.getStatus()).body(groupFeedback.getMessage());
        }

        CheckResult<Void> checkResultJson = groupFeedbackUtil.checkGroupFeedbackUpdateJson(request, projectId);
        if (checkResultJson.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResultJson.getStatus()).body(checkResultJson.getMessage());
        }

        GroupFeedbackEntity groupFeedbackEntity = new GroupFeedbackEntity(groupId, projectId, request.getScore(), request.getFeedback());

        try {
            groupFeedbackEntity = groupFeedbackRepository.save(groupFeedbackEntity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not add score to group feedback");
        }
    }


    /**
     * Function to get the score of a group
     *
     * @param groupId   identifier of a group
     * @param projectId identifier of a project
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity<Object>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-7436611">apiDog documentation</a>
     * @HttpMethod Get
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/groups/{groupid}/score
     */
    @GetMapping(ApiRoutes.GROUP_FEEDBACK_PATH)
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> getGroupScore(@PathVariable("groupid") long groupId, @PathVariable("projectid") long projectId, Auth auth) {
        UserEntity user = auth.getUserEntity();

        CheckResult<Void> checkResult = groupFeedbackUtil.checkGroupFeedback(groupId, projectId);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        CheckResult<Void> canGetFeedback = groupUtil.canGetProjectGroupData(groupId, projectId, user);
        if (canGetFeedback.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(canGetFeedback.getStatus()).body(canGetFeedback.getMessage());
        }

        CheckResult<GroupFeedbackEntity> groupFeedback = groupFeedbackUtil.getGroupFeedbackIfExists(groupId, projectId);
        if (groupFeedback.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(groupFeedback.getStatus()).body(groupFeedback.getMessage());
        }
        GroupFeedbackEntity groupFeedbackEntity = groupFeedback.getData();

        return ResponseEntity.ok(entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity));
    }

    /**
     * Function to get the grades of a course
     *
     * @param courseId identifier of a course
     * @param auth     authentication object of the requesting user
     * @return ResponseEntity<Object>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-7436985">apiDog documentation</a>
     * @HttpMethod Get
     * @AllowedRoles teacher, student
     * @ApiPath /api/courses/{courseId}/grades
     */
    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/grades")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getCourseGrades(@PathVariable("courseId") long courseId, Auth auth) {
        UserEntity user = auth.getUserEntity();
        CheckResult<Pair<CourseEntity, CourseRelation>> courseCheck = courseUtil.getCourseIfUserInCourse(courseId, user);
        if (courseCheck.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(courseCheck.getStatus()).body(courseCheck.getMessage());
        }
        CourseRelation relation = courseCheck.getData().getSecond();
        if (!relation.equals(CourseRelation.enrolled)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are a admin of this course so no grades are available");
        }


        List<ProjectEntity> projects = projectRepository.findByCourseId(courseId);
        projects = projects.stream().filter(ProjectEntity::isVisible).toList();

        List<GroupFeedbackJsonWithProject> grades = new ArrayList<>();
        for (ProjectEntity project : projects) {
            Long groupId = groupRepository.groupIdByProjectAndUser(project.getId(), user.getId());
            if (groupId == null) { // Student not yet in a group for this project
              grades.add(entityToJsonConverter.groupFeedbackEntityToJsonWithProject(null, project));
            } else {
              CheckResult<GroupFeedbackEntity> checkResult = groupFeedbackUtil.getGroupFeedbackIfExists(groupId, project.getId());
              if (checkResult.getStatus() != HttpStatus.OK) {
                grades.add(entityToJsonConverter.groupFeedbackEntityToJsonWithProject(null, project));
              } else {
                GroupFeedbackEntity groupFeedbackEntity = checkResult.getData();
                grades.add(entityToJsonConverter.groupFeedbackEntityToJsonWithProject(groupFeedbackEntity, project));
              }
            }
        }
        return ResponseEntity.ok(grades);
    }



}
