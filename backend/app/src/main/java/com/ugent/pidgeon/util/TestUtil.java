package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
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

    public TestEntity getTestIfExists(long projectId) {
        return testRepository.findByProjectId(projectId).orElse(null);
    }

    public CheckResult<Pair<TestEntity, ProjectEntity>> checkForTestUpdate(
            long projectId,
            UserEntity user,
            String dockerImage,
            MultipartFile dockerTest,
            MultipartFile structureTest,
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

        if (!httpMethod.equals(HttpMethod.PATCH)) {
            if (dockerImage == null || dockerTest == null || structureTest == null) {
                return new CheckResult<>(HttpStatus.BAD_REQUEST, "Missing parameters: dockerimage (string), dockertest (file), structuretest (file) are required", null);
            }
        }

        return new CheckResult<>(HttpStatus.OK, "", new Pair<>(testEntity, projectEntity));
    }

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

    public TestJson testEntityToTestJson(TestEntity testEntity, long projectId) {
        return new TestJson(
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId,
                testEntity.getDockerImage(),
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId + "/tests/dockertest",
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId + "/tests/structuretest"
        );
    }
}
