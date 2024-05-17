package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupFeedbackJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.LastGroupSubmissionJson;
import com.ugent.pidgeon.model.json.SubmissionJson;
import com.ugent.pidgeon.model.submissionTesting.DockerOutput;
import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.DockerTestState;
import com.ugent.pidgeon.postgre.models.types.DockerTestType;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

@RestController
public class    SubmissionController {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;

    @Autowired
    private SubmissionUtil submissionUtil;
    @Autowired
    private ProjectUtil projectUtil;
    @Autowired
    private GroupUtil groupUtil;
    @Autowired
    private EntityToJsonConverter entityToJsonConverter;
    @Autowired
    private CommonDatabaseActions commonDatabaseActions;
    @Autowired
    private TestUtil testUtil;

    @Autowired
    private TestRunner testRunner;


    /**
     * Function to get a submission by its ID
     *
     * @param submissionid ID of the submission to get
     * @param auth         authentication object of the requesting user
     * @return ResponseEntity with the submission
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723933">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/submissions/{submissionid}
     */
    @GetMapping(ApiRoutes.SUBMISSION_BASE_PATH + "/{submissionid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getSubmission(@PathVariable("submissionid") long submissionid, Auth auth) {
        CheckResult<SubmissionEntity> checkResult = submissionUtil.canGetSubmission(submissionid, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        SubmissionEntity submission = checkResult.getData();
        SubmissionJson submissionJson = entityToJsonConverter.getSubmissionJson(submission);

    return ResponseEntity.ok(submissionJson);
  }

    /**
     * Function to get all submissions
     *
     * @param projectid ID of the project to get the submissions from
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity with a list of submissions
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723934">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/submissions
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/submissions") //Route to get all submissions for a project
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getSubmissions(@PathVariable("projectid") long projectid, Auth auth) {
        try {
            CheckResult<Void> checkResult = projectUtil.isProjectAdmin(projectid, auth.getUserEntity());
            if (!checkResult.getStatus().equals(HttpStatus.OK)) {
                return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
            }

            List<Long> projectGroupIds = projectRepository.findGroupIdsByProjectId(projectid);
            List<LastGroupSubmissionJson> res = projectGroupIds.stream().map(groupId -> {
                GroupEntity group = groupRepository.findById(groupId).orElse(null);
                if (group == null) {
                    throw new RuntimeException("Group not found");
                }
                GroupJson groupjson = entityToJsonConverter.groupEntityToJson(group);
                GroupFeedbackEntity groupFeedbackEntity = groupFeedbackRepository.getGroupFeedback(groupId, projectid);
                GroupFeedbackJson groupFeedbackJson;
                if (groupFeedbackEntity == null) {
                    groupFeedbackJson = null;
                } else {
                    groupFeedbackJson = entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity);
                }
                SubmissionEntity submission = submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(projectid, groupId).orElse(null);
                if (submission == null) {
                    return new LastGroupSubmissionJson(null, groupjson, groupFeedbackJson);
                }

                return new LastGroupSubmissionJson(entityToJsonConverter.getSubmissionJson(submission), groupjson, groupFeedbackJson);

            }).toList();
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    /**
     * Function to submit a file
     *
     * @param file      file to submit
     * @param projectid ID of the project to submit to
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity with the submission
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723930">apiDog documentation</a>
     * @HttpMethod POST
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/submit
     */
    @PostMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/submit")
    //Route to submit a file, it accepts a multiform with the file and submissionTime
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> submitFile(@RequestParam("file") MultipartFile file, @PathVariable("projectid") long projectid, Auth auth) {
        long userId = auth.getUserEntity().getId();
        CheckResult<Long> checkResult = submissionUtil.checkOnSubmit(projectid, auth.getUserEntity());

        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        Long groupId = checkResult.getData();

        try {
            //Save the file entry in the database to get the id
            FileEntity fileEntity = new FileEntity("", "", userId);
            fileEntity = fileRepository.save(fileEntity);
            long fileid = fileEntity.getId();

            OffsetDateTime now = OffsetDateTime.now();
            SubmissionEntity submissionEntity = new SubmissionEntity(
                    projectid,
                    groupId,
                    fileid,
                    now,
                    false,
                    false
            );
            submissionEntity.setDockerTestState(DockerTestState.finished);

            //Save the submission in the database
            SubmissionEntity submission = submissionRepository.save(submissionEntity);

            //Save the file on the server
            String filename = file.getOriginalFilename();
            Path path = Filehandler.getSubmissionPath(projectid, groupId, submission.getId());
            File savedFile = Filehandler.saveSubmission(path, file);
            String pathname = path.resolve(Filehandler.SUBMISSION_FILENAME).toString();

            //Update name and path for the file entry
            fileEntity.setName(filename);
            fileEntity.setPath(pathname);
            fileRepository.save(fileEntity);

      // Run structure tests
      TestEntity testEntity = testRepository.findByProjectId(projectid).orElse(null);
      SubmissionTemplateModel.SubmissionResult structureTestResult;
      if (testEntity == null) {
        Logger.getLogger("SubmissionController").info("no tests");
        submission.setStructureFeedback("No specific structure requested for this project.");
        submission.setStructureAccepted(true);
      } else {

        // Check file structure
        SubmissionTemplateModel model = new SubmissionTemplateModel();
        structureTestResult = testRunner.runStructureTest(new ZipFile(savedFile), testEntity, model);
        if (structureTestResult == null) {
          submission.setStructureFeedback(
              "No specific structure requested for this project.");
          submission.setStructureAccepted(true);
        } else {
          submission.setStructureAccepted(structureTestResult.passed);
          submission.setStructureFeedback(structureTestResult.feedback);
        }

        if (testEntity.getDockerTestTemplate() != null) {
          submission.setDockerType(DockerTestType.TEMPLATE);
        } else if (testEntity.getDockerTestScript() != null) {
          submission.setDockerType(DockerTestType.SIMPLE);
        } else {
          submission.setDockerType(DockerTestType.NONE);
        }

        // save the first feedback, without docker feedback
        submissionRepository.save(submission);

        if (testEntity.getDockerTestScript() != null) {
          // Define docker test as running
          submission.setDockerTestState(DockerTestState.running);
          // run docker tests in background
          File finalSavedFile = savedFile;
          Path artifactPath = Filehandler.getSubmissionArtifactPath(projectid, groupId, submission.getId());

          CompletableFuture.runAsync(() -> {
            try {
              // Check if docker tests succeed
              DockerSubmissionTestModel dockerModel = new DockerSubmissionTestModel(testEntity.getDockerImage());
              DockerOutput dockerOutput = testRunner.runDockerTest(new ZipFile(finalSavedFile), testEntity, artifactPath, dockerModel);
              if (dockerOutput == null) {
                throw new RuntimeException("Error while running docker tests.");
              }
              // Representation of dockerOutput, this will be a json(easily displayable in frontend) if it is a template test
              // or a string if it is a simple test
              submission.setDockerFeedback(dockerOutput.getFeedbackAsString());
              submission.setDockerAccepted(dockerOutput.isAllowed());

              submission.setDockerTestState(DockerTestState.finished);
              submissionRepository.save(submission);
            } catch (Exception e) {
              /* Log error */
              Logger.getLogger("SubmissionController").log(Level.SEVERE, e.getMessage(), e);

              submission.setDockerFeedback("");
              submission.setDockerAccepted(false);

              submission.setDockerTestState(DockerTestState.aborted);
              submissionRepository.save(submission);

            }
          });
        }
      }

      return ResponseEntity.ok(entityToJsonConverter.getSubmissionJson(submission));
    } catch (Exception e) {
      Logger.getLogger("SubmissionController").log(Level.SEVERE, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to save submissions on file server.");
    }
  }

    /**
     * Function to get a submission file
     *
     * @param submissionid ID of the submission to get the file from
     * @param auth         authentication object of the requesting user
     * @return ResponseEntity with the file
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5904321">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/submissions/{submissionid}/file
     */
    @GetMapping(ApiRoutes.SUBMISSION_BASE_PATH + "/{submissionid}/file") //Route to get a submission
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getSubmissionFile(@PathVariable("submissionid") long submissionid, Auth auth) {
        CheckResult<SubmissionEntity> checkResult = submissionUtil.canGetSubmission(submissionid, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        SubmissionEntity submission = checkResult.getData();

        // Get the file entry from the database
        FileEntity file = fileRepository.findById(submission.getFileId()).orElse(null);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Get the file from the server
        try {
            Resource zipFile = Filehandler.getFileAsResource(Path.of(file.getPath()));
            if (zipFile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found.");
            }

            // Set headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipFile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping(ApiRoutes.SUBMISSION_BASE_PATH + "/{submissionid}/artifacts") //Route to get a submission
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getSubmissionArtifacts(@PathVariable("submissionid") long submissionid, Auth auth) {
        CheckResult<SubmissionEntity> checkResult = submissionUtil.canGetSubmission(submissionid, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        SubmissionEntity submission = checkResult.getData();

        // Get the file from the server
        try {
            Resource zipFile = Filehandler.getFileAsResource(Filehandler.getSubmissionArtifactPath(submission.getProjectId(), submission.getGroupId(), submission.getId()));
            if (zipFile == null) {
              return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No artifacts found for this submission.");
            }
            // Set headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=artifacts.zip" );
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipFile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }




    /**
     * Function to delete a submission
     *
     * @param submissionid ID of the submission to delete
     * @param auth         authentication object of the requesting user
     * @return ResponseEntity
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723955">apiDog documentation</a>
     * @HttpMethod DELETE
     * @AllowedRoles teacher
     * @ApiPath /api/submissions/{submissionid}
     */
    @DeleteMapping(ApiRoutes.SUBMISSION_BASE_PATH + "/{submissionid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> deleteSubmissionById(@PathVariable("submissionid") long submissionid, Auth auth) {
        CheckResult<SubmissionEntity> checkResult = submissionUtil.canDeleteSubmission(submissionid, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        commonDatabaseActions.deleteSubmissionById(submissionid);

        return ResponseEntity.ok().build();
    }

    /**
     * Function to get all submissions for a group
     *
     * @param projectid ID of the project to get the submissions from
     * @param groupid   ID of the group to get the submissions from
     * @param auth      authentication object of the requesting user
     * @return ResponseEntity with a list of submissions
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6257745">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/submissions/{groupid}
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/submissions/{groupid}")
    //Route to get all submissions for a project
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getSubmissionsForGroup(@PathVariable("projectid") long projectid, @PathVariable("groupid") long groupid, Auth auth) {
        CheckResult<Void> accesCheck = groupUtil.canGetProjectGroupData(groupid, projectid, auth.getUserEntity());
        if (!accesCheck.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(accesCheck.getStatus()).body(accesCheck.getMessage());
        }

        List<SubmissionEntity> submissions = submissionRepository.findByProjectIdAndGroupId(projectid, groupid);
        List<SubmissionJson> res = submissions.stream().map(entityToJsonConverter::getSubmissionJson).toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/adminsubmissions")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getAdminSubmissions(@PathVariable("projectid") long projectid, Auth auth) {
        CheckResult<Void> checkResult = projectUtil.isProjectAdmin(projectid, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        List<SubmissionEntity> submissions = submissionRepository.findAdminSubmissionsByProjectId(projectid);
        List<SubmissionJson> res = submissions.stream().map(entityToJsonConverter::getSubmissionJson).toList();
        return ResponseEntity.ok(res);
    }
}