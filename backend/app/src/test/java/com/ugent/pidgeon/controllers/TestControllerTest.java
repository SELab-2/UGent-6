package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.model.json.TestUpdateJson;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.CommonDatabaseActions;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.FileUtil;
import com.ugent.pidgeon.util.Filehandler;
import com.ugent.pidgeon.util.Pair;
import com.ugent.pidgeon.util.ProjectUtil;
import com.ugent.pidgeon.util.TestUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.time.OffsetDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TestControllerTest extends ControllerTest{
    @Mock
    private TestUtil testUtil;

    @Mock
    private TestRepository testRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private FileRepository fileRepository;

    @Mock
    private EntityToJsonConverter entityToJsonConverter;
    @Mock
    private CommonDatabaseActions commonDatabaseActions;

    @Mock
    private FileUtil fileUtil;


    @InjectMocks
    private TestController testController;


    private ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();

    private MockMultipartFile mockMultipartFile;
    private FileEntity fileEntity;
    private ProjectEntity project;
    private TestEntity test;
    private TestJson testJson;

    @BeforeEach
    public void setup() {
        setUpController(testController);

        project = new ProjectEntity(
            67,
            "projectName",
            "projectDescription",
            5,
            38L,
            true,
            34,
            OffsetDateTime.now()
        );
        project.setId(64);

        test = new TestEntity(
            "dockerImageBasic",
            "dockerTestScriptBasic",
            "dockerTestTemplateBasic",
            "structureTemplateBasic"
        );
        test.setId(990);
        testJson = new TestJson(
            "projectUrl",
            test.getDockerImage(),
            test.getDockerTestScript(),
            test.getDockerTestTemplate(),
            test.getStructureTemplate(),
            "extraFilesUrl",
            "extraFilesName"

        );

        byte[] fileContent = "Your file content".getBytes();
        mockMultipartFile = new MockMultipartFile("file", "filename.txt",
            MediaType.TEXT_PLAIN_VALUE, fileContent);

        fileEntity = new FileEntity("name", "dir/name", 1L);
        fileEntity.setId(77L);
    }

    @Test
    public void testUpdateTest() throws Exception {
        when(testRepository.imageIsUsed(any())).thenReturn(true);
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests";
        String dockerImage = "dockerImage";
        String dockerTestScript = "dockerTestScript";
        String dockerTestTemplate = "dockerTestTemplate";
        String structureTemplate = "structureTemplate";

        TestUpdateJson testUpdateJson = new TestUpdateJson(
            dockerImage,
            dockerTestScript,
            dockerTestTemplate,
            structureTemplate
        );

        TestJson testJson = new TestJson(
            "projectUrl",
            dockerImage,
            dockerTestScript,
            dockerTestTemplate,
            structureTemplate,
            "extraFilesUrl",
            "extraFilesName"
        );
        /* All checks succeed */
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(dockerImage),
            eq(dockerTestScript),
            eq(dockerTestTemplate),
            eq(structureTemplate),
            eq(HttpMethod.POST)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(null, project)));

        when(testRepository.save(argThat(
            testEntity -> testEntity.getDockerImage().equals(dockerImage) &&
                testEntity.getDockerTestScript().equals(dockerTestScript) &&
                testEntity.getDockerTestTemplate().equals(dockerTestTemplate) &&
                testEntity.getStructureTemplate().equals(structureTemplate)
        ))).thenReturn(test);

        when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateJson))
            ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        verify(projectRepository, times(1)).save(project);
        assertEquals(test.getId(), project.getTestId());

        /* fields are blank */
        String dockerImageBlank = "";
        String dockerTestScriptBlank = "";
        String dockerTemplateBlank = "";
        String structureTemplateBlank = "";

        testJson = new TestJson(
            "projectUrl",
            null,
            null,
            null,
            null,
            "extraFilesUrl",
            "extraFilesName"
        );
        testUpdateJson = new TestUpdateJson(
            dockerImageBlank,
            dockerTestScriptBlank,
            dockerTemplateBlank,
            structureTemplateBlank
        );
        reset(testUtil);
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(HttpMethod.POST)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(null, project)));

        reset(testRepository);
        when(testRepository.save(argThat(
            testEntity -> testEntity.getDockerImage() == null &&
                testEntity.getDockerTestScript() == null &&
                testEntity.getDockerTestTemplate() == null &&
                testEntity.getStructureTemplate() == null
        ))).thenReturn(test);

        when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateJson))
            ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        /* Fields are null */
        String dockerImageNull = null;
        String dockerTestScriptNull = null;
        String dockerTemplateNull = null;
        String structureTemplateNull = null;

        testUpdateJson = new TestUpdateJson(
            dockerImageNull,
            dockerTestScriptNull,
            dockerTemplateNull,
            structureTemplateNull
        );

        when(testRepository.imageIsUsed(any())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateJson))
            ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        /* Check fails */
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(dockerImage),
            eq(dockerTestScript),
            eq(dockerTestTemplate),
            eq(structureTemplate),
            eq(HttpMethod.POST)
        )).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        testUpdateJson = new TestUpdateJson(
            dockerImage,
            dockerTestScript,
            dockerTestTemplate,
            structureTemplate
        );

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateJson))
        ).andExpect(status().isIAmATeapot());
    }

    @Test
    public void testPutTest() throws Exception {
        when(testRepository.imageIsUsed(any())).thenReturn(true);
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests";

        String originalDockerImage = test.getDockerImage();
        String originalDockerTestScript = test.getDockerTestScript();
        String originalDockerTestTemplate = test.getDockerTestTemplate();
        String originalStructureTemplate = test.getStructureTemplate();

        String dockerImage = "dockerImage";
        String dockerTestScript = "dockerTestScript";
        String dockerTestTemplate = "dockerTestTemplate";
        String structureTemplate = "structureTemplate";

        TestUpdateJson testUpdateJson = new TestUpdateJson(
            dockerImage,
            dockerTestScript,
            dockerTestTemplate,
            structureTemplate
        );

        test.setDockerImage(null);
        test.setDockerTestScript(null);
        test.setDockerTestTemplate(null);

        TestJson testJson = new TestJson(
            "projectUrl",
            dockerImage,
            dockerTestScript,
            dockerTestTemplate,
            structureTemplate,
            "extraFilesUrl",
            "extraFilesName"
        );
        /* All checks succeed */
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(dockerImage),
            eq(dockerTestScript),
            eq(dockerTestTemplate),
            eq(structureTemplate),
            eq(HttpMethod.PUT)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        when(testRepository.save(test)).thenReturn(test);

        when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

        mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateJson))
            ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        verify(projectRepository, times(1)).save(project);
        assertEquals(test.getId(), project.getTestId());
        assertEquals(dockerImage, test.getDockerImage());
        assertEquals(dockerTestScript, test.getDockerTestScript());
        assertEquals(dockerTestTemplate, test.getDockerTestTemplate());
        assertEquals(structureTemplate, test.getStructureTemplate());

        test.setDockerImage(originalDockerImage);
        test.setDockerTestScript(originalDockerTestScript);
        test.setDockerTestTemplate(originalDockerTestTemplate);

        /* fields are blank */
        String dockerImageBlank = "";
        String dockerTestScriptBlank = "";
        String dockerTemplateBlank = "";
        String structureTemplateBlank = "";

        testUpdateJson = new TestUpdateJson(
            dockerImageBlank,
            dockerTestScriptBlank,
            dockerTemplateBlank,
            structureTemplateBlank
        );

        testJson = new TestJson(
            "projectUrl",
            null,
            null,
            null,
            null,
            "extraFilesUrl",
            "extraFilesName"
        );
        reset(testUtil);
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(HttpMethod.PUT)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        reset(testRepository);
        when(testRepository.save(argThat(
            testEntity -> testEntity.getDockerImage() == null &&
                testEntity.getDockerTestScript() == null &&
                testEntity.getDockerTestTemplate() == null &&
                testEntity.getStructureTemplate() == null
        ))).thenReturn(test);

        when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

        mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateJson))
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        assertNull(test.getDockerImage());
        assertNull(test.getDockerTestScript());
        assertNull(test.getDockerTestTemplate());
        assertNull(test.getStructureTemplate());

        test.setDockerImage(originalDockerImage);
        test.setDockerTestScript(originalDockerTestScript);
        test.setDockerTestTemplate(originalDockerTestTemplate);
        test.setStructureTemplate(originalStructureTemplate);

        /* Fields are null */
        String dockerImageNull = null;
        String dockerTestScriptNull = null;
        String dockerTemplateNull = null;
        String structureTemplateNull = null;

        when(testRepository.imageIsUsed(any())).thenReturn(true);

        testUpdateJson = new TestUpdateJson(
            dockerImageNull,
            dockerTestScriptNull,
            dockerTemplateNull,
            structureTemplateNull
        );

        mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateJson))
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        assertNull(test.getDockerImage());
        assertNull(test.getDockerTestScript());
        assertNull(test.getDockerTestTemplate());
        assertNull(test.getStructureTemplate());

        test.setDockerImage(originalDockerImage);
        test.setDockerTestScript(originalDockerTestScript);
        test.setDockerTestTemplate(originalDockerTestTemplate);
        test.setStructureTemplate(originalStructureTemplate);

        /* Check fails */
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(dockerImage),
            eq(dockerTestScript),
            eq(dockerTestTemplate),
            eq(structureTemplate),
            eq(HttpMethod.PUT)
        )).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        testUpdateJson = new TestUpdateJson(
            dockerImage,
            dockerTestScript,
            dockerTestTemplate,
            structureTemplate
        );

        mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateJson))
        ).andExpect(status().isIAmATeapot());

    }

    @Test
    public void testGetPatch() throws Exception {
        when(testRepository.imageIsUsed(any())).thenReturn(true);
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests";

        String dockerImage = "dockerImage";
        String dockerTestScript = "dockerTestScript";
        String dockerTestTemplate = "dockerTestTemplate";
        String structureTemplate = "structureTemplate";

        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(dockerImage),
            eq(null),
            eq(null),
            eq(null),
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        when(testRepository.save(test)).thenReturn(test);
        when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

        /* Start with test all null, fill them in one by one */
        test.setDockerImage(null);
        test.setDockerTestScript(null);
        test.setDockerTestTemplate(null);
        test.setStructureTemplate(null);

        TestUpdateJson testUpdateJson = new TestUpdateJson(
            dockerImage,
            null,
            null,
            null
        );

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUpdateJson))
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        assertEquals(dockerImage, test.getDockerImage());
        assertNull(test.getDockerTestScript());
        assertNull(test.getDockerTestTemplate());
        assertNull(test.getStructureTemplate());

        verify(projectRepository, times(1)).save(project);
        assertEquals(test.getId(), project.getTestId());

        reset(testUtil);
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(null),
            eq(dockerTestScript),
            eq(null),
            eq(null),
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        testUpdateJson = new TestUpdateJson(
            null,
            dockerTestScript,
            null,
            null
        );

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateJson))
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        assertEquals(dockerImage, test.getDockerImage());
        assertEquals(dockerTestScript, test.getDockerTestScript());
        assertNull(test.getDockerTestTemplate());
        assertNull(test.getStructureTemplate());

        verify(projectRepository, times(2)).save(project);
        assertEquals(test.getId(), project.getTestId());

        reset(testUtil);
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(null),
            eq(null),
            eq(dockerTestTemplate),
            eq(null),
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        testUpdateJson = new TestUpdateJson(
            null,
            null,
            dockerTestTemplate,
            null
        );

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateJson))
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        assertEquals(dockerImage, test.getDockerImage());
        assertEquals(dockerTestScript, test.getDockerTestScript());
        assertEquals(dockerTestTemplate, test.getDockerTestTemplate());
        assertNull(test.getStructureTemplate());

        verify(projectRepository, times(3)).save(project);
        assertEquals(test.getId(), project.getTestId());

        reset(testUtil);
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(null),
            eq(null),
            eq(null),
            eq(structureTemplate),
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        testUpdateJson = new TestUpdateJson(
            null,
            null,
            null,
            structureTemplate
        );

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateJson))
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        assertEquals(dockerImage, test.getDockerImage());
        assertEquals(dockerTestScript, test.getDockerTestScript());
        assertEquals(dockerTestTemplate, test.getDockerTestTemplate());
        assertEquals(structureTemplate, test.getStructureTemplate());

        verify(projectRepository, times(4)).save(project);
        assertEquals(test.getId(), project.getTestId());

        /* Check fails */
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(dockerImage),
            eq(null),
            eq(null),
            eq(null),
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        testUpdateJson = new TestUpdateJson(
            dockerImage,
            null,
            null,
            null
        );

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUpdateJson))
        ).andExpect(status().isIAmATeapot());
    }

    @Test
    public void testGetTest() throws Exception {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests";

        /* All checks succeed */
        when(testUtil.getTestWithAdminStatus(project.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(test, true)));

        when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        /* Check succeed but user isn't admin */
        when(testUtil.getTestWithAdminStatus(project.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(test, false)));

        testJson.setDockerImage(null);
        testJson.setDockerScript(null);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        /* Check fails */
        when(testUtil.getTestWithAdminStatus(project.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        mockMvc.perform(MockMvcRequestBuilders.get(url));
    }

    @Test
    public void testDeleteTest() throws Exception {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests";

        /* All checks succeed */
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(HttpMethod.DELETE)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(test, project)));

        when(commonDatabaseActions.deleteTestById(project, test)).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isOk());

        /* Deleting fails */
        when(commonDatabaseActions.deleteTestById(project, test)).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isIAmATeapot());

        /* Check fails */
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(HttpMethod.DELETE)
        )).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isIAmATeapot());
    }

    public static File createTestFile() throws IOException {
        // Create a temporary directory
        File tempDir = Files.createTempDirectory("SELAB6CANDELETEtest-dir").toFile();

        // Create a temporary file within the directory
        File tempFile = File.createTempFile("SELAB6CANDELETEtest-file", ".zip", tempDir);

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

    @Test
    public void testUploadExtraTestFiles() throws IOException {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests/extrafiles";
        /* All checks succeed */
        when(testUtil.getTestIfAdmin(project.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", test));

        Path savePath = Path.of("savePath");
        File file = createTestFile();

        try (MockedStatic<Filehandler> mockedFilehandler = mockStatic(Filehandler.class)) {
            mockedFilehandler.when(() -> Filehandler.getTestExtraFilesPath(project.getId())).thenReturn(savePath);
            mockedFilehandler.when(() -> Filehandler.saveFile(savePath, mockMultipartFile, Filehandler.EXTRA_TESTFILES_FILENAME))
                .thenReturn(file);

            when(fileRepository.save(argThat(
                fileEntity -> fileEntity.getName().equals(mockMultipartFile.getOriginalFilename()) &&
                    fileEntity.getPath()
                        .equals(savePath.resolve(Filehandler.EXTRA_TESTFILES_FILENAME).toString()) &&
                    fileEntity.getUploadedBy() == getMockUser().getId()
            ))).thenReturn(fileEntity);

            when(testRepository.save(test)).thenReturn(test);
            when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                .file(mockMultipartFile)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
            ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

            verify(testRepository, times(1)).save(test);
            assertEquals(fileEntity.getId(), test.getExtraFilesId());

            /* Unexpected error */
            mockedFilehandler.when(() -> Filehandler.saveFile(savePath, mockMultipartFile, Filehandler.EXTRA_TESTFILES_FILENAME))
                .thenThrow(new IOException("Unexpected error"));

            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                .file(mockMultipartFile)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
            ).andExpect(status().isInternalServerError());

            /* Check fails */
            when(testUtil.getTestIfAdmin(project.getId(), getMockUser()))
                .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

            mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                .file(mockMultipartFile)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
            ).andExpect(status().isIAmATeapot());

        } catch (Exception e) {
          throw new RuntimeException(e);
        }
    }

    @Test
    public void testDeleteExtraFiles() throws Exception {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests/extrafiles";
        test.setExtraFilesId(fileEntity.getId());

        /* All checks succeed */
        when(testUtil.getTestIfAdmin(project.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", test));

        when(fileRepository.findById(test.getExtraFilesId())).thenReturn(Optional.of(fileEntity));
        when(testRepository.save(test)).thenReturn(test);
        when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

        when(fileUtil.deleteFileById(test.getExtraFilesId()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        verify(testRepository, times(1)).save(test);
        verify(fileUtil, times(1)).deleteFileById(fileEntity.getId());
        assertNull(test.getExtraFilesId());

        /* Unexpected error when deleting file */
        test.setExtraFilesId(fileEntity.getId());
        when(fileUtil.deleteFileById(test.getExtraFilesId()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "Unexpected error", null));

        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isIAmATeapot());

        /* Error thrown */
        test.setExtraFilesId(fileEntity.getId());
        when(fileUtil.deleteFileById(test.getExtraFilesId()))
            .thenThrow(new RuntimeException("Error thrown"));

        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isInternalServerError());

        /* No extra files */
        test.setExtraFilesId(null);

        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isNotFound());

        /* Check fails */
        when(testUtil.getTestIfAdmin(project.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isIAmATeapot());
    }

    @Test
    public void getExtraTestFiles() {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests/extrafiles";

        ResponseEntity<?> mockResponseEntity = ResponseEntity.ok().build();
        test.setExtraFilesId(fileEntity.getId());

        /* All checks succeed */
        when(testUtil.getTestIfAdmin(project.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", test));

        when(fileRepository.findById(test.getExtraFilesId())).thenReturn(Optional.of(fileEntity));

        try (MockedStatic<Filehandler> mockedFilehandler = mockStatic(Filehandler.class)) {
            mockedFilehandler.when(() -> Filehandler.getZipFileAsResponse(argThat(
                path -> path.toString().equals(fileEntity.getPath())
                ), eq(fileEntity.getName())))
                .thenReturn(mockResponseEntity);

            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk());

            /* Files not found */
            when(fileRepository.findById(test.getExtraFilesId())).thenReturn(Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isNotFound());

            /* No extra files */
            test.setExtraFilesId(null);

            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isNotFound());

            /* check fails */
            when(testUtil.getTestIfAdmin(project.getId(), getMockUser()))
                .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

            mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isIAmATeapot());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
