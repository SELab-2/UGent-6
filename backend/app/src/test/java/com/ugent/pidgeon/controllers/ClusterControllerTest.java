package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.json.GroupClusterJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupMemberRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.util.*;
import java.time.OffsetDateTime;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ClusterControllerTest extends ControllerTest{

    @Mock
    GroupClusterRepository groupClusterRepository;
    @Mock
    GroupRepository groupRepository;
    @Mock
    GroupMemberRepository groupMemberRepository;


    @Mock
    private ClusterUtil clusterUtil;
    @Mock
    private EntityToJsonConverter entityToJsonConverter;

    @Mock
    private CourseUtil courseUtil;
    @Mock
    private CommonDatabaseActions commonDatabaseActions;

    @InjectMocks
    private ClusterController clusterController;

    private CourseEntity courseEntity;
    private GroupClusterEntity groupClusterEntity;
    private GroupClusterJson groupClusterJson;
    private GroupEntity groupEntity;
    private GroupJson groupJson;
    private final Long courseId = 1L;

    private ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();

    @BeforeEach
    public void setup() {
        setUpController(clusterController);

        courseEntity = new CourseEntity("name", "description",2024);
        courseEntity.setId(32L);
        groupClusterEntity = new GroupClusterEntity(courseEntity.getId(), 20, "clustername", 5);
        groupClusterEntity.setId(29L);
        groupClusterJson = new GroupClusterJson(
            groupClusterEntity.getId(),
            groupClusterEntity.getName(),
            groupClusterEntity.getMaxSize(),
            groupClusterEntity.getGroupAmount(),
            OffsetDateTime.now(),
            Collections.emptyList(),
            "");
        groupEntity = new GroupEntity("groupName", 1L);
        groupEntity.setId(78L);
        groupJson = new GroupJson(groupClusterEntity.getMaxSize(), groupEntity.getId(), groupEntity.getName(), "");
    }

    @Test
    public void testGetClustersForCourse() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + courseId  + "/clusters";

        /* If the user is enrolled in the course, the clusters are returned */
        when(courseUtil.getCourseIfUserInCourse(courseId, getMockUser()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, CourseRelation.enrolled)));
        when(groupClusterRepository.findClustersWithoutInvidualByCourseId(courseId)).thenReturn(List.of(groupClusterEntity));
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity, true)).thenReturn(groupClusterJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(groupClusterJson))));

        verify(entityToJsonConverter, times(1)).clusterEntityToClusterJson(groupClusterEntity, true);


        /* If user is course_admin, studentnumber isn't hidden */
        when(courseUtil.getCourseIfUserInCourse(courseId, getMockUser()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, CourseRelation.course_admin)));
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity, false)).thenReturn(groupClusterJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(groupClusterJson))));

        verify(entityToJsonConverter, times(1)).clusterEntityToClusterJson(groupClusterEntity, false);

        /* If a certain check fails, the corresponding status code is returned */
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateClusterForCourse() throws Exception {
        String url = ApiRoutes.COURSE_BASE_PATH + "/" + courseId +"/clusters";

        /* If the user is an admin of the course and the json is valid, the cluster is created */
        String request = "{\"name\": \"test\", \"capacity\": 20, \"groupCount\": 5}";
        when(courseUtil.getCourseIfAdmin(courseId, getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", courseEntity));
        when(clusterUtil.checkGroupClusterCreateJson(argThat(
                json -> json.name().equals("test") && json.capacity().equals(20) && json.groupCount().equals(5)
        ))).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(groupClusterRepository.save(any())).thenReturn(groupClusterEntity);
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity, false)).thenReturn(groupClusterJson);
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(groupClusterJson)));

        /* If the json is invalid, the corresponding status code is returned */
        reset(clusterUtil);
        when(clusterUtil.checkGroupClusterCreateJson(any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isIAmATeapot());

        /* If the user is not an admin of the course, the corresponding status code is returned */
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetCluster() throws Exception {
        String url = ApiRoutes.CLUSTER_BASE_PATH + "/" + groupClusterEntity.getId();

        /* If the user has acces to the cluster and it isn't an individual cluster, the cluster is returned */
        /* User is not an admin, studentNumber should be hidden */
        when(courseUtil.getCourseIfAdmin(courseEntity.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", courseEntity));
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity, true)).thenReturn(groupClusterJson);
        when(clusterUtil.getGroupClusterEntityIfNotIndividual(groupClusterEntity.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(groupClusterJson)));

        verify(entityToJsonConverter, times(1)).clusterEntityToClusterJson(groupClusterEntity, true);

        /* User is an admin, studentNumber should be visible */
        when(courseUtil.getCourseIfAdmin(courseEntity.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", courseEntity));
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity, false)).thenReturn(groupClusterJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(groupClusterJson)));

        verify(entityToJsonConverter, times(1)).clusterEntityToClusterJson(groupClusterEntity, false);

        /* If any check fails, the corresponding status code is returned */
        when(clusterUtil.getGroupClusterEntityIfNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isIAmATeapot());
    }

    //This function also tests doGroupClusterUpdate
    @Test
    public void testUpdateCluster() throws Exception {
        String url = ApiRoutes.CLUSTER_BASE_PATH + "/" + groupClusterEntity.getId();
        String request = "{\"name\": \"newclustername\", \"capacity\": 22}";
        String originalname = groupClusterEntity.getName();
        Integer originalcapacity = groupClusterEntity.getMaxSize();
        /* If the user is an admin of the cluster, the cluster isn't individual and the json is valid, the cluster is updated */
        GroupClusterEntity copy = new GroupClusterEntity(1L, 20, "newclustername", 5);
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(clusterUtil.checkGroupClusterUpdateJson(
                argThat(json -> json.getName().equals("newclustername") && json.getCapacity().equals(22))
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        copy.setName("newclustername");
        GroupClusterJson updated = new GroupClusterJson(1L, "newclustername", 20, 5, OffsetDateTime.now(), Collections.emptyList(), "");
        when(groupClusterRepository.save(groupClusterEntity)).thenReturn(copy);
        when(entityToJsonConverter.clusterEntityToClusterJson(copy, false)).thenReturn(updated);
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));
        assertNotEquals(originalname, groupClusterEntity.getName());
        assertNotEquals(originalcapacity, groupClusterEntity.getMaxSize());

        /* If the json is invalid, the corresponding status code is returned */
        reset(clusterUtil);
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(clusterUtil.checkGroupClusterUpdateJson(any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        /* If the user is not an admin of the cluster or the cluster is individual, the corresponding status code is returned */
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testFillCluster() throws Exception {
        String url = ApiRoutes.CLUSTER_BASE_PATH + "/" + groupClusterEntity.getId() + "/fill";
        String request = """
            {
            		"group1": [3, 2],
                "group2": [4, 5]
            }
        """;

        long newGroupEntityId  = 89L;
        GroupEntity newGroupEntity = new GroupEntity("group1", groupClusterEntity.getId());
        newGroupEntity.setId(newGroupEntityId);
        long newGroupEntityId2 = 221L;
        GroupEntity newGroupEntity2 = new GroupEntity("group2", groupClusterEntity.getId());
        newGroupEntity2.setId(newGroupEntityId2);
        /* All checks succeed */
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));

        when(groupRepository.findAllByClusterId(groupClusterEntity.getId())).thenReturn(List.of(groupEntity));
        when(clusterUtil.checkFillClusterJson(argThat(
            json -> {
                boolean check = json.getClusterGroupMembers().size() == 2;
                check = check && json.getClusterGroupMembers().get("group1").length == 2;
                check = check && json.getClusterGroupMembers().get("group1")[0] == 3;
                check = check && json.getClusterGroupMembers().get("group1")[1] == 2;
                check = check && json.getClusterGroupMembers().get("group2").length == 2;
                check = check && json.getClusterGroupMembers().get("group2")[0] == 4;
                check = check && json.getClusterGroupMembers().get("group2")[1] == 5;
                return check;
            }
        ), eq(groupClusterEntity)))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

        when(groupRepository.save(argThat(
            g1 -> g1 != null && g1.getName().equals("group1") && g1.getClusterId() == groupClusterEntity.getId()
        ))).thenReturn(newGroupEntity);

        when(groupRepository.save(argThat(
            g2 -> g2 != null && g2.getName().equals("group2") && g2.getClusterId() == groupClusterEntity.getId()
        ))).thenReturn(newGroupEntity2);

        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(commonDatabaseActions, times(1)).removeGroup(groupEntity.getId());
        verify(groupMemberRepository, times(1)).addMemberToGroup(newGroupEntityId, 2);
        verify(groupMemberRepository, times(1)).addMemberToGroup(newGroupEntityId, 3);
        verify(groupMemberRepository, times(1)).addMemberToGroup(newGroupEntityId2, 4);
        verify(groupMemberRepository, times(1)).addMemberToGroup(newGroupEntityId2, 5);
        assertEquals(2, groupClusterEntity.getGroupAmount());
        verify(groupClusterRepository, times(1)).save(groupClusterEntity);

        /* Error when checking json */
        reset(clusterUtil);
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(clusterUtil.checkFillClusterJson(any(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isIAmATeapot());

        /* Error when getting group cluster entity */
        reset(clusterUtil);
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isIAmATeapot());

        /* Unexepcted error */
        reset(clusterUtil);
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(groupRepository.findAllByClusterId(groupClusterEntity.getId())).thenThrow(new RuntimeException());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isInternalServerError());

    }

    @Test
    public void testPatchCluster() throws Exception {
        String url = ApiRoutes.CLUSTER_BASE_PATH + "/" + groupClusterEntity.getId();

        /* If the user is an admin of the cluster and the json is valid, the cluster is updated */
        String originalname = groupClusterEntity.getName();
        Integer originalcapacity = groupClusterEntity.getMaxSize();
            /* If fields are null they are not updated */
        String request = "{\"name\": null, \"capacity\": null}";
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(clusterUtil.checkGroupClusterUpdateJson(
                argThat(json -> json.getName() == groupClusterEntity.getName() && json.getCapacity() == groupClusterEntity.getMaxSize())
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(groupClusterRepository.save(groupClusterEntity)).thenReturn(groupClusterEntity);
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity, false)).thenReturn(groupClusterJson);
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(groupClusterJson)));
        assertEquals(originalname, groupClusterEntity.getName());
        assertEquals(originalcapacity, groupClusterEntity.getMaxSize());

            /* If fields are not null they are updated */
        request = "{\"name\": \"newclustername\", \"capacity\": 22}";
        reset(clusterUtil);
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        GroupClusterEntity copy = new GroupClusterEntity(1L, 20, "newclustername", 5);
        when(clusterUtil.checkGroupClusterUpdateJson(
                argThat(json -> json.getName().equals("newclustername") && json.getCapacity().equals(22))
        )).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        GroupClusterJson updated = new GroupClusterJson(1L, "newclustername", 22, 5, OffsetDateTime.now(), Collections.emptyList(), "");
        when(groupClusterRepository.save(groupClusterEntity)).thenReturn(copy);
        when(entityToJsonConverter.clusterEntityToClusterJson(copy, false)).thenReturn(updated);
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));
        assertNotEquals(originalname, groupClusterEntity.getName());
        assertNotEquals(originalcapacity, groupClusterEntity.getMaxSize());

        /* If the json is invalid, the corresponding status code is returned */
        reset(clusterUtil);
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(clusterUtil.checkGroupClusterUpdateJson(any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        /* If the user is not an admin of the cluster or the cluster is individual, the corresponding status code is returned */
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testDeleteCluster() throws Exception {
        String url = ApiRoutes.CLUSTER_BASE_PATH + "/"  + groupClusterEntity.getId();

        /* If the user can delete the cluster, the cluster is deleted */
        when(clusterUtil.canDeleteCluster(groupClusterEntity.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(commonDatabaseActions.deleteClusterById(groupClusterEntity.getId())).thenReturn(new CheckResult<>(HttpStatus.OK,"", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isNoContent());

        /* If the delete fails, the corresponding status code is returned */
        when(commonDatabaseActions.deleteClusterById(anyLong())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT,"", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isIAmATeapot());

        /* If the user can't delete the cluster, the corresponding status code is returned */
        when(clusterUtil.canDeleteCluster(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN,"", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateGroupForCluster() throws Exception {
        String url = ApiRoutes.CLUSTER_BASE_PATH + "/" + groupClusterEntity.getId() + "/groups";
        String request = "{\"name\": \"test\"}";
        /* If the user is an admin of the cluster and the json is valid, the group is created */
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(groupClusterEntity.getId(), getMockUser())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(groupRepository.save(argThat(
                group -> group.getName().equals("test") && group.getClusterId() == groupClusterEntity.getId()
        ))).thenReturn(groupEntity);
        when(entityToJsonConverter.groupEntityToJson(groupEntity, false)).thenReturn(groupJson);
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(groupJson)));

        /* if the user is not an admin or the cluster is individual, the corresponding status code is returned */
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        /* If the json is invalid, the corresponding status code is returned */
        request = "{\"name\": \"\"}";
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
        request = "{\"name\": null}";
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}
