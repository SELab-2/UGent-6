package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.*;
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

import java.util.List;
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

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseUserRepository courseUserRepository;

    @InjectMocks
    private GroupFeedbackController groupFeedbackController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupFeedbackController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
    }

    @Test
    public void updateGroupScoreReturnsOkWhenGroupExistsAndUserHasAccess() throws Exception {
        CourseEntity mockedCourse = new CourseEntity();
        mockedCourse.setId(1L);
        CourseUserEntity mockedCourseUser = new CourseUserEntity();
        mockedCourseUser.setRelation(CourseRelation.course_admin);
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setGroupClusterId(1L);
        projectEntity.setMaxScore(10);
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(projectEntity));
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setClusterId(1L);
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(groupEntity));
        when(groupRepository.isAdminOfGroup(anyLong(), anyLong())).thenReturn(true);
        when(groupFeedbackRepository.findById(any(GroupFeedbackId.class))).thenReturn(Optional.of(new GroupFeedbackEntity()));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\": 5, \"feedback\": \"Good work\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void addGroupScoreReturnsOkWhenGroupExistsAndUserHasAccess() throws Exception {

        CourseEntity mockedCourse = new CourseEntity();
        mockedCourse.setId(1L);
        CourseUserEntity mockedCourseUser = new CourseUserEntity();
        mockedCourseUser.setRelation(CourseRelation.course_admin);
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setGroupClusterId(1L);
        projectEntity.setMaxScore(10);
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(projectEntity));
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setClusterId(1L);
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(groupEntity));
        when(groupRepository.isAdminOfGroup(anyLong(), anyLong())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\": 5, \"feedback\": \"Good work\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void getGroupScoreReturnsOkWhenGroupExistsAndUserHasAccess() throws Exception {
        GroupFeedbackEntity groupFeedbackEntity = new GroupFeedbackEntity();
        groupFeedbackEntity.setScore(5.0f);
        groupFeedbackEntity.setFeedback("Good work");
        groupFeedbackEntity.setGroupId(1L);
        groupFeedbackEntity.setProjectId(1L);

        when(groupRepository.userInGroup(anyLong(), anyLong())).thenReturn(true);
        when(groupFeedbackRepository.getGroupFeedback(anyLong(), anyLong())).thenReturn(groupFeedbackEntity);
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setGroupClusterId(1L);
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(projectEntity));
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setClusterId(1L);
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(groupEntity));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.GROUP_FEEDBACK_PATH.replace("{groupid}", "1").replace("{projectid}", "1")))
                .andExpect(status().isOk());
    }
}

