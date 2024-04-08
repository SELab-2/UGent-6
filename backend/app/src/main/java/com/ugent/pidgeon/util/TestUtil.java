package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.controllers.CheckResult;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

@Component
public class TestUtil {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRepository testRepository;

    public TestEntity getTestIfExists(long projectId) {
        return testRepository.findByProjectId(projectId).orElse(null);
    }

    public CheckResult checkForTestUpdate(
            long projectId,
            UserEntity user,
            String dockerImage,
            MultipartFile dockerTest,
            MultipartFile structureTest,
            HttpMethod httpMethod
    ) {
        ProjectEntity projectEntity = projectRepository.findById(projectId).orElse(null);
        if (projectEntity == null) {
            return new CheckResult(HttpStatus.NOT_FOUND, "Project not found");
        }

        if(!projectRepository.adminOfProject(projectId, user.getId()) && user.getRole() != UserRole.admin){
            return new CheckResult(HttpStatus.FORBIDDEN, "You don't have acces to update the tests of this project");
        }

        if (httpMethod.equals(HttpMethod.POST) && projectEntity.getTestId() != null) {
            return new CheckResult(HttpStatus.CONFLICT, "Tests already exist for this project");
        }

        if (!httpMethod.equals(HttpMethod.PATCH)) {
            if (dockerImage == null || dockerTest == null || structureTest == null) {
                return new CheckResult(HttpStatus.BAD_REQUEST, "Missing parameters: dockerimage (string), dockertest (file), structuretest (file) are required");
            }
        }

        return new CheckResult(HttpStatus.OK, "");
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
