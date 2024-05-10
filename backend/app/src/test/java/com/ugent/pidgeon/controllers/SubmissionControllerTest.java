package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.json.DockerTestFeedbackJson;
import com.ugent.pidgeon.model.json.GroupFeedbackJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.LastGroupSubmissionJson;
import com.ugent.pidgeon.model.json.SubmissionJson;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.types.DockerTestState;
import com.ugent.pidgeon.postgre.models.types.DockerTestType;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
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


    private SubmissionEntity submission;
    private List<Long> groupIds;
    private SubmissionJson submissionJson;
    private GroupJson groupJson;
    private GroupFeedbackJson groupFeedbackJson;
    private GroupEntity groupEntity;
    private GroupFeedbackEntity groupFeedbackEntity;
    private MockMultipartFile mockMultipartFile;
    private FileEntity fileEntity;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(submissionController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> {
                            request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
                            return request;
                        }))
                .build();
        submission = new SubmissionEntity(1L, 1L, 1L, OffsetDateTime.MIN, true, true);
        groupIds = List.of(1L);
        submissionJson = new SubmissionJson(1L, "projecturl", "groupurl", 1L,
                1L, "fileurl", true, OffsetDateTime.MIN, "structureFeedback",
                new DockerTestFeedbackJson(DockerTestType.NONE, "", true), DockerTestState.running.toString(), "artifacturl");
        groupJson = new GroupJson(1, 1L, "groupname", "groupclusterurl");
        groupFeedbackJson = new GroupFeedbackJson(0F, "feedback", 1L, 1L);
        groupEntity = new GroupEntity("groupname", 1L);
        groupFeedbackEntity = new GroupFeedbackEntity(1L, 1L, 0F, "feedback");
        byte[] fileContent = "Your file content".getBytes();
        mockMultipartFile = new MockMultipartFile("file", "filename.txt", MediaType.TEXT_PLAIN_VALUE, fileContent);
        fileEntity = new FileEntity("name", "dir/name", 1L);
    }

    @Test
    public void testGetSubmission() throws Exception {
        when(submissionUtil.canGetSubmission(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", submission));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1"))
                .andExpect(status().isOk());

        when(submissionUtil.canGetSubmission(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1"))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testGetSubmissions() throws Exception {
        List<LastGroupSubmissionJson> lastGroupSubmissionJsons = List.of(new LastGroupSubmissionJson(submissionJson, groupJson, groupFeedbackJson));
        when(projectUtil.isProjectAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(projectRepository.findGroupIdsByProjectId(anyLong())).thenReturn(groupIds);
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(groupEntity));
        when(entityToJsonConverter.groupEntityToJson(any())).thenReturn(groupJson);
        when(groupFeedbackRepository.getGroupFeedback(anyLong(), anyLong())).thenReturn(groupFeedbackEntity);
        when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(anyLong(), anyLong())).thenReturn(Optional.of(submission));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.PROJECT_BASE_PATH + "/1/submissions"))
                .andExpect(status().isOk());

        when(submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(anyLong(), anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.PROJECT_BASE_PATH + "/1/submissions"))
                .andExpect(status().isOk());

        when(groupFeedbackRepository.getGroupFeedback(anyLong(), anyLong())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.PROJECT_BASE_PATH + "/1/submissions"))
                .andExpect(status().isOk());

        when(groupRepository.findById(anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.PROJECT_BASE_PATH + "/1/submissions"))
                .andExpect(status().isInternalServerError());

        when(projectUtil.isProjectAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.PROJECT_BASE_PATH + "/1/submissions"))
                .andExpect(status().isBadRequest());

        when(projectUtil.isProjectAdmin(anyLong(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.PROJECT_BASE_PATH + "/1/submissions"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testSubmitFile() throws Exception {
        //TODO: dit ook een correcte test laten uitvoeren met dummyfiles
        when(submissionUtil.checkOnSubmit(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", 1L));
        when(fileRepository.save(any())).thenReturn(fileEntity);
        when(submissionRepository.save(any())).thenReturn(submission);
        mockMvc.perform(MockMvcRequestBuilders.multipart(ApiRoutes.PROJECT_BASE_PATH + "/1/submit")
                        .file(mockMultipartFile))
                .andExpect(status().isInternalServerError());
    }

//    @Test
//    public void testGetSubmissionFile() throws Exception {
//        //TODO: dit ook een correcte test laten uitvoeren met dummyfiles
//        when(submissionUtil.canGetSubmission(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", submission));
//        when(fileRepository.findById(anyLong())).thenReturn(Optional.of(fileEntity));
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1/file"))
//                .andExpect(status().isInternalServerError());
//
//        when(fileRepository.findById(anyLong())).thenReturn(Optional.empty());
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1/file"))
//                .andExpect(status().isNotFound());
//
//        when(submissionUtil.canGetSubmission(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1/file"))
//                .andExpect(status().isForbidden());
//    }

//    @Test
//    public void testGetStructureFeedback() throws Exception {
//        when(submissionUtil.canGetSubmission(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", submission));
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1/structurefeedback"))
//                .andExpect(status().isOk());
//
//        when(submissionUtil.canGetSubmission(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1/structurefeedback"))
//                .andExpect(status().isIAmATeapot());
//    }

//    @Test
//    public void testGetDockerFeedback() throws Exception {
//        when(submissionUtil.canGetSubmission(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", submission));
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.SUBMISSION_BASE_PATH + "/1/dockerfeedback"))
//                .andExpect(status().isOk());
//    }

    @Test
    public void testDeleteSubmissionById() throws Exception {
        when(submissionUtil.canDeleteSubmission(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", submission));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.SUBMISSION_BASE_PATH + "/1"))
                .andExpect(status().isOk());

        when(submissionUtil.canDeleteSubmission(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.SUBMISSION_BASE_PATH + "/1"))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testGetSubmissionsForGroup() throws Exception {
        when(groupUtil.canGetProjectGroupData(anyLong(), anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.PROJECT_BASE_PATH + "/1/submissions/1"))
                .andExpect(status().isOk());

        when(groupUtil.canGetProjectGroupData(anyLong(), anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.PROJECT_BASE_PATH + "/1/submissions/1"))
                .andExpect(status().isIAmATeapot());
    }
}