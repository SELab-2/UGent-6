package com.ugent.pidgeon.controllers.requestBodies;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private TestRepository testRepository;


    @DeleteMapping(ApiRoutes.TEST_BASE_PATH + "/{testId}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> deleteTestById(@PathVariable("testId") long testId, Auth auth) {
        // Get the submission entry from the database
        TestEntity testEntity = testRepository.findById(testId).orElse(null);
        if (testEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        testRepository.delete(testEntity);
        return  ResponseEntity.ok(testEntity);
    }
}
