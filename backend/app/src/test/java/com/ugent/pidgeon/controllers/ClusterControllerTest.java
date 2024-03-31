package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ClusterControllerTest extends ControllerTest {
    @Mock
    private GroupClusterRepository groupClusterRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private CourseUserRepository courseUserRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private GroupUserRepository groupUserRepository;
    @Mock
    private GroupController groupController;

    @InjectMocks
    private ClusterController clusterController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(clusterController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
    }

    @Test
    public void getClustersReturns404WhenCourseDoesNotExist() throws Exception {
        when(courseRepository.existsById(anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/clusters"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getClustersReturns403WhenUserIsNotInCourse() throws Exception {
        when(courseRepository.existsById(anyLong())).thenReturn(true);
        when(courseUserRepository.findByCourseIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/clusters"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getClustersReturns200WhenUserIsInCourse() throws Exception {
        when(courseRepository.existsById(anyLong())).thenReturn(true);
        when(courseUserRepository.findByCourseIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new CourseUserEntity()));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/clusters"))
                .andExpect(status().isOk());
    }

    @Test
    public void postClusterReturns404WhenCourseDoesNotExist() throws Exception {
        when(courseRepository.existsById(anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/clusters")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"test\", \"capacity\": 10, \"groupCount\": 1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void postClusterReturns403WhenUserIsNotAdminOfCourse() throws Exception {
        when(courseRepository.existsById(anyLong())).thenReturn(true);
        when(courseRepository.adminOfCourse(anyLong(), anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/clusters")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"test\", \"capacity\": 10, \"groupCount\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void postClusterReturns200WhenUserIsAdminOfCourse() throws Exception {
        when(courseRepository.existsById(anyLong())).thenReturn(true);
        when(courseRepository.adminOfCourse(anyLong(), anyLong())).thenReturn(true);
        GroupClusterEntity dummycreated = new GroupClusterEntity(1, 1, "", 1);
        when(groupClusterRepository.save(ArgumentMatchers.any())).thenReturn(dummycreated);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/clusters")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"test\", \"capacity\": 10, \"groupCount\": 1}"))
                .andExpect(status().isCreated());
    }
}
