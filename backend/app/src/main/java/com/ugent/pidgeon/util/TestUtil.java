package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class TestUtil {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private ProjectUtil projectUtil;

    /**
     * Check if a test exists for a project
     * @param projectId id of the project
     * @return the test if it exists, null otherwise
     */
    public TestEntity getTestIfExists(long projectId) {
        return testRepository.findByProjectId(projectId).orElse(null);
    }

    /**
     * Check if a user can update a test
     * @param projectId id of the project
     * @param user user that wants to update the test
     * @param dockerImage docker image for the test
     * @param dockerScript docker script for the test
     * @param dockerTemplate docker template for the test
     * @param httpMethod http method used to update the test
     * @return CheckResult with the status of the check and the test and project
     */
    public CheckResult<Pair<TestEntity, ProjectEntity>> checkForTestUpdate(
            long projectId,
            UserEntity user,
            String dockerImage,
            String dockerScript,
            String dockerTemplate,
            HttpMethod httpMethod
    ) {

        CheckResult<ProjectEntity> projectCheck = projectUtil.getProjectIfAdmin(projectId, user);
        if (!projectCheck.getStatus().equals(HttpStatus.OK)) {
            return new CheckResult<>(projectCheck.getStatus(), projectCheck.getMessage(), null);
        }
        ProjectEntity projectEntity = projectCheck.getData();
        TestEntity testEntity = getTestIfExists(projectId);

        if (testEntity == null && !httpMethod.equals(HttpMethod.POST)) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "No tests found for project with id: " + projectId, null);
        }

        if (httpMethod.equals(HttpMethod.DELETE)) {
            return new CheckResult<>(HttpStatus.OK, "", new Pair<>(testEntity, projectEntity));
        }

        if (httpMethod.equals(HttpMethod.POST) && projectEntity.getTestId() != null) {
            return new CheckResult<>(HttpStatus.CONFLICT, "Tests already exist for this project", null);
        }

        if(!httpMethod.equals(HttpMethod.PATCH) && dockerImage != null && dockerScript == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "A test script is required if u add a dockerimage.", null);
        }
        if (!httpMethod.equals(HttpMethod.PATCH) && dockerScript != null && dockerImage == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "A docker image is required if u add a script", null);
        }

        // This returns false if the image isn't pullt yet! FIX PLS
        if(dockerImage != null && !DockerSubmissionTestModel.imageExists(dockerImage)) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "A valid docker image is required in a docker test.", null);
        }

        if (!httpMethod.equals(HttpMethod.PATCH) && dockerTemplate != null && dockerImage == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "A test script and image are required in a docker template test.", null);
        }

        if(httpMethod.equals(HttpMethod.PATCH) && dockerScript != null && testEntity.getDockerImage() == null && dockerImage == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "No docker image is configured for this test", null);
        }

        if(httpMethod.equals(HttpMethod.PATCH) && dockerImage != null && testEntity.getDockerTestScript() == null && dockerScript == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "No docker test script is configured for this test", null);
        }

        if(dockerTemplate != null && !DockerSubmissionTestModel.isValidTemplate(dockerTemplate)) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Invalid docker template", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", new Pair<>(testEntity, projectEntity));
    }

    /**
     * Check if a user can get a test (only project admins can get tests)
     * @param projectId id of the project
     * @param user user that wants to get the test
     * @return CheckResult with the status of the check and the test
     */
    public CheckResult<TestEntity> getTestIfAdmin(long projectId, UserEntity user) {
        TestEntity testEntity = getTestIfExists(projectId);
        if (testEntity == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "No tests found for project with id: " + projectId, null);
        }

        CheckResult<Void> isProjectAdmin = projectUtil.isProjectAdmin(projectId, user);
        if (!isProjectAdmin.getStatus().equals(HttpStatus.OK)) {
            return new CheckResult<>(isProjectAdmin.getStatus(), isProjectAdmin.getMessage(), null);
        }

        return new CheckResult<>(HttpStatus.OK, "", testEntity);
    }

    public CheckResult<Pair<TestEntity, Boolean>> getTestWithAdminStatus(long projectId, UserEntity user) {
        TestEntity testEntity = getTestIfExists(projectId);
        if (testEntity == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "No tests found for project with id: " + projectId, null);
        }

        boolean userPartOfProject = projectUtil.userPartOfProject(projectId, user.getId());
        if (!userPartOfProject) {
            return new CheckResult<>(HttpStatus.FORBIDDEN, "You are not part of this project", null);
        }

        boolean admin = false;

        CheckResult<Void> isProjectAdmin = projectUtil.isProjectAdmin(projectId, user);
        if (isProjectAdmin.getStatus().equals(HttpStatus.OK)) {
            admin = true;
        } else if (!isProjectAdmin.getStatus().equals(HttpStatus.FORBIDDEN)){
            return new CheckResult<>(isProjectAdmin.getStatus(), isProjectAdmin.getMessage(), null);
        } else if (user.getRole().equals(UserRole.admin)) {
            admin = true;
        }

        return new CheckResult<>(HttpStatus.OK, "", new Pair<>(testEntity, admin));
    }
}
