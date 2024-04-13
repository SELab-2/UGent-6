package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import com.ugent.pidgeon.util.Filehandler;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class TestControllerTest extends ControllerTest{
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private TestRepository testRepository;
    @InjectMocks
    private ControllerTest testController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
    }

//    @Test
//    public void updateTestsReturnsOkWhenUserHasAccessToProject() throws Exception {
//        when(projectRepository.userPartOfProject(anyLong(), anyLong())).thenReturn(true);
//        when(testRepository.findByProjectId(anyLong())).thenReturn(Optional.empty());
//        when(projectRepository.save(any())).thenReturn(new GroupEntity());
//        when(testRepository.save(any())).thenReturn(new GroupEntity());
//        when(fileRepository.save(any())).thenReturn(new GroupEntity());
//
//
//        Filehandler filehandler = mock(Filehandler.class);
//
//        // Define the path that should be returned
//                Path mockPath = Paths.get("mock/path");
//
//        // Specify what should be returned when saveTest is called
//                when(filehandler.saveTest(any(MultipartFile.class), anyLong())).thenReturn(mockPath);
//
//
//        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.PROJECT_BASE_PATH + "/1/tests")
//                        .contentType(MediaType.MULTIPART_FORM_DATA)
//                        .param("dockerimage", "dockerImage")
//                        .param("dockertest", "dockerTest")
//                        .param("structuretest", "structureTest"))
//                .andExpect(status().isOk());
//    }

//    @Test
//    public void updateTestsReturnsForbiddenWhenUserDoesNotHaveAccessToProject() throws Exception {
//        when(projectRepository.userPartOfProject(anyLong(), anyLong())).thenReturn(false);
//
//        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.PROJECT_BASE_PATH + "/1/tests")
//                        .contentType(MediaType.MULTIPART_FORM_DATA)
//                        .param("dockerimage", "dockerImage")
//                        .param("dockertest", "dockerTest")
//                        .param("structuretest", "structureTest"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    public void updateTestsReturnsInternalServerErrorWhenErrorOccursWhileSavingFiles() throws Exception {
//        when(projectRepository.userPartOfProject(anyLong(), anyLong())).thenReturn(true);
//        when(fileRepository.save(any())).thenThrow(new IOException());
//
//        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.PROJECT_BASE_PATH + "/1/tests")
//                        .contentType(MediaType.MULTIPART_FORM_DATA)
//                        .param("dockerimage", "dockerImage")
//                        .param("dockertest", "dockerTest")
//                        .param("structuretest", "structureTest"))
//                .andExpect(status().isInternalServerError());
//    }
}
