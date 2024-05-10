package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.Filehandler;
import com.ugent.pidgeon.util.Pair;
import com.ugent.pidgeon.util.ProjectUtil;
import com.ugent.pidgeon.util.TestUtil;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private EntityToJsonConverter entityToJsonConverter;


    @InjectMocks
    private TestController testController;


    private ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();

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
            test.getStructureTemplate()
        );

        when(testRepository.imageIsUsed(any())).thenReturn(true);
    }

    @Test
    public void testUpdateTest() throws Exception {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests";
        String dockerImage = "dockerImage";
        String dockerTestScript = "dockerTestScript";
        String dockerTestTemplate = "dockerTestTemplate";
        String structureTemplate = "structureTemplate";

        TestJson testJson = new TestJson(
            "projectUrl",
            dockerImage,
            dockerTestScript,
            dockerTestTemplate,
            structureTemplate
        );
        /* All checks succeed */
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(dockerImage),
            eq(dockerTestScript),
            eq(dockerTestTemplate),
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
            .param("dockerimage", dockerImage)
            .param("dockerscript", dockerTestScript)
            .param("dockertemplate", dockerTestTemplate)
            .param("structuretest", structureTemplate)
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
            null
        );
        reset(testUtil);
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
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
            .param("dockerimage", dockerImageBlank)
            .param("dockerscript", dockerTestScriptBlank)
            .param("dockertemplate", dockerTemplateBlank)
            .param("structuretest", structureTemplateBlank)
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(testJson)));

        /* Fields are null */
        String dockerImageNull = null;
        String dockerTestScriptNull = null;
        String dockerTemplateNull = null;
        String structureTemplateNull = null;

        mockMvc.perform(MockMvcRequestBuilders.post(url)
            .param("dockerimage", dockerImageNull)
            .param("dockerscript", dockerTestScriptNull)
            .param("dockertemplate", dockerTemplateNull)
            .param("structuretest", structureTemplateNull)
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
            eq(HttpMethod.POST)
        )).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        mockMvc.perform(MockMvcRequestBuilders.post(url)
            .param("dockerimage", dockerImage)
            .param("dockerscript", dockerTestScript)
            .param("dockertemplate", dockerTestTemplate)
            .param("structuretest", structureTemplate)
        ).andExpect(status().isIAmATeapot());
    }

    @Test
    public void testPutTest() throws Exception {
        String url = ApiRoutes.PROJECT_BASE_PATH + "/" + project.getId() + "/tests";

        String originalDockerImage = test.getDockerImage();
        String originalDockerTestScript = test.getDockerTestScript();
        String originalDockerTestTemplate = test.getDockerTestTemplate();
        String originalStructureTemplate = test.getStructureTemplate();

        String dockerImage = "dockerImage";
        String dockerTestScript = "dockerTestScript";
        String dockerTestTemplate = "dockerTestTemplate";
        String structureTemplate = "structureTemplate";

        test.setDockerImage(null);
        test.setDockerTestScript(null);
        test.setDockerTestTemplate(null);

        TestJson testJson = new TestJson(
            "projectUrl",
            dockerImage,
            dockerTestScript,
            dockerTestTemplate,
            structureTemplate
        );
        /* All checks succeed */
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
            eq(dockerImage),
            eq(dockerTestScript),
            eq(dockerTestTemplate),
            eq(HttpMethod.PUT)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        when(testRepository.save(test)).thenReturn(test);

        when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

        mockMvc.perform(MockMvcRequestBuilders.put(url)
            .param("dockerimage", dockerImage)
            .param("dockerscript", dockerTestScript)
            .param("dockertemplate", dockerTestTemplate)
            .param("structuretest", structureTemplate)
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

        testJson = new TestJson(
            "projectUrl",
            null,
            null,
            null,
            null
        );
        reset(testUtil);
        when(testUtil.checkForTestUpdate(
            eq(project.getId()),
            eq(getMockUser()),
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
            .param("dockerimage", dockerImageBlank)
            .param("dockerscript", dockerTestScriptBlank)
            .param("dockertemplate", dockerTemplateBlank)
            .param("structuretest", structureTemplateBlank)
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

        mockMvc.perform(MockMvcRequestBuilders.put(url)
            .param("dockerimage", dockerImageNull)
            .param("dockerscript", dockerTestScriptNull)
            .param("dockertemplate", dockerTemplateNull)
            .param("structuretest", structureTemplateNull)
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
            eq(HttpMethod.PUT)
        )).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        mockMvc.perform(MockMvcRequestBuilders.put(url)
            .param("dockerimage", dockerImage)
            .param("dockerscript", dockerTestScript)
            .param("dockertemplate", dockerTestTemplate)
            .param("structuretest", structureTemplate)
        ).andExpect(status().isIAmATeapot());

    }

    @Test
    public void testGetPatch() throws Exception {
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
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        when(testRepository.save(test)).thenReturn(test);
        when(entityToJsonConverter.testEntityToTestJson(test, project.getId())).thenReturn(testJson);

        /* Start with test all null, fill them in one by one */
        test.setDockerImage(null);
        test.setDockerTestScript(null);
        test.setDockerTestTemplate(null);
        test.setStructureTemplate(null);

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .param("dockerimage", dockerImage)
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
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));


        mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .param("dockerscript", dockerTestScript)
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
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .param("dockertemplate", dockerTestTemplate)
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
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "",new Pair<>(test, project)));

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .param("structuretest", structureTemplate)
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
            eq(HttpMethod.PATCH)
        )).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null));

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .param("dockerimage", dockerImage)
        ).andExpect(status().isIAmATeapot());

    }
}
