package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
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

import java.sql.Timestamp;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class SubmissionControllerTest extends ControllerTest {
    @Mock
    private SubmissionRepository submissionRepository;

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
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void submitFileReturnsOkWhenUserHasAccessToProject() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.PROJECT_BASE_PATH + "/1/submit")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("file", "testFile")
                        .param("submissionTime", new Timestamp(System.currentTimeMillis()).toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getSubmissionFileReturnsOkWhenSubmissionExistsAndUserHasAccess() throws Exception {
        when(submissionRepository.findById(anyLong())).thenReturn(Optional.of(new SubmissionEntity()));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1/file"))
                .andExpect(status().isOk());
    }
}
