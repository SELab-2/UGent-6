package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.CourseWithInfoJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
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
    private CourseUtil courseUtil;

    @Mock
    private CommonDatabaseActions commonDatabaseActions;

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
    public void testGetUserCourses() throws Exception{
        Auth auth = mock(Auth.class);
        UserEntity user = mock(UserEntity.class);
        CourseEntity course = mock(CourseEntity.class);
        when(userRepository.findCourseIdsByUserId(anyLong())).
                thenReturn(List.of(new UserRepository.CourseIdWithRelation[]{new UserRepository.CourseIdWithRelation() {
                    @Override
                    public Long getCourseId() {
                        return 1L;
                    }

                    @Override
                    public CourseRelation getRelation() {
                        return CourseRelation.course_admin;
                    }

                    @Override
                    public String getName() {
                        return "";
                    }
                }}));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH))
                .andExpect(status().isOk());
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH))
                .andExpect(status().isOk());
        when(userRepository.findCourseIdsByUserId(anyLong())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH))
                .andExpect(status().isInternalServerError());
    }




    @Test
    public void testGetCourseByCourseId() throws Exception {
        when(courseUtil.getJoinLink(any(), any())).thenReturn("");
        when(entityToJsonConverter.courseEntityToCourseWithInfo(any(), any())).
                thenReturn(new CourseWithInfoJson(0L, "", "", new UserReferenceJson("", "", 0L),
                        new ArrayList<>(), "", ""));
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any(UserEntity.class))).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(new CourseEntity(), null)));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfUserInCourse(anyLong(), any(UserEntity.class))).
                thenReturn(new CheckResult<>(HttpStatus.NOT_FOUND, "", new Pair<>(null, null)));

        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testCreateCourse() throws Exception{
        Auth auth = mock(Auth.class);
        UserEntity user = mock(UserEntity.class);
        
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
