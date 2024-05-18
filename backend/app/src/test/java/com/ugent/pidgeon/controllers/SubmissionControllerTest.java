package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.json.DockerTestFeedbackJson;
import com.ugent.pidgeon.model.json.GroupFeedbackJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.LastGroupSubmissionJson;
import com.ugent.pidgeon.model.json.SubmissionJson;
import com.ugent.pidgeon.model.submissionTesting.DockerOutput;
import com.ugent.pidgeon.model.submissionTesting.DockerTestOutput;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel.SubmissionResult;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.types.DockerTestState;
import com.ugent.pidgeon.postgre.models.types.DockerTestType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class SubmissionControllerTest extends ControllerTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TestRepository testRepository;
    @Mock
    private GroupFeedbackRepository groupFeedbackRepository;
    @Mock
    private TestRunner testRunner;

    @Mock
    private SubmissionUtil submissionUtil;
    @Mock
    private ProjectUtil projectUtil;
    @Mock
    private GroupUtil groupUtil;
    @Mock
    private EntityToJsonConverter entityToJsonConverter;
    @Mock
    private CommonDatabaseActions commonDatabaseActions;
    @InjectMocks
    private SubmissionController submissionController;

    private ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();

    private SubmissionEntity submission;
    private List<Long> groupIds;
    private SubmissionJson submissionJson;
    private GroupJson groupJson;
    private GroupFeedbackJson groupFeedbackJson;
    private GroupEntity groupEntity;
    private GroupFeedbackEntity groupFeedbackEntity;
    private MockMultipartFile mockMultipartFile;
    private FileEntity fileEntity;
    private LastGroupSubmissionJson lastGroupSubmissionJson;
    private TestEntity testEntity;


    public static File createTestFile() throws IOException {
        // Create a temporary directory
        File tempDir = Files.createTempDirectory("test-dir").toFile();

        // Create a temporary file within the directory
        File tempFile = File.createTempFile("test-file", ".zip", tempDir);

        // Create some content to write into the zip file
        String content = "Hello, this is a test file!";
        byte[] bytes = content.getBytes();

        // Write the content into a file inside the zip file
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempFile))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zipOut.putNextEntry(entry);
            zipOut.write(bytes);
            zipOut.closeEntry();
        }

        // Return the File object representing the zip file
        return tempFile;
    }

    @BeforeEach
    public void setup() {
        setUpController(submissionController);

        submission = new SubmissionEntity(22L, 45L, 99L, OffsetDateTime.MIN, true, true);
        submission.setId(56L);
        groupIds = List.of(45L);
        submissionJson = new SubmissionJson(
            submission.getId(),
            "projecturl",
            "groupurl",
            submission.getProjectId(),
            submission.getGroupId(),
            "fileurl",
            true,
            OffsetDateTime.MIN,
            "structureFeedback",
            new DockerTestFeedbackJson(DockerTestType.NONE, "", true),
            null,
            "artifacturl"
        );
        groupEntity = new GroupEntity("groupname", 99L);
        groupEntity.setId(submission.getGroupId());
        groupJson = new GroupJson(3, groupEntity.getId(), "groupname", "groupclusterurl");

        groupFeedbackEntity = new GroupFeedbackEntity(groupEntity.getId(),
            submission.getProjectId(), 3F, "feedback");
        groupFeedbackJson = new GroupFeedbackJson(groupFeedbackEntity.getScore(),
            groupFeedbackEntity.getFeedback(), groupFeedbackEntity.getGroupId(),
            groupFeedbackEntity.getProjectId());

        byte[] fileContent = "Your file content".getBytes();
        mockMultipartFile = new MockMultipartFile("file", "filename.txt",
            MediaType.TEXT_PLAIN_VALUE, fileContent);
        fileEntity = new FileEntity("name", "dir/name", 1L);
        fileEntity.setId(submission.getFileId());

        lastGroupSubmissionJson = new LastGroupSubmissionJson(
            submissionJson,
            groupJson,
            groupFeedbackJson
        );

        testEntity = new TestEntity(
            "dockerImage",
            "dockerTestScript",
            "dockerTestTemplate",
            "structureTemplate"
        );

    }

    @Test
    public void testGetSubmission() throws Exception {
        String url = ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId();
        /* all checks succeed */
        when(submissionUtil.canGetSubmission(submission.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", submission));
        when(entityToJsonConverter.getSubmissionJson(submission)).thenReturn(submissionJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(submissionJson)));

        /* User can't get submission */
        when(submissionUtil.canGetSubmission(submission.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testGetSubmissions() throws Exception {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + submission.getProjectId() + "/submissions";
        /* all checks succeed */
        when(projectUtil.isProjectAdmin(submission.getProjectId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(projectRepository.findGroupIdsByProjectId(submission.getProjectId())).thenReturn(groupIds);
        when(groupRepository.findById(groupIds.get(0))).thenReturn(Optional.of(groupEntity));
        when(entityToJsonConverter.groupEntityToJson(groupEntity, false)).thenReturn(groupJson);
        when(groupFeedbackRepository.getGroupFeedback(groupEntity.getId(), submission.getProjectId())).thenReturn(groupFeedbackEntity);
        when(entityToJsonConverter.groupFeedbackEntityToJson(groupFeedbackEntity)).thenReturn(groupFeedbackJson);
        when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(submission.getProjectId(), groupEntity.getId())).thenReturn(Optional.of(submission));
        when(entityToJsonConverter.getSubmissionJson(submission)).thenReturn(submissionJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(lastGroupSubmissionJson))));

        verify(entityToJsonConverter, times(1)).groupEntityToJson(groupEntity, false);

        /* no submission */
        when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(submission.getProjectId(), groupEntity.getId())).thenReturn(Optional.empty());
        lastGroupSubmissionJson.setSubmission(null);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(lastGroupSubmissionJson))));

        /* no feedback */
        when(groupFeedbackRepository.getGroupFeedback(groupEntity.getId(), submission.getProjectId())).thenReturn(null);
        lastGroupSubmissionJson.setFeedback(null);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(lastGroupSubmissionJson))));

        /* Unexpected error */
        when(projectUtil.isProjectAdmin(submission.getProjectId(), getMockUser())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isInternalServerError());

        /* group not found */
        reset(projectUtil);
        when(projectUtil.isProjectAdmin(submission.getProjectId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(groupRepository.findById(groupIds.get(0))).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isInternalServerError());

        /* User can't get project */
        when(projectUtil.isProjectAdmin(submission.getProjectId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isIAmATeapot());

    }

    @Test
    public void testSubmitFile() throws Exception {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + submission.getProjectId() + "/submit";
        /* all checks succeed */
        when(submissionUtil.checkOnSubmit(submission.getProjectId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity.getId()));
        when(fileRepository.save(argThat(
            file -> file.getUploadedBy() == getMockUser().getId()
        ))).thenReturn(fileEntity);
        when(submissionRepository.save(argThat(
            sub -> {
                Duration duration = Duration.between(sub.getSubmissionTime(), OffsetDateTime.now());
                return sub.getProjectId() == submission.getProjectId() &&
                    sub.getGroupId() == groupEntity.getId() &&
                    sub.getFileId() == fileEntity.getId() &&
                    duration.getSeconds() < 2;
            }
        ))).thenReturn(submission);
        Path path = Path.of(fileEntity.getPath());
        Path artifactPath = Path.of("artifactPath");
        File file = createTestFile();
        try (MockedStatic<Filehandler> mockedFileHandler = mockStatic(Filehandler.class)) {
            mockedFileHandler.when(() -> Filehandler.getSubmissionPath(submission.getProjectId(), groupEntity.getId(), submission.getId())).thenReturn(path);
            mockedFileHandler.when(() -> Filehandler.saveSubmission(path, mockMultipartFile)).thenReturn(file);
            mockedFileHandler.when(() -> Filehandler.getSubmissionArtifactPath(anyLong(), anyLong(), anyLong())).thenReturn(artifactPath);

            when(testRunner.runStructureTest(any(), eq(testEntity), any())).thenReturn(null);
            when(testRunner.runDockerTest(any(), eq(testEntity), eq(artifactPath), any())).thenReturn(null);

            when(entityToJsonConverter.getSubmissionJson(submission)).thenReturn(submissionJson);

            when(testRepository.findByProjectId(submission.getProjectId())).thenReturn(Optional.of(testEntity));
            when(entityToJsonConverter.getSubmissionJson(submission)).thenReturn(submissionJson);

            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                    .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(submissionJson)));

            /* assertEquals(DockerTestState.running, submission.getDockerTestState()); */ // This executes too quickly so we can't test this

            Thread.sleep(2000);

            // File repository needs to save again after setting path
            verify(fileRepository, times(1)).save(argThat(
                f -> f.getId() == fileEntity.getId() && f.getPath().equals(fileEntity.getPath())
            ));

            // Submissions should be update 3 times, once for the initial save, once for structuretest, once for docker test.
            // The first one is being checked by the when(...)
            verify(submissionRepository, times(2)).save(argThat(
                s -> s.getId() == submission.getId()
            ));

            assertEquals(DockerTestState.aborted, submission.getDockerTestState());

            /* structuretestResult isn't null */
            submission.setStructureAccepted(false);
            submission.setStructureFeedback("");
            SubmissionResult submissionResult = new SubmissionResult(true, "structureFeedback-test");
            when(testRunner.runStructureTest(any(), eq(testEntity), any())).thenReturn(submissionResult);
            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                    .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(submissionJson)));

            assertTrue(submission.getStructureAccepted());
            assertEquals("structureFeedback-test", submission.getStructureFeedback());

            /* Correctly updates the dockertype */
            testEntity.setDockerTestTemplate("dockerTestTemplate");
            testEntity.setDockerTestScript("dockerTestScript");
            submission.setDockerType(DockerTestType.NONE);
            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                    .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(submissionJson)));
            assertEquals(DockerTestType.TEMPLATE, submission.getDockerTestType());

            testEntity.setDockerTestTemplate(null);
            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                    .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(submissionJson)));
            assertEquals(DockerTestType.SIMPLE, submission.getDockerTestType());

            testEntity.setDockerTestScript(null);
            testEntity.setDockerTestTemplate(null);
            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                    .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(submissionJson)));
            assertEquals(DockerTestType.NONE, submission.getDockerTestType());

            /* A valid docker result is returned */
            testEntity.setDockerImage("dockerImage");
            testEntity.setDockerTestScript("dockerTestScript");
            DockerOutput dockerOutput = new DockerTestOutput( List.of("dockerFeedback-test"), true);
            when(testRunner.runDockerTest(any(), eq(testEntity), eq(artifactPath), any())).thenReturn(dockerOutput);
            submission.setDockerAccepted(false);
            submission.setDockerFeedback("dockerFeedback-test");
            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                    .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(submissionJson)));

            Thread.sleep(1000);

            assertTrue(submission.getDockerAccepted());
            assertEquals("dockerFeedback-test", submission.getDockerFeedback());
            assertEquals(DockerTestState.finished, submission.getDockerTestState());

            /* No testEntity */
            when(testRepository.findByProjectId(submission.getProjectId())).thenReturn(Optional.empty());
            reset(testRunner);
            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                    .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(submissionJson)));
            verify(testRunner, times(0)).runStructureTest(any(), eq(testEntity), any());
            verify(testRunner, times(0)).runDockerTest(any(), eq(testEntity), eq(artifactPath), any());

            /* Unexpected error */
            reset(fileRepository);
            when(fileRepository.save(any())).thenThrow(new RuntimeException());
            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                    .file(mockMultipartFile))
                .andExpect(status().isInternalServerError());

            /* CheckOnSUbmit fails */
            when(submissionUtil.checkOnSubmit(submission.getProjectId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                    .file(mockMultipartFile))
                .andExpect(status().isIAmATeapot());



        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    @Test
    public void testGetSubmissionFile() throws Exception {
        try (MockedStatic<Filehandler> mockedFileHandler = mockStatic(Filehandler.class)) {
            String url = ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId() + "/file";
            Path path = Path.of(fileEntity.getPath());
            File file = createTestFile();
            Resource mockedResource = new FileSystemResource(file);

            /* all checks succeed */
            when(submissionUtil.canGetSubmission(submission.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", submission));
            when(fileRepository.findById(submission.getFileId())).thenReturn(Optional.of(fileEntity));
            mockedFileHandler.when(() -> Filehandler.getFileAsResource(path)).thenReturn(mockedResource);

            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string(
                    HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileEntity.getName()))
                .andExpect(content().bytes(mockedResource.getInputStream().readAllBytes()));

            /* Resource not found */
            mockedFileHandler.when(() -> Filehandler.getFileAsResource(path)).thenReturn(null);
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isNotFound());

            /* file not found */
            when(fileRepository.findById(submission.getFileId())).thenReturn(Optional.empty());
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isNotFound());

            /* Unexpected error */
            when(fileRepository.findById(submission.getFileId())).thenReturn(Optional.of(fileEntity));
            mockedFileHandler.reset();
            mockedFileHandler.when(() -> Filehandler.getFileAsResource(path)).thenThrow(new RuntimeException());
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isInternalServerError());

            /* User can't get submission */
            when(submissionUtil.canGetSubmission(submission.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isIAmATeapot());
        }
    }

    @Test
    public void testGetSubmissionArtifacts() throws Exception {
        try (MockedStatic<Filehandler> mockedFileHandler = mockStatic(Filehandler.class)) {
            String url = ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId() + "/artifacts";
            Path path = Path.of("artifactPath");
            File file = createTestFile();
            Resource mockedResource = new FileSystemResource(file);

            /* all checks succeed */
            when(submissionUtil.canGetSubmission(submission.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", submission));
            mockedFileHandler.when(() -> Filehandler.getSubmissionArtifactPath(submission.getProjectId(), submission.getGroupId(), submission.getId())).thenReturn(path);
            mockedFileHandler.when(() -> Filehandler.getFileAsResource(path)).thenReturn(mockedResource);

            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string(
                    HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=artifacts.zip"))
                .andExpect(content().bytes(mockedResource.getInputStream().readAllBytes()));


            /* Resource not found */
            mockedFileHandler.when(() -> Filehandler.getFileAsResource(path)).thenReturn(null);
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isNotFound());

            /* Unexpected error */
            mockedFileHandler.reset();
            mockedFileHandler.when(() -> Filehandler.getSubmissionArtifactPath(submission.getProjectId(), submission.getGroupId(), submission.getId())).thenThrow(new RuntimeException());
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isInternalServerError());

            /* User can't get submission */
            when(submissionUtil.canGetSubmission(submission.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isIAmATeapot());
        }
    }

    @Test
    public void testDeleteSubmissionById() throws Exception {
        String url = ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId();
        /* all checks succeed */
        when(submissionUtil.canDeleteSubmission(submission.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", submission));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isOk());

        verify(commonDatabaseActions, times(1)).deleteSubmissionById(submission.getId());

        /* User can't delete submission */
        when(submissionUtil.canDeleteSubmission(submission.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testGetSubmissionsForGroup() throws Exception {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + submission.getProjectId() + "/submissions/" + groupEntity.getId();
        /* all checks succeed */
        when(groupUtil.canGetProjectGroupData(groupEntity.getId(), submission.getProjectId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(submissionRepository.findByProjectIdAndGroupId(submission.getProjectId(), groupEntity.getId())).thenReturn(List.of(submission));
        when(entityToJsonConverter.getSubmissionJson(submission)).thenReturn(submissionJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(submissionJson))));

        /* No submissions */
        when(submissionRepository.findByProjectIdAndGroupId(submission.getProjectId(), groupEntity.getId())).thenReturn(List.of());
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        /* User can't get group */
        when(groupUtil.canGetProjectGroupData(groupEntity.getId(), submission.getProjectId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testGetAdminSubmissions() {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + submission.getProjectId() + "/adminsubmissions";

        /* all checks succeed */
        when(projectUtil.isProjectAdmin(submission.getProjectId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(submissionRepository.findAdminSubmissionsByProjectId(submission.getProjectId()))
            .thenReturn(List.of(submission));
        when(entityToJsonConverter.getSubmissionJson(submission)).thenReturn(submissionJson);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(submissionJson))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* No submissions */
        when(submissionRepository.findAdminSubmissionsByProjectId(submission.getProjectId()))
            .thenReturn(List.of());

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* User can't get project */
        when(projectUtil.isProjectAdmin(submission.getProjectId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isIAmATeapot());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}