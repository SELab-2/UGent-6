package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.CourseWithInfoJson;
import com.ugent.pidgeon.model.json.CourseWithRelationJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.postgre.repository.UserRepository.CourseIdWithRelation;
import com.ugent.pidgeon.util.*;
import java.util.Collections;
import java.util.logging.Logger;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    private CourseEntity archivedCourse;
    private CourseEntity activeCourse;
    private CourseWithInfoJson activeCourseJson;

    private ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();


    @BeforeEach
    public void setup() {
        setUpController(courseController);

        archivedCourse = new CourseEntity("archivedname", "description",2024);
        archivedCourse.setArchivedAt(OffsetDateTime.now());
        archivedCourse.setId(1);
        activeCourse = new CourseEntity("name", "description",2024);
        archivedCourse.setId(2);

        activeCourseJson = new CourseWithInfoJson(
            activeCourse.getId(),
            activeCourse.getName(),
            activeCourse.getDescription(),
            new UserReferenceJson("", "", 0L),
            new ArrayList<>(),
            "",
            "",
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            activeCourse.getCourseYear());

    }

    @Test
    public void testGetUserCourses() throws Exception {

        /* Mock active course return */
        when(userRepository.findCourseIdsByUserId(anyLong())).
                thenReturn(List.of(new UserRepository.CourseIdWithRelation[]{new UserRepository.CourseIdWithRelation() {
                    @Override
                    public Long getCourseId() {
                        return activeCourse.getId();
                    }

                    @Override
                    public CourseRelation getRelation() {
                        return CourseRelation.course_admin;
                    }

                    @Override
                    public String getName() {
                        return activeCourse.getName();
                    }
                }}));
        CourseWithRelationJson courseJson = new CourseWithRelationJson(
            "",
                CourseRelation.course_admin,
                activeCourse.getName(),
                activeCourse.getId(),
                activeCourse.getArchivedAt(),
                2,
                activeCourse.getCreatedAt(),
                activeCourse.getCourseYear()
        );
        when(entityToJsonConverter.courseEntityToCourseWithRelation(activeCourse, CourseRelation.course_admin)).
                thenReturn(courseJson);
        when(courseRepository.findById(activeCourse.getId())).thenReturn(Optional.of(activeCourse));

        /* Mock archived course return */
        when(userRepository.findArchivedCoursesByUserId(anyLong())).
                thenReturn(List.of(new UserRepository.CourseIdWithRelation[]{new UserRepository.CourseIdWithRelation() {
                    @Override
                    public Long getCourseId() {
                        return archivedCourse.getId();
                    }

                    @Override
                    public CourseRelation getRelation() {
                        return CourseRelation.course_admin;
                    }

                    @Override
                    public String getName() {
                        return archivedCourse.getName();
                    }
                }}));
        CourseWithRelationJson archivedCourseJson = new CourseWithRelationJson(
            "",
                CourseRelation.course_admin,
                archivedCourse.getName(),
                archivedCourse.getId(),
                archivedCourse.getArchivedAt(),
                2,
                archivedCourse.getCreatedAt(),
                archivedCourse.getCourseYear()
        );
        when(entityToJsonConverter.courseEntityToCourseWithRelation(archivedCourse, CourseRelation.course_admin)).
                thenReturn(archivedCourseJson);
        when(courseRepository.findById(archivedCourse.getId())).thenReturn(Optional.of(archivedCourse));

        /* If no archived param, return archived and active courses */
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(courseJson, archivedCourseJson))));

        /* If archived param is false, return only active courses */
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "?archived=false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(courseJson))));

        /* If archived param is true, return only archived courses */
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "?archived=true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(archivedCourseJson))));

        /* If no courses are found, return empty list */
        when(userRepository.findCourseIdsByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(userRepository.findArchivedCoursesByUserId(anyLong())).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        /* If error occurs, return 500 */
        when(userRepository.findCourseIdsByUserId(anyLong())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH))
                .andExpect(status().isInternalServerError());
    }


    @Test
    public void testCreateCourse() throws Exception {
        String courseJson = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024}";
        /* If everything is correct, return 200 */
        when(courseUtil.checkCourseJson(any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(courseRepository.save(any())).thenReturn(activeCourse);
        when(courseUserRepository.save(any())).thenReturn(null);
        when(groupClusterRepository.save(any())).thenReturn(null);
        when(courseUtil.getJoinLink(any(), any())).thenReturn("");
        when(entityToJsonConverter.courseEntityToCourseWithInfo(any(), any(), anyBoolean())).
                thenReturn(activeCourseJson);

        Logger.getGlobal().info("User: " + getMockUser().getRole());

        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(activeCourseJson)));

        verify(courseUserRepository, times(1)).save(argThat(courseUser ->
            courseUser.getCourseId() == activeCourse.getId() &&
                courseUser.getUserId() == getMockUser().getId() &&
                courseUser.getRelation().equals(CourseRelation.creator)
        ));
        verify(groupClusterRepository, times(1)).save(argThat(groupCluster ->
            groupCluster.getCourseId() == activeCourse.getId() &&
                groupCluster.getMaxSize() == 1 &&
                groupCluster.getGroupAmount() == 0
        ));
        verify(entityToJsonConverter, times(1)).courseEntityToCourseWithInfo(activeCourse, "", false);
        verify(courseUtil, times(1)).getJoinLink(activeCourse.getJoinKey(), "" + activeCourse.getId());

        /* If user is not a teacher, return 403 */
        setMockUserRoles(UserRole.student);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isForbidden());
        setMockUserRoles(UserRole.teacher);

        /* If course json is invalid, return 400 */
        when(courseUtil.checkCourseJson(any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        when(courseUtil.checkCourseJson(any(), any(),any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isIAmATeapot());

        /* If error occurs, return 500 */
        when(courseUtil.checkCourseJson(any(), any(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isInternalServerError());
    }


    // This function also tests all lines of doCourseUpdate
    @Test
    public void testUpdateCourse() throws Exception {
            String courseJson = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024}";
            /* If admin and valid json, update course and return 200 */
            when(courseUtil.getCourseIfAdmin(anyLong(), any())).
                    thenReturn(new CheckResult<>(HttpStatus.OK, "", activeCourse));
            CourseEntity updatedEntity = new CourseEntity("test", "description",2024);
            CourseWithInfoJson updatedJson = new CourseWithInfoJson(
                activeCourse.getId(),
                "test",
                "description",
                new UserReferenceJson("", "", 0L),
                new ArrayList<>(),
                "",
                "",
                "",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                2023
            );
            when(courseUtil.checkCourseJson(any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
            when(courseUtil.getJoinLink(any(), any())).thenReturn("");
            when(courseRepository.save(any())).thenReturn(updatedEntity);
            when(entityToJsonConverter.courseEntityToCourseWithInfo(updatedEntity, "", false)).thenReturn(updatedJson);
            activeCourse.setArchivedAt(OffsetDateTime.now());
            OffsetDateTime originalArchivedAt = activeCourse.getArchivedAt();
            mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJson))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json(objectMapper.writeValueAsString(updatedJson)));
            assertEquals(originalArchivedAt, activeCourse.getArchivedAt());
            activeCourse.setArchivedAt(null);


            /* If courseJson has archived field, update archived accordingly */
            String courseJsonWithArchivedTrue = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024, \"archived\": \"true\"}";
            mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJsonWithArchivedTrue))
                    .andExpect(status().isOk());
            assertNotNull(activeCourse.getArchivedAt());


            String courseJsonWithArchivedFalse = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024, \"archived\": \"false\"}";
            mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJsonWithArchivedFalse))
                    .andExpect(status().isOk());
            assertNull(activeCourse.getArchivedAt());


            /* If invalid json, return corresponding statuscode */
            when(courseUtil.checkCourseJson(any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
            mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJson))
                    .andExpect(status().isIAmATeapot());

            /* If not admin, return 403 */
            when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
            mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJson))
                    .andExpect(status().isForbidden());

            /* If error occurs, return 500 */
            when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenThrow(new RuntimeException());
            mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJson))
                    .andExpect(status().isInternalServerError());
    }

    @Test
    public void testPatchCourse() throws Exception {
        
        /* If admin and valid json, update course and return 200 */
        String originalName = activeCourse.getName();
        String originalDescription = activeCourse.getDescription();
        Integer originalYear = activeCourse.getCourseYear();
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).
            thenReturn(new CheckResult<>(HttpStatus.OK, "", activeCourse));
        CourseEntity updatedEntity = new CourseEntity("test", "description2",2024);
        CourseWithInfoJson updatedJson = new CourseWithInfoJson(
            activeCourse.getId(),
            "test",
            "description2",
            new UserReferenceJson("", "", 0L),
            new ArrayList<>(),
            "",
            "",
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            2023
        );
        when(courseUtil.checkCourseJson(any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(courseUtil.getJoinLink(any(), any())).thenReturn("");
        when(courseRepository.save(activeCourse)).thenReturn(updatedEntity);
        when(entityToJsonConverter.courseEntityToCourseWithInfo(updatedEntity, "", false)).thenReturn(updatedJson);
            /* If field is not present, do not update it */
        String patchCourseJson = "{\"name\": \"test\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchCourseJson))
            .andExpect(status().isOk());
        assertNotEquals(originalName, activeCourse.getName());
        assertEquals(originalDescription, activeCourse.getDescription());
        assertEquals(originalYear, activeCourse.getCourseYear());
        assertNull(activeCourse.getArchivedAt());
            /* If fields are present, update them */
        String requestJson = "{\"name\": \"test2\", \"description\": \"description2\",\"courseYear\" : 2034}";
        originalName = activeCourse.getName();
        activeCourse.setArchivedAt(OffsetDateTime.now());
        OffsetDateTime originalArchivedAt = activeCourse.getArchivedAt();
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(updatedJson)));
        assertNotEquals(originalName, activeCourse.getName());
        assertNotEquals(originalDescription, activeCourse.getDescription());
        assertNotEquals(originalYear, activeCourse.getCourseYear());
        assertEquals(originalArchivedAt, activeCourse.getArchivedAt());
        activeCourse.setArchivedAt(null);


        /* If courseJson has archived field, update archived accordingly */
        String courseJsonWithArchivedTrue = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024, \"archived\": \"true\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJsonWithArchivedTrue))
            .andExpect(status().isOk());
        assertNotNull(activeCourse.getArchivedAt());


        String courseJsonWithArchivedFalse = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024, \"archived\": \"false\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJsonWithArchivedFalse))
            .andExpect(status().isOk());
        assertNull(activeCourse.getArchivedAt());


        /* If invalid json, return corresponding statuscode */
        when(courseUtil.checkCourseJson(any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isIAmATeapot());

        /* If not admin, return 403 */
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isForbidden());

        /* If error occurs, return 500 */
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isInternalServerError());

    }


    @Test
    public void testGetCourseByCourseId() throws Exception {
        when(courseUtil.getJoinLink(any(), any())).thenReturn("");
        when(entityToJsonConverter.courseEntityToCourseWithInfo(any(), any(), anyBoolean())).
                thenReturn(new CourseWithInfoJson(0L, "", "", new UserReferenceJson("", "", 0L),
                        new ArrayList<>(), "", "", "", OffsetDateTime.now(), OffsetDateTime.now(), 2023));
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any(UserEntity.class))).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(new CourseEntity(), CourseRelation.course_admin)));
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
        CourseEntity course = new CourseEntity("name", "descripton",2024);
        course.setId(1);
        List<ProjectEntity> projects = Arrays.asList(new ProjectEntity(), new ProjectEntity());
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(course, CourseRelation.creator)));
        when(projectRepository.findByCourseId(anyLong())).thenReturn(projects);
        when(entityToJsonConverter.projectEntityToProjectResponseJson(any(ProjectEntity.class), any(CourseEntity.class), any(UserEntity.class))).thenReturn(new ProjectResponseJson(
                new CourseReferenceJson("", "Test Course", 1L, OffsetDateTime.now()),
                OffsetDateTime.MIN,
                "",
                1L,
                "Test Description",
                "",
                "",
                1,
                true,
                new ProjectProgressJson(1, 1),
                1L,
            1L
        ));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/projects"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/projects"))
                .andExpect(status().isIAmATeapot());
    }


    @Test
    public void testJoinCourse() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton",2024);
        course.setId(1);
        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join/1908"))
                .andExpect(status().isOk());

        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join/1908"))
                .andExpect(status().isInternalServerError());

        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join/1908"))
                .andExpect(status().isIAmATeapot());
    }


    @Test
    public void testGetJoinKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton",2024);
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
        CourseEntity course = new CourseEntity("name", "descripton",2024);
        course.setId(1);
        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), any())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join"))
                .andExpect(status().isOk());

        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/join"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetCourseJoinKeyNoKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton",2024);
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
        when(courseUtil.canDeleteUser(anyLong(), anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", CourseRelation.enrolled));
        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userIdJson))
                .andExpect(status().isOk());

        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userIdJson))
                .andExpect(status().isInternalServerError());

        when(courseUtil.canDeleteUser(anyLong(), anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userIdJson))
                .andExpect(status().isIAmATeapot());

    }

    @Test
    public void testAddCourseMember() throws Exception {
        String request = "{\"userId\": 1, \"relation\": \"enrolled\"}";
        when(courseUtil.canUpdateUserInCourse(anyLong(), any(), any(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new CourseUserEntity(1, 1, CourseRelation.enrolled)));
        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), any()))
            .thenReturn(true);
        when(userUtil.getUserIfExists(anyLong())).thenReturn(new UserEntity("name", "surname", "email", UserRole.teacher, "id"));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), any()))
            .thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isInternalServerError());

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
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new CourseUserEntity(1, 1, CourseRelation.course_admin)));
        when(userUtil.getUserIfExists(anyLong())).thenReturn(new UserEntity("name", "surname", "email", UserRole.teacher, "id"));
        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        when(courseUtil.canUpdateUserInCourse(anyLong(), any(), any(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new CourseUserEntity(1, 3, CourseRelation.enrolled)));
        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(false);
        request = "{\"relation\": \"course_admin\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isInternalServerError());

        when(courseUtil.canUpdateUserInCourse(anyLong(), any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.COURSE_BASE_PATH + "/1/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetCourseMembers() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton",2024);
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
        CourseEntity course = new CourseEntity("name", "descripton",2024);
        course.setId(1);
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/joinKey"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/joinKey"))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testGetAndCreateCourseKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton",2024);
        course.setId(1);
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1/joinKey"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.COURSE_BASE_PATH + "/1/joinKey"))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testDeleteCourseKey() throws Exception {
        CourseEntity course = new CourseEntity("name", "descripton",2024);
        course.setId(1);
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/joinKey"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.COURSE_BASE_PATH + "/1/joinKey"))
                .andExpect(status().isIAmATeapot());
    }
}