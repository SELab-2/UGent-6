package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.util.EntityToJsonConverter;
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

import java.time.OffsetDateTime;
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

    @Mock
    private CourseUserRepository courseUserRepository;

    @Mock
    private ProjectController projectController;

    @Mock
    private EntityToJsonConverter entityToJsonConverter;

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
        when(courseUserRepository.findByCourseIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new CourseUserEntity()));
        when(courseRepository.findTeacherByCourseId(anyLong())).thenReturn(new UserEntity());
        when(courseRepository.findAssistantsByCourseId(anyLong())).thenReturn(Collections.singletonList(new UserEntity()));
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
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(new CourseEntity()));
        when(projectRepository.findByCourseId(anyLong())).thenReturn(projects);
        when(courseUserRepository.findByCourseIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new CourseUserEntity()));
        when(entityToJsonConverter.projectEntityToProjectResponseJson(any(ProjectEntity.class), any(CourseEntity.class), any(UserEntity.class))).thenReturn(new ProjectResponseJson(
                new CourseReferenceJson("", "Test Course", 1L),
                OffsetDateTime.MIN,
                "",
                1L,
                "Test Description",
                "",
                "",
                1,
                true,
                new ProjectProgressJson(1, 1)
        ));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/projects"))
                .andExpect(status().isOk());
    }

    @Test
    public void getProjectByCourseIdReturnsNotFoundWhenCourseDoesNotExist() throws Exception {
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/projects"))
                .andExpect(status().isNotFound());
    }

}
