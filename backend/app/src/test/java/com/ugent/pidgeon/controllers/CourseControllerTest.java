package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class CourseControllerTest extends ControllerTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private CourseController courseController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(courseController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
    }

    @Test
    public void getCourseByCourseIdReturnsCourseWhenCourseExists() throws Exception {
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(new CourseEntity()));

        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getCourseByCourseIdReturnsNotFoundWhenCourseDoesNotExist() throws Exception {
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getProjectByCourseIdReturnsProjectsWhenProjectsExist() throws Exception {
        List<ProjectEntity> projects = Arrays.asList(new ProjectEntity(), new ProjectEntity());
        when(projectRepository.findByCourseId(anyLong())).thenReturn(projects);

        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/projects"))
                .andExpect(status().isOk());
    }

    @Test
    public void getProjectByCourseIdReturnsNotFoundWhenProjectsDoNotExist() throws Exception {
        when(projectRepository.findByCourseId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/projects"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createProjectReturnsOkWhenUserHasAccessToProject() throws Exception {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(1L);
        when(projectRepository.save(any())).thenReturn(projectEntity);


        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/projects")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("name", "Project Name")
                        .param("description", "Project Description")
                        .param("groupClusterId", "1")
                        .param("testId", "1")
                        .param("projectType", "true")
                        .param("maxScore", "100"))
                .andExpect(status().isOk());
    }
}
