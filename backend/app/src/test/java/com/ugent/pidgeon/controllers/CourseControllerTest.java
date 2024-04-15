package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.CourseWithInfoJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
    private GroupClusterRepository groupClusterRepository;

    @Mock
    private CourseUtil courseUtil;

    @Mock
    private UserUtil userUtil;

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
                        .with(request -> {
                            request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
                            return request;
                        }))
                .build();
    }

    @Test
    public void testGetUserCourses() throws Exception {
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
    public void testCreateCourse() throws Exception {
        String courseJson = "{\"name\": \"test\", \"description\": \"description\"}";
        when(courseUtil.checkCourseJson(any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(courseRepository.save(any())).thenReturn(null);
        when(courseUserRepository.save(any())).thenReturn(null);
        when(groupClusterRepository.save(any())).thenReturn(null);
        when(courseUtil.getJoinLink(any(), any())).thenReturn("");
        when(entityToJsonConverter.courseEntityToCourseWithInfo(any(), any())).
                thenReturn(new CourseWithInfoJson(0L, "", "", new UserReferenceJson("", "", 0L),
                        new ArrayList<>(), "", ""));

        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isOk());

        when(courseUtil.checkCourseJson(any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isIAmATeapot());

        when(courseUtil.checkCourseJson(any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isInternalServerError());
    }


    // This function also tests all lines of doCourseUpdate
    @Test
    public void testUpdateCourse() throws Exception {
        String courseJson = "{\"name\": \"test\", \"description\": \"description\"}";
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new CourseEntity()));
        when(courseUtil.checkCourseJson(any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(courseRepository.save(any())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isOk());

        when(courseUtil.checkCourseJson(any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isIAmATeapot());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isForbidden());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testPatchCourse() throws Exception {
        String courseJson = "{\"name\": null, \"description\": \"description\"}";
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", new CourseEntity()));
        when(courseUtil.checkCourseJson(any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(courseRepository.save(any())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isOk());

        courseJson = "{\"name\": \"name\", \"description\": null}";
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isOk());

        courseJson = "{\"name\": null, \"description\": null}";
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isBadRequest());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isForbidden());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
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
    public void testDeleteCourse() throws Exception {
        ProjectEntity project = new ProjectEntity(1, "name", "description", 1L, 1L, true, 20, OffsetDateTime.now());
        GroupClusterEntity groupCluster = new GroupClusterEntity(1L, 20, "cluster", 5);
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(new CourseEntity(), CourseRelation.creator)));
        when(courseRepository.findAllProjectsByCourseId(anyLong())).thenReturn(List.of(project));
        when(commonDatabaseActions.deleteProject(anyLong())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(groupClusterRepository.findByCourseId(anyLong())).thenReturn(List.of(groupCluster));
        when(commonDatabaseActions.deleteClusterById(anyLong())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isOk());

        when(commonDatabaseActions.deleteClusterById(anyLong())).thenReturn(new CheckResult<>(HttpStatus.NO_CONTENT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isNoContent());

        when(commonDatabaseActions.deleteProject(anyLong())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isIAmATeapot());

        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", new Pair<>(null, null)));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isBadRequest());

        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(new CourseEntity(), CourseRelation.enrolled)));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isForbidden());

        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    public void testGetProjectsByCourseId() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton");
        course.setId(1);
        List<ProjectEntity> projects = Arrays.asList(new ProjectEntity(), new ProjectEntity());
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(course, CourseRelation.creator)));
        when(projectRepository.findByCourseId(anyLong())).thenReturn(projects);
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

        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/projects"))
                .andExpect(status().isIAmATeapot());
    }


    @Test
    public void testJoinCourse() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton");
        course.setId(1);
        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), anyLong())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join/1908"))
                .andExpect(status().isOk());

        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join/1908"))
                .andExpect(status().isInternalServerError());

        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join/1908"))
                .andExpect(status().isIAmATeapot());
    }


    @Test
    public void testGetJoinKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton");
        course.setId(1);
        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/join/1908"))
                .andExpect(status().isOk());

        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", course));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/join/1908"))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testJoinCourseNoKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton");
        course.setId(1);
        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), anyLong())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join"))
                .andExpect(status().isOk());

        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetCourseJoinKeyNoKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton");
        course.setId(1);
        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/join"))
                .andExpect(status().isOk());

    }

    @Test
    public void testLeaveCourse() throws Exception {
        when(courseUtil.canLeaveCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", CourseRelation.enrolled));
        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/leave"))
                .andExpect(status().isOk());

        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/leave"))
                .andExpect(status().isInternalServerError());

        when(courseUtil.canLeaveCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/leave"))
                .andExpect(status().isBadRequest());

        when(courseUtil.canLeaveCourse(anyLong(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/leave"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testRemoveCourseMember() throws Exception {
        String userIdJson = "{\"userId\": 1}";
        when(courseUtil.canDeleteUser(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", CourseRelation.enrolled));
        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userIdJson))
                .andExpect(status().isOk());

        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userIdJson))
                .andExpect(status().isInternalServerError());

        when(courseUtil.canDeleteUser(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userIdJson))
                .andExpect(status().isIAmATeapot());

    }

    @Test
    public void testAddCourseMember() throws Exception {
        String request = "{\"userId\": 1, \"relation\": \"enrolled\"}";
        when(courseUtil.canUpdateUserInCourse(anyLong(), any(), any(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new CourseUserEntity(1, 1, CourseRelation.enrolled)));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        when(courseUtil.canUpdateUserInCourse(anyLong(), any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testUpdateCourseMember() throws Exception {
        String request = "{\"userId\": 1, \"relation\": \"enrolled\"}";
        when(courseUtil.canUpdateUserInCourse(anyLong(), any(), any(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new CourseUserEntity(1, 1, CourseRelation.enrolled)));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        when(courseUtil.canUpdateUserInCourse(anyLong(), any(), any(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new CourseUserEntity(1, 1, CourseRelation.course_admin)));
        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(false);
        request = "{\"userId\": 1, \"relation\": \"course_admin\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isInternalServerError());

        when(courseUtil.canUpdateUserInCourse(anyLong(), any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetCourseMembers() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton");
        course.setId(1);
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(course, CourseRelation.course_admin)));
        when(courseUserRepository.findAllMembers(anyLong())).thenReturn(
                List.of(new CourseUserEntity(1, 2, CourseRelation.creator))
        );
        when(userUtil.getUserIfExists(anyLong())).thenReturn(new UserEntity("name", "surname", "email", UserRole.teacher, "id"));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/members"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/members"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetCourseKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton");
        course.setId(1);
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/joinLink"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/joinLink"))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testGetAndCreateCourseKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton");
        course.setId(1);
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1/joinLink"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1/joinLink"))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testDeleteCourseKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton");
        course.setId(1);
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/joinLink"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/joinLink"))
                .andExpect(status().isIAmATeapot());
    }
}
