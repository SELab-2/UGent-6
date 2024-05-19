package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseJoinInformationJson;
import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.CourseWithInfoJson;
import com.ugent.pidgeon.model.json.CourseWithRelationJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;
import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.model.json.UserReferenceWithRelation;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.postgre.repository.UserRepository.CourseIdWithRelation;
import com.ugent.pidgeon.util.*;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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
            new UserReferenceJson("", "", 0L, ""),
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

        /* If course doesn't get found, it just gets filtered */
        when(courseRepository.findById(activeCourse.getId())).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH))
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
        when(courseUtil.checkCourseJson(argThat(
            json -> json.getName().equals("test") &&
                json.getDescription().equals("description") &&
                json.getYear() == 2024
        ), eq(getMockUser()), eq(null))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(courseRepository.save(any())).thenReturn(activeCourse);
        when(courseUserRepository.save(argThat(
            courseUser -> courseUser.getCourseId() == activeCourse.getId() &&
                courseUser.getUserId() == getMockUser().getId() &&
                courseUser.getRelation().equals(CourseRelation.creator)
        ))).thenReturn(null);
        when(groupClusterRepository.save(argThat(
            groupCluster -> groupCluster.getCourseId() == activeCourse.getId() &&
                groupCluster.getMaxSize() == 1 &&
                groupCluster.getGroupAmount() == 0
        ))).thenReturn(null);
        when(courseUtil.getJoinLink(activeCourse.getJoinKey(), ""+activeCourse.getId())).thenReturn("");
        when(entityToJsonConverter.courseEntityToCourseWithInfo(activeCourse, "", false)).
                thenReturn(activeCourseJson);

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

        /* If user is not a teacher, return 403 */
        setMockUserRoles(UserRole.student);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isForbidden());
        setMockUserRoles(UserRole.teacher);

        /* If course json is invalid, return 400 */
        reset(courseUtil);
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
            String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId();
            String courseJson = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024}";
            CourseEntity updatedEntity = new CourseEntity("test", "description",2024);
            CourseWithInfoJson updatedJson = new CourseWithInfoJson(
                activeCourse.getId(),
                "test",
                "description",
                new UserReferenceJson("", "", 0L, ""),
                new ArrayList<>(),
                "",
                "",
                "",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                2023
            );
            /* If admin and valid json, update course and return 200 */
            when(courseUtil.getCourseIfAdmin(activeCourse.getId(), getMockUser())).
                    thenReturn(new CheckResult<>(HttpStatus.OK, "", activeCourse));
            when(courseUtil.checkCourseJson(
                argThat(
                    json -> json.getName().equals("test") &&
                            json.getDescription().equals("description") &&
                            json.getYear() == 2024
                ),
                eq(getMockUser()),
                eq(activeCourse.getId()))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
            when(courseUtil.getJoinLink(activeCourse.getJoinKey(), ""+activeCourse.getId())).thenReturn("");
            when(courseRepository.save(activeCourse)).thenReturn(updatedEntity);
            when(entityToJsonConverter.courseEntityToCourseWithInfo(updatedEntity, "", false)).thenReturn(updatedJson);
            activeCourse.setArchivedAt(OffsetDateTime.now());
            OffsetDateTime originalArchivedAt = activeCourse.getArchivedAt();
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJson))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json(objectMapper.writeValueAsString(updatedJson)));
            assertEquals(originalArchivedAt, activeCourse.getArchivedAt());
            activeCourse.setArchivedAt(null);


            /* If courseJson has archived field, update archived accordingly */
            String courseJsonWithArchivedTrue = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024, \"archived\": \"true\"}";
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJsonWithArchivedTrue))
                    .andExpect(status().isOk());
            assertNotNull(activeCourse.getArchivedAt());


            String courseJsonWithArchivedFalse = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024, \"archived\": \"false\"}";
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJsonWithArchivedFalse))
                    .andExpect(status().isOk());
            assertNull(activeCourse.getArchivedAt());


            /* If invalid json, return corresponding statuscode */
            reset(courseUtil);
            when(courseUtil.getCourseIfAdmin(activeCourse.getId(), getMockUser())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", activeCourse));
            when(courseUtil.checkCourseJson(
            argThat(
                json -> json.getName().equals("test") &&
                    json.getDescription().equals("description") &&
                    json.getYear() == 2024
            ),
            eq(getMockUser()),
            eq(activeCourse.getId()))).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJson))
                    .andExpect(status().isIAmATeapot());

            /* If not admin, return 403 */
            when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJson))
                    .andExpect(status().isForbidden());

            /* If error occurs, return 500 */
            when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenThrow(new RuntimeException());
            mockMvc.perform(MockMvcRequestBuilders.put(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(courseJson))
                    .andExpect(status().isInternalServerError());
    }

    @Test
    public void testPatchCourse() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId();
        String originalName = activeCourse.getName();
        String originalDescription = activeCourse.getDescription();
        Integer originalYear = activeCourse.getCourseYear();
        CourseEntity updatedEntity = new CourseEntity("test", "description2",2024);
        CourseWithInfoJson updatedJson = new CourseWithInfoJson(
            activeCourse.getId(),
            "test",
            "description2",
            new UserReferenceJson("", "", 0L, ""),
            new ArrayList<>(),
            "",
            "",
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            2023
        );
        /* If admin and valid json, update course and return 200 */
        when(courseUtil.getCourseIfAdmin(activeCourse.getId(), getMockUser())).
            thenReturn(new CheckResult<>(HttpStatus.OK, "", activeCourse));
        when(courseUtil.checkCourseJson(any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(courseUtil.getJoinLink(activeCourse.getJoinKey(), ""+activeCourse.getId())).thenReturn("");
        when(courseRepository.save(activeCourse)).thenReturn(updatedEntity);
        when(entityToJsonConverter.courseEntityToCourseWithInfo(updatedEntity, "", false)).thenReturn(updatedJson);
            /* If field is not present, do not update it */
        String patchCourseJson = "{\"name\": \"test\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchCourseJson))
            .andExpect(status().isOk());
        String finalOriginalDescription = originalDescription;
        verify(courseUtil, times(1)).checkCourseJson(argThat(
              json -> json.getName().equals("test") &&
                  json.getDescription().equals(finalOriginalDescription) &&
                  Objects.equals(json.getYear(), originalYear)
          ), eq(getMockUser()), eq(activeCourse.getId()));
        assertNotEquals(originalName, activeCourse.getName());
        assertEquals(originalDescription, activeCourse.getDescription());
        assertEquals(originalYear, activeCourse.getCourseYear());
        assertNull(activeCourse.getArchivedAt());
        originalName = activeCourse.getName();

        String patchCourseJsonNoName =  "{\"description\": \"description88\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchCourseJsonNoName))
            .andExpect(status().isOk());
        String finalOriginalName = originalName;
        verify(courseUtil, times(1)).checkCourseJson(argThat(
                json -> json.getName().equals(finalOriginalName) &&
                    json.getDescription().equals("description88") &&
                    Objects.equals(json.getYear(), originalYear)
            ), eq(getMockUser()), eq(activeCourse.getId()));
        assertEquals(originalName, activeCourse.getName());
        assertNotEquals(originalDescription, activeCourse.getDescription());
        assertEquals(originalYear, activeCourse.getCourseYear());
        assertNull(activeCourse.getArchivedAt());

            /* If fields are present, update them */
        String requestJson = "{\"name\": \"test2\", \"description\": \"description2\",\"courseYear\" : 2034}";
        originalDescription = activeCourse.getDescription();
        activeCourse.setArchivedAt(OffsetDateTime.now());
        OffsetDateTime originalArchivedAt = activeCourse.getArchivedAt();
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(updatedJson)));
        verify(courseUtil, times(1)).checkCourseJson(argThat(
            json -> json.getName().equals("test2") &&
                json.getDescription().equals("description2") &&
                json.getYear() == 2034
        ), eq(getMockUser()), eq(activeCourse.getId()));
        assertNotEquals(originalName, activeCourse.getName());
        assertNotEquals(originalDescription, activeCourse.getDescription());
        assertNotEquals(originalYear, activeCourse.getCourseYear());
        assertEquals(originalArchivedAt, activeCourse.getArchivedAt());
        activeCourse.setArchivedAt(null);

        /* If courseJson has archived field, update archived accordingly */
        String courseJsonWithArchivedTrue = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024, \"archived\": \"true\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJsonWithArchivedTrue))
            .andExpect(status().isOk());
        assertNotNull(activeCourse.getArchivedAt());


        String courseJsonWithArchivedFalse = "{\"name\": \"test\", \"description\": \"description\",\"courseYear\" : 2024, \"archived\": \"false\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJsonWithArchivedFalse))
            .andExpect(status().isOk());
        assertNull(activeCourse.getArchivedAt());

        /* If no fields are present, change nothing */
        String emptyJson = "{\"ietswatnietboeit\": \"test\"}";
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
            .andExpect(status().isOk());
        assertEquals("test", activeCourse.getName());
        assertEquals("description", activeCourse.getDescription());
        assertEquals(2024, activeCourse.getCourseYear());



        /* If invalid json, return corresponding statuscode */
        when(courseUtil.checkCourseJson(any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isIAmATeapot());

        /* If not admin, return 403 */
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isForbidden());

        /* If error occurs, return 500 */
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isInternalServerError());

    }


    @Test
    public void testGetCourseByCourseId() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId();
        /* If user is admin, return course with joinKey information */
        when(courseUtil.getJoinLink(activeCourse.getJoinKey(), ""+activeCourse.getId())).thenReturn("");
        when(entityToJsonConverter.courseEntityToCourseWithInfo(any(), any(), anyBoolean())).
                thenReturn(activeCourseJson);
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(), getMockUser())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(activeCourse, CourseRelation.course_admin)));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(activeCourseJson)));
        verify(entityToJsonConverter, times(1)).courseEntityToCourseWithInfo(activeCourse, "", false);

        /* If user is not admin, return course without joinKey information */
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(), getMockUser())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(activeCourse, CourseRelation.enrolled)));
        mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isOk());
        verify(entityToJsonConverter, times(1)).courseEntityToCourseWithInfo(activeCourse, "", true);

        /* If course is not found, or user no acces return corresponding status */
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any(UserEntity.class))).
                thenReturn(new CheckResult<>(HttpStatus.NOT_FOUND, "", new Pair<>(null, null)));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isNotFound());
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any(UserEntity.class))).
                thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", new Pair<>(null, null)));
        mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isForbidden());
    }


    @Test
    public void testDeleteCourse() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId();

        /* If user is the creator of the course, delete succeeds and also deletes linked projects & coursses */
        ProjectEntity project = new ProjectEntity(1, "name", "description", 1L, 1L, true, 20, OffsetDateTime.now());
        GroupClusterEntity groupCluster = new GroupClusterEntity(1L, 20, "cluster", 5);
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(), getMockUser())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(activeCourse, CourseRelation.creator)));
        when(courseRepository.findAllProjectsByCourseId(activeCourse.getId())).thenReturn(List.of(project));
        when(commonDatabaseActions.deleteProject(project.getId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(groupClusterRepository.findByCourseId(activeCourse.getId())).thenReturn(List.of(groupCluster));
        when(commonDatabaseActions.deleteClusterById(groupCluster.getId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        CourseUserEntity courseUser = new CourseUserEntity(1L, 1L, CourseRelation.creator);
        List<CourseUserEntity> courseUsers = List.of(courseUser);
        when(courseUserRepository.findAllUsersByCourseId(activeCourse.getId())).thenReturn(courseUsers);
        doNothing().when(courseUserRepository).deleteAll(anyIterable());
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isOk());
        verify(courseUserRepository, times(1)).deleteAll(courseUsers);

        /* If something goes wrong while deleting a cluster or project, return corresponding status */
        when(commonDatabaseActions.deleteClusterById(anyLong())).thenReturn(new CheckResult<>(HttpStatus.NO_CONTENT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isNoContent());

        when(commonDatabaseActions.deleteProject(anyLong())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isIAmATeapot());

        /* If user isn't in course or course doesn't exist return corresponding status */
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", new Pair<>(null, null)));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isBadRequest());

        /* If user isn't the creator of the course, return 403 */
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).
                thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(new CourseEntity(), CourseRelation.enrolled)));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isForbidden());

        /* If a unexpected error occurs, return 500 */
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isInternalServerError());
    }


    @Test
    public void testGetProjectsByCourseId() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/projects";

        Pair<CourseEntity, CourseRelation> creatorPair = new Pair<>(activeCourse, CourseRelation.creator);
        Pair<CourseEntity, CourseRelation> enrolledPair = new Pair<>(activeCourse, CourseRelation.enrolled);
        ProjectEntity project = new ProjectEntity(1, "name", "description", 1L, 1L, true, 20, OffsetDateTime.now());
        ProjectResponseJson projectJson = new ProjectResponseJson(
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
            1L,
            OffsetDateTime.now()
        );
        /* If user is in course, return projects */
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(),getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", creatorPair));

        List<ProjectEntity> projects = List.of(project);
        when(projectRepository.findByCourseId(activeCourse.getId())).thenReturn(projects);
        when(entityToJsonConverter.projectEntityToProjectResponseJson(project, activeCourse, getMockUser())).thenReturn(projectJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(projectJson))));

        /* If a project isn't visible, and user role is student, it should not be returned */
        project.setVisible(false);
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", enrolledPair));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", creatorPair));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(projectJson))));

        /* If user not in course, or course doesn't exit or any other check fails, return corresponding status */
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isIAmATeapot());
    }


    @Test
    public void testJoinCourse() throws Exception {
        String urlWithKey = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/join/1908";
        String urlWithoutKey = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/join";
        CourseEntity course = activeCourse;
        /* If join key is correct, course is not archived and no error occurs, return 200 */
        when(courseUtil.checkJoinLink(activeCourse.getId(), "1908", getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        when(courseUtil.checkJoinLink(activeCourse.getId(), null, getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        when(commonDatabaseActions.createNewIndividualClusterGroup(activeCourse.getId(), getMockUser())).thenReturn(true);
        when(courseUtil.getJoinLink(course.getJoinKey(), ""+course.getId())).thenReturn("");
        when(entityToJsonConverter.courseEntityToCourseWithInfo(activeCourse, "", false)).thenReturn(activeCourseJson);
        mockMvc.perform(MockMvcRequestBuilders.post(urlWithKey))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(activeCourseJson)));
        mockMvc.perform(MockMvcRequestBuilders.post(urlWithoutKey))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(activeCourseJson)));
        verify(courseUserRepository, times(2)).save(argThat(courseUser ->
                courseUser.getCourseId() == activeCourse.getId() &&
                courseUser.getUserId() == getMockUser().getId() &&
                courseUser.getRelation().equals(CourseRelation.enrolled)
        ));

        /* If course is archived, return 403 */
        activeCourse.setArchivedAt(OffsetDateTime.now());
        mockMvc.perform(MockMvcRequestBuilders.post(urlWithKey))
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.post(urlWithoutKey))
                .andExpect(status().isForbidden());
        activeCourse.setArchivedAt(null);

        /* If an error occures when creating individual cluster group, return 500 */
        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(urlWithKey))
                .andExpect(status().isInternalServerError());
        mockMvc.perform(MockMvcRequestBuilders.post(urlWithoutKey))
                .andExpect(status().isInternalServerError());

        /* If join key check fails return corresponding status */
        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(urlWithKey))
            .andExpect(status().isIAmATeapot());
        mockMvc.perform(MockMvcRequestBuilders.post(urlWithoutKey))
            .andExpect(status().isIAmATeapot());
    }


    @Test
    public void testGetJoinInformation() throws Exception {
        String urlWithKey = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/join/1908";
        String urlWithoutKey = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/join";

        CourseEntity course = activeCourse;
        CourseJoinInformationJson courseJoinInformationJson = new CourseJoinInformationJson(
            activeCourse.getName(),
            activeCourse.getDescription()
        );
        /* If join key is correct, course is not archived and no error occurs, return 200 */
        when(courseUtil.checkJoinLink(activeCourse.getId(), "1908", getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        when(courseUtil.checkJoinLink(activeCourse.getId(), null, getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", course));
        mockMvc.perform(MockMvcRequestBuilders.get(urlWithKey))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(courseJoinInformationJson)));
      mockMvc.perform(MockMvcRequestBuilders.get(urlWithoutKey))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(courseJoinInformationJson)));

        /* If course is archived, reutrn 403 */
        activeCourse.setArchivedAt(OffsetDateTime.now());
        mockMvc.perform(MockMvcRequestBuilders.get(urlWithKey))
            .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.get(urlWithoutKey))
            .andExpect(status().isForbidden());
        activeCourse.setArchivedAt(null);

        /* If join key check fails return corresponding status */
        when(courseUtil.checkJoinLink(anyLong(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(urlWithKey))
            .andExpect(status().isIAmATeapot());
        mockMvc.perform(MockMvcRequestBuilders.get(urlWithoutKey))
            .andExpect(status().isIAmATeapot());
    }


    @Test
    public void testLeaveCourse() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/leave";
        /* If user can leave course, return 200 */
        /* If role is enrolled, an individualclustergroup should be deleted */
        when(courseUtil.canLeaveCourse(activeCourse.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", CourseRelation.enrolled));
        when(commonDatabaseActions.removeIndividualClusterGroup(activeCourse.getId(), getMockUser().getId())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isOk());
        verify(commonDatabaseActions, times(1)).removeIndividualClusterGroup(activeCourse.getId(), getMockUser().getId());

        /* If the role isn't enrolled, no individualclustergroup should be deleted */
        when(courseUtil.canLeaveCourse(activeCourse.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", CourseRelation.course_admin));
        reset(commonDatabaseActions);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isOk());
        verify(commonDatabaseActions, times(0)).removeIndividualClusterGroup(activeCourse.getId(), getMockUser().getId());

        /* If something goes wrong while deleting individual cluster group, return 500 */
        when(courseUtil.canLeaveCourse(activeCourse.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", CourseRelation.enrolled));
        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isInternalServerError());
        verify(commonDatabaseActions, times(1)).removeIndividualClusterGroup(activeCourse.getId(), getMockUser().getId());

        /* If user can't leave course for some reason, return corresponding error code */
        when(courseUtil.canLeaveCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isBadRequest());

        /* If an unexpected error occurs, return 500 */
        when(courseUtil.canLeaveCourse(anyLong(), any())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testRemoveCourseMember() throws Exception {
        long userId = 2L;
        String url = ApiRoutes.COURSE_BASE_PATH + "/" +  activeCourse.getId() + "/members/2";

        /* If user can remove other people, and course exists, return 200 */
        /* If user is admin, removeIndividualClusterGroup gets called */
        when(courseUtil.canDeleteUser(activeCourse.getId(), userId, getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", CourseRelation.course_admin));
        when(commonDatabaseActions.removeIndividualClusterGroup(activeCourse.getId(), userId)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isOk());
        verify(courseUserRepository, times(1)).deleteById(argThat(
            id -> id.getCourseId() == activeCourse.getId() && id.getUserId() == userId
        ));
        verify(commonDatabaseActions, times(0)).removeIndividualClusterGroup(activeCourse.getId(), userId);

        /* If user enrolled, removeIndividualClusterGroup gets called */
        when(courseUtil.canDeleteUser(activeCourse.getId(), userId, getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", CourseRelation.enrolled));
        when(commonDatabaseActions.removeIndividualClusterGroup(activeCourse.getId(), userId)).thenReturn(true);
        reset(courseUserRepository);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isOk());
        verify(courseUserRepository, times(1)).deleteById(argThat(
            id -> id.getCourseId() == activeCourse.getId() && id.getUserId() == userId
        ));
        verify(commonDatabaseActions, times(1)).removeIndividualClusterGroup(activeCourse.getId(), userId);

        /* If something goes wrong when removing individual group, return 500 */
        when(commonDatabaseActions.removeIndividualClusterGroup(activeCourse.getId(), userId)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isInternalServerError());

        /* If user can't delete the other use, return corresponding status*/
        when(courseUtil.canDeleteUser(anyLong(), anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isIAmATeapot());

    }

    @Test
    public void testAddCourseMember() throws Exception {
        String requestString = "{\"userId\": 1, \"relation\": \"enrolled\"}";
        String requestStringAdmin = "{\"userId\": 1, \"relation\": \"course_admin\"}";
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/members";
        CourseUserEntity courseUser = new CourseUserEntity(activeCourse.getId(), 1, CourseRelation.enrolled);
        UserEntity user = new UserEntity("name", "surname", "email", UserRole.teacher, "id", "");
        /* If all checks succeed, return 201 */

        when(courseUtil.canUpdateUserInCourse(
            eq(activeCourse.getId()),
            argThat(
                request -> request.getUserId() == 1 && request.getRelationAsEnum().equals(CourseRelation.course_admin)
            ),
            eq(getMockUser()),
            eq(HttpMethod.POST))).thenReturn(new CheckResult<>(HttpStatus.OK, "", courseUser));
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestStringAdmin))
            .andExpect(status().isCreated());
        /* If user is not enrolled, there is no attempt to create individual cluster group */
        verify(userUtil, times(0)).getUserIfExists(anyLong());
        verify(courseUserRepository, times(1)).save(argThat(
            courseUserEntity -> courseUserEntity.getCourseId() == activeCourse.getId() &&
                courseUserEntity.getUserId() == 1 &&
                courseUserEntity.getRelation().equals(CourseRelation.course_admin)
        ));
        verify(commonDatabaseActions, times(0)).createNewIndividualClusterGroup(anyLong(), any());

        reset(courseUtil);
        when(courseUtil.canUpdateUserInCourse(
            eq(activeCourse.getId()),
            argThat(
                request -> request.getUserId() == 1 && request.getRelationAsEnum().equals(CourseRelation.enrolled)
            ),
            eq(getMockUser()),
            eq(HttpMethod.POST)))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", courseUser));
        when(userUtil.getUserIfExists(anyLong())).thenReturn(user);
        when(commonDatabaseActions.createNewIndividualClusterGroup(activeCourse.getId(), user)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isCreated());
        verify(courseUserRepository, times(1)).save(argThat(
            courseUserEntity -> courseUserEntity.getCourseId() == activeCourse.getId() &&
                courseUserEntity.getUserId() == 1 &&
                courseUserEntity.getRelation().equals(CourseRelation.enrolled)
        ));

        /* If something goes wrong when creating individual cluster, return 500 */
        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), any()))
            .thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString))
            .andExpect(status().isInternalServerError());

        /* If user isn't found, return 404 */
        when(userUtil.getUserIfExists(anyLong())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString))
            .andExpect(status().isNotFound());

        /* If user can't be added to the course, return corresponding status */
        reset(courseUtil);
        when(courseUtil.canUpdateUserInCourse(
            anyLong(), any(), any(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testUpdateCourseMember() throws Exception {
        long userId = 2L;
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/members/" + userId;
        String request = "{\"relation\": \"enrolled\"}";
        String adminRequest = "{\"relation\": \"course_admin\"}";
        UserEntity user = new UserEntity("name", "surname", "email", UserRole.teacher, "id", "");
        CourseUserEntity enrolledUser = new CourseUserEntity(activeCourse.getId(), userId, CourseRelation.enrolled);
        CourseUserEntity adminUser = new CourseUserEntity(activeCourse.getId(), userId, CourseRelation.course_admin);
        /* If all checks succeed, 200 gets returned  */
        /* If the new role is the same as the old, no changes to individualgroupClusters are done */
        when(courseUtil.canUpdateUserInCourse(
            eq(activeCourse.getId()),
            argThat(
                requestJson -> requestJson.getRelationAsEnum().equals(CourseRelation.enrolled)
            ),
            eq(getMockUser()),
            eq(HttpMethod.PATCH))).thenReturn(new CheckResult<>(HttpStatus.OK, "", enrolledUser));
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk());
        verify(commonDatabaseActions, times(0)).removeIndividualClusterGroup(anyLong(), anyLong());
        verify(commonDatabaseActions, times(0)).createNewIndividualClusterGroup(anyLong(), any());
        verify(courseUserRepository, times(0)).save(any());

        /* If the new role is enrolled, individual clustergroup should be created */
        reset(courseUtil);
        when(courseUtil.canUpdateUserInCourse(
            eq(activeCourse.getId()),
            argThat(
                requestJson -> requestJson.getRelationAsEnum().equals(CourseRelation.enrolled)
            ),
            eq(getMockUser()),
            eq(HttpMethod.PATCH))).thenReturn(new CheckResult<>(HttpStatus.OK, "", adminUser));
        when(userUtil.getUserIfExists(userId)).thenReturn(user);
        when(commonDatabaseActions.createNewIndividualClusterGroup(activeCourse.getId(), user)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk());
        verify(commonDatabaseActions, times(1)).createNewIndividualClusterGroup(activeCourse.getId(), user);
        assertEquals(CourseRelation.enrolled, adminUser.getRelation());
        verify(courseUserRepository, times(1)).save(adminUser);
        adminUser.setRelation(CourseRelation.course_admin);
        /* If something goes wrong when creating individual cluster, return 500 */
        reset(commonDatabaseActions);
        when(commonDatabaseActions.createNewIndividualClusterGroup(anyLong(), any()))
            .thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isInternalServerError());

        /* If the user doesn't get found when trying to create individualadmin group should return 404 */
        when(userUtil.getUserIfExists(anyLong())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isNotFound());

        /* If the new role is course_admin, individual clustergroup should be deleted */
        reset(commonDatabaseActions);
        reset(courseUtil);
        when(courseUtil.canUpdateUserInCourse(
            eq(activeCourse.getId()),
            argThat(
                requestJson -> requestJson.getRelationAsEnum().equals(CourseRelation.course_admin)
            ),
            eq(getMockUser()),
            eq(HttpMethod.PATCH))).thenReturn(new CheckResult<>(HttpStatus.OK, "", enrolledUser));
        when(commonDatabaseActions.removeIndividualClusterGroup(activeCourse.getId(), userId)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminRequest))
            .andExpect(status().isOk());
        verify(commonDatabaseActions, times(1)).removeIndividualClusterGroup(activeCourse.getId(), userId);
        assertEquals(CourseRelation.course_admin, enrolledUser.getRelation());
        verify(courseUserRepository, times(1)).save(enrolledUser);
        enrolledUser.setRelation(CourseRelation.enrolled);

        /* If something goes wrong when deleting individual cluster, return 500 */
        reset(commonDatabaseActions);
        when(commonDatabaseActions.removeIndividualClusterGroup(anyLong(), anyLong()))
            .thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminRequest))
                .andExpect(status().isInternalServerError());

        /* If user can't be updated, return corresponding status */
        reset(courseUtil);
        when(courseUtil.canUpdateUserInCourse(anyLong(), any(), any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isIAmATeapot());

    }

    @Test
    public void testGetCourseMembers() throws Exception {
        CourseUserEntity courseUserEntity = new CourseUserEntity(1L, 1L, CourseRelation.enrolled);
        UserEntity user = new UserEntity("name", "surname", "email", UserRole.teacher, "id", "");
        UserReferenceWithRelation userJson = new UserReferenceWithRelation(
            new UserReferenceJson("name", "surname", 1L, ""),
            ""+CourseRelation.enrolled
        );
        List<CourseUserEntity> userList = List.of(courseUserEntity);
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/members";
        /* If user is in course, return members */
        when(courseUtil.getCourseIfUserInCourse(activeCourseJson.courseId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(activeCourse, CourseRelation.enrolled)));
        when(courseUserRepository.findAllMembers(activeCourseJson.courseId())).thenReturn(userList);
        when(userUtil.getUserIfExists(courseUserEntity.getUserId())).thenReturn(user);
        /* User is enrolled so studentNumber should be hidden */
        when(entityToJsonConverter.userEntityToUserReferenceWithRelation(user, CourseRelation.enrolled, true)).thenReturn(userJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));
        verify(entityToJsonConverter, times(1)).userEntityToUserReferenceWithRelation(user, CourseRelation.enrolled, true);

        /* If user is admin studentNumber should be visible */
        when(courseUtil.getCourseIfUserInCourse(activeCourseJson.courseId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(activeCourse, CourseRelation.course_admin)));
        when(entityToJsonConverter.userEntityToUserReferenceWithRelation(user, CourseRelation.enrolled, false)).thenReturn(userJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));
        verify(entityToJsonConverter, times(1)).userEntityToUserReferenceWithRelation(user, CourseRelation.enrolled, false);

        /* If user doesn't get found it gets filtered out */
        when(userUtil.getUserIfExists(anyLong())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));


        /* If user is not in course, or course not found or ... return corresponding status */
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isIAmATeapot());

    }

    @Test
    public void testGetCourseKey() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/joinKey";
        /* If user is admin and course exists, returns joinKey */
        activeCourse.setJoinKey("1908");
        when(courseUtil.getCourseIfAdmin(activeCourse.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", activeCourse));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().string("1908"));
        activeCourse.setJoinKey(null);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        /* If any check fails, return corresponding status */
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/joinKey"))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testGetAndCreateCourseKey() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/joinKey";
        /* If user is admin and course exists, update and returns joinKey */
        activeCourse.setJoinKey("1908");
        when(courseUtil.getCourseIfAdmin(activeCourse.getId(), getMockUser())).thenReturn(
            new CheckResult<>(HttpStatus.OK, "", activeCourse));
        mockMvc.perform(MockMvcRequestBuilders.put(url))
            .andExpect(status().isOk())
            .andExpect(content().string(not(equalTo(""))))
            .andExpect(content().string(not(equalTo("1908"))));
        assertNotEquals("1908", activeCourse.getJoinKey());
        verify(courseRepository, times(1)).save(activeCourse);

        /* If any check fails, return corresponding status */
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(
            new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(url))
            .andExpect(status().isIAmATeapot());

    }

    @Test
    public void testDeleteCourseKey() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/joinKey";
        /* If user is admin and course exists, update and returns joinKey */
        activeCourse.setJoinKey("1908");
        when(courseUtil.getCourseIfAdmin(activeCourse.getId(), getMockUser())).thenReturn(
            new CheckResult<>(HttpStatus.OK, "", activeCourse));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
        assertNull(activeCourse.getJoinKey());
        verify(courseRepository, times(1)).save(activeCourse);

        /* If any check fails, return corresponding status */
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(
            new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testCopyCourse() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + activeCourse.getId() + "/copy";
        CourseEntity copiedCourse = new CourseEntity("name", "description", 2024);
        CourseWithInfoJson copiedCourseJson = new CourseWithInfoJson(
            2L,
            "name",
            "description",
            new UserReferenceJson("", "", 0L, ""),
            new ArrayList<>(),
            "",
            "",
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            2024
        );
        /* If user is creator, can copy course */
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(activeCourse, CourseRelation.creator)));
        when(commonDatabaseActions.copyCourse(activeCourse, getMockUser().getId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", copiedCourse));
        when(courseUtil.getJoinLink(copiedCourse.getJoinKey(), ""+copiedCourse.getId())).thenReturn("");
        when(entityToJsonConverter.courseEntityToCourseWithInfo(copiedCourse, "", false)).thenReturn(copiedCourseJson);
        mockMvc.perform(MockMvcRequestBuilders.post(url))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(copiedCourseJson)));

        /* If something goes wrong when copying course, return corresponding status */
        when(commonDatabaseActions.copyCourse(activeCourse, getMockUser().getId())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(url))
            .andExpect(status().isIAmATeapot());

        /* If user isn't the creator, return 403 */
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(activeCourse, CourseRelation.course_admin)));
        mockMvc.perform(MockMvcRequestBuilders.post(url))
            .andExpect(status().isForbidden());

        /* If user isn't in course, or course not found return corresponding status code */
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.NOT_FOUND, "", new Pair<>(null, null)));
        mockMvc.perform(MockMvcRequestBuilders.post(url))
            .andExpect(status().isNotFound());

        /* If an unexpected error occurs, return 500 */
        when(courseUtil.getCourseIfUserInCourse(activeCourse.getId(), getMockUser())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.post(url))
            .andExpect(status().isInternalServerError());

    }

}