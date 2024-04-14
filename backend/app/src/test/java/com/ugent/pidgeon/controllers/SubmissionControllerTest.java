package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.repository.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class SubmissionControllerTest extends ControllerTest {
    @Mock
    private SubmissionRepository submissionRepository;


    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private TestRepository testRepository;

    @InjectMocks
    private SubmissionController submissionController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(submissionController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();

    }

    @Test
    public void getSubmissionReturnsOkWhenSubmissionExistsAndUserHasAccess() throws Exception {
        when(submissionRepository.findById(anyLong())).thenReturn(Optional.of(new SubmissionEntity()));
        when(groupRepository.userInGroup(anyLong(), anyLong())).thenReturn(true);
        when(projectRepository.adminOfProject(anyLong(), anyLong())).thenReturn(true);




        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1"))
                .andExpect(status().isOk());
    }

//    @Test
//    public void submitFileReturnsOkWhenUserHasAccessToProject() throws Exception {
//
//        SubmissionEntity submission = new SubmissionEntity();
//        submission.setProjectId(1L);
//
//        FileEntity file = new FileEntity();
//        file.setPath("testDir");
//        file.setName("testFile");
//
//
//        when(fileRepository.save(any())).thenReturn(file);
//        when(submissionRepository.save(any())).thenReturn(submission);
//        when(testRepository.findByProjectId(anyLong())).thenReturn(null);
//
//        MultipartFile multipartFile =  new MockMultipartFile("file", "testFile.txt", "text/plain", "test data".getBytes());
//
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.PROJECT_BASE_PATH + "/1/submit")
//                        .contentType(MediaType.MULTIPART_FORM_DATA)
//                        .param("file", multipartFile)
//                        .param("submissionTime", new Timestamp(System.currentTimeMillis()).toString()))
//                .andExpect(status().isOk());
//    }

    @Test
    public void getSubmissionFileReturnsOkWhenSubmissionExistsAndUserHasAccess() throws Exception {

        when(submissionRepository.findById(anyLong())).thenReturn(Optional.of(new SubmissionEntity()));
        when(groupRepository.userInGroup(anyLong(), anyLong())).thenReturn(true);
        when(projectRepository.adminOfProject(anyLong(), anyLong())).thenReturn(true);


        SubmissionEntity submission = new SubmissionEntity();
        submission.setFileId(1L);


        when(submissionRepository.findById(anyLong())).thenReturn(Optional.of(submission));

        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1"))
                .andExpect(status().isOk());
    }
}
