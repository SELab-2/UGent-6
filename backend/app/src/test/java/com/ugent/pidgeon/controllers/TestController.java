package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class TestController extends ControllerTest{
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private TestRepository testRepository;

    @InjectMocks
    private TestController testController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController,testRepository)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
    }

    @Test
    public void updateTestsReturnsOkWhenUserHasAccessToProject() throws Exception {
        when(projectRepository.userPartOfProject(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.PROJECT_BASE_PATH + "/1/tests")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("dockerimage", "dockerImage")
                        .param("dockertest", "dockerTest")
                        .param("structuretest", "structureTest"))
                .andExpect(status().isOk());
    }

    @Test
    public void updateTestsReturnsForbiddenWhenUserDoesNotHaveAccessToProject() throws Exception {
        when(projectRepository.userPartOfProject(anyLong(), anyLong())).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.PROJECT_BASE_PATH + "/1/tests")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("dockerimage", "dockerImage")
                        .param("dockertest", "dockerTest")
                        .param("structuretest", "structureTest"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateTestsReturnsInternalServerErrorWhenErrorOccursWhileSavingFiles() throws Exception {
        when(projectRepository.userPartOfProject(anyLong(), anyLong())).thenReturn(true);
        when(fileRepository.save(any())).thenThrow(new IOException());

        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.PROJECT_BASE_PATH + "/1/tests")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("dockerimage", "dockerImage")
                        .param("dockertest", "dockerTest")
                        .param("structuretest", "structureTest"))
                .andExpect(status().isInternalServerError());
    }
}
