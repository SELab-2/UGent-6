package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupFeedbackJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.LastGroupSubmissionJson;
import com.ugent.pidgeon.model.json.SubmissionJson;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

@RestController
public class SubmissionController {

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


    private SubmissionTemplateModel.SubmissionResult runStructureTest(ZipFile file, TestEntity testEntity) throws IOException {
        // Get the test file from the server
        FileEntity testfileEntity = fileRepository.findById(testEntity.getStructureTestId()).orElse(null);
        if (testfileEntity == null) {
            return null;
        }
        String testfile = Filehandler.getStructureTestString(Path.of(testfileEntity.getPath()));

        // Parse the file
        SubmissionTemplateModel model = new SubmissionTemplateModel();
        model.parseSubmissionTemplate(testfile);

        return model.checkSubmission(file);
    }

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
                    groupFeedbackJson = new GroupFeedbackJson(groupFeedbackEntity.getScore(), groupFeedbackEntity.getFeedback(), groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());
                }
                Long submissionId = submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(projectid, groupId);
                if (submissionId == null) {
                    return new LastGroupSubmissionJson(null, groupjson, groupFeedbackJson);
                }

                SubmissionEntity submission = submissionRepository.findById(submissionId).orElse(null);
                if (submission == null) {
                    throw new RuntimeException("Submission not found");
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

        long groupId = checkResult.getData();

        //TODO: execute the docker tests onces these are implemented
        try {
            //Save the file entry in the database to get the id
            FileEntity fileEntity = new FileEntity("", "", userId);
            long fileid = fileRepository.save(fileEntity).getId();

            OffsetDateTime now = OffsetDateTime.now();
            SubmissionEntity submissionEntity = new SubmissionEntity(
                    projectid,
                    groupId,
                    fileid,
                    now,
                    false,
                    false
            );

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
            SubmissionTemplateModel.SubmissionResult testresult;
            if (testEntity == null) {
                Logger.getLogger("SubmissionController").info("no test");
                testresult = new SubmissionTemplateModel.SubmissionResult(true, "No structure requirements for this project.");
            } else {
                testresult = runStructureTest(new ZipFile(savedFile), testEntity);
            }
            if (testresult == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while running tests: test files not found");
            }
            submissionRepository.save(submissionEntity);
            // Update the submission with the test resultsetAccepted
            submission.setStructureAccepted(testresult.passed);
            submission = submissionRepository.save(submission);

            // Update the submission with the test feedbackfiles
            submission.setDockerFeedback("TEMP DOCKER FEEDBACK");
            submission.setStructureFeedback(testresult.feedback);
            submissionRepository.save(submission);

            return ResponseEntity.ok(entityToJsonConverter.getSubmissionJson(submissionEntity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving file: " + e.getMessage());
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
            Resource zipFile = Filehandler.getSubmissionAsResource(Path.of(file.getPath()));

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


    public ResponseEntity<?> getFeedbackReponseEntity(long submissionid, Auth auth, Function<SubmissionEntity, String> feedbackGetter) {

        CheckResult<SubmissionEntity> checkResult = submissionUtil.canGetSubmission(submissionid, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        SubmissionEntity submission = checkResult.getData();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.TEXT_PLAIN));
        return ResponseEntity.ok().headers(headers).body(feedbackGetter.apply(submission));
    }

    /**
     * Function to get the structure feedback of a submission
     *
     * @param submissionid ID of the submission to get the feedback from
     * @param auth         authentication object of the requesting user
     * @return ResponseEntity with the feedback
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6195994">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/submissions/{submissionid}/structurefeedback
     */
    @GetMapping(ApiRoutes.SUBMISSION_BASE_PATH + "/{submissionid}/structurefeedback")
    //Route to get the structure feedback
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getStructureFeedback(@PathVariable("submissionid") long submissionid, Auth auth) {
        return getFeedbackReponseEntity(submissionid, auth, SubmissionEntity::getStructureFeedback);
    }

    /**
     * Function to get the docker feedback of a submission
     *
     * @param submissionid ID of the submission to get the feedback from
     * @param auth         authentication object of the requesting user
     * @return ResponseEntity with the feedback
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6195996">apiDog documentation</a>
     * @HttpMethod GET
     * @AllowedRoles teacher, student
     * @ApiPath /api/submissions/{submissionid}/dockerfeedback
     */
    @GetMapping(ApiRoutes.SUBMISSION_BASE_PATH + "/{submissionid}/dockerfeedback") //Route to get the docker feedback
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getDockerFeedback(@PathVariable("submissionid") long submissionid, Auth auth) {
        return getFeedbackReponseEntity(submissionid, auth, SubmissionEntity::getDockerFeedback);
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
}