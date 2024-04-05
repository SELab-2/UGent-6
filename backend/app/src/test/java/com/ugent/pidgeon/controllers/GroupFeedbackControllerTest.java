package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class GroupFeedbackControllerTest extends ControllerTest {
    @Mock
    private GroupFeedbackRepository groupFeedbackRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private GroupFeedbackController groupFeedbackController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupFeedbackController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
    }

//    @Test
//    public void updateGroupScoreReturnsOkWhenGroupExistsAndUserHasAccess() throws Exception {
//        when(groupRepository.userAccessToGroup(anyLong(), anyLong())).thenReturn(true);
//        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(new ProjectEntity()));
//        when(groupFeedbackRepository.updateGroupScore(anyFloat(), anyLong(), anyLong(), anyString())).thenReturn(1);
//
//
//        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"score\": 5, \"feedback\": \"Good work\"}"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void addGroupScoreReturnsOkWhenGroupExistsAndUserHasAccess() throws Exception {
//        when(groupRepository.userAccessToGroup(anyLong(), anyLong())).thenReturn(true);
//        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(new ProjectEntity()));
//        when(groupFeedbackRepository.addGroupScore(anyFloat(), anyLong(), anyLong(), anyString())).thenReturn(1);
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"score\": 5, \"feedback\": \"Good work\"}"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void getGroupScoreReturnsOkWhenGroupExistsAndUserHasAccess() throws Exception {
//        GroupFeedbackEntity groupFeedbackEntity = new GroupFeedbackEntity();
//        groupFeedbackEntity.setScore(5.0f);
//        groupFeedbackEntity.setFeedback("Good work");
//        groupFeedbackEntity.setGroupId(1L);
//        groupFeedbackEntity.setProjectId(1L);
//
//        when(groupRepository.userInGroup(anyLong(), anyLong())).thenReturn(true);
//        when(groupFeedbackRepository.getGroupFeedback(anyLong(), anyLong())).thenReturn(groupFeedbackEntity);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1")))
//                .andExpect(status().isOk());
//    }
}

