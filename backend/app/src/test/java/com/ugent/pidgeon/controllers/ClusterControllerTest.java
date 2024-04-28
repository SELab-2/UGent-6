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
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.GroupUserRepository;
import com.ugent.pidgeon.util.*;
import java.time.OffsetDateTime;
import java.util.Collections;
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
    GroupUserRepository groupUserRepository;


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

    private ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();

    @BeforeEach
    public void setup() {
        setUpController(clusterController);

        courseEntity = new CourseEntity("name", "description",2024);
        groupClusterEntity = new GroupClusterEntity(1L, 20, "clustername", 5);
        groupClusterJson = new GroupClusterJson(1L, "clustername", 20, 5, OffsetDateTime.now(), Collections.emptyList(), "");
        groupEntity = new GroupEntity("groupName", 1L);
        groupJson = new GroupJson(10, 1L, "Groupname", "");
    }

    @Test
    public void testGetClustersForCourse() throws Exception {

        /* If the user is enrolled in the course, the clusters are returned */
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, CourseRelation.enrolled)));
        when(groupClusterRepository.findClustersWithoutInvidualByCourseId(anyLong())).thenReturn(List.of(groupClusterEntity));
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity)).thenReturn(groupClusterJson);
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/clusters"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(groupClusterJson))));

        /* If a certain check fails, the corresponding status code is returned */
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/clusters"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateClusterForCourse() throws Exception {
        /* If the user is an admin of the course and the json is valid, the cluster is created */
        String request = "{\"name\": \"test\", \"capacity\": 20, \"groupCount\": 5}";
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", courseEntity));
        when(clusterUtil.checkGroupClusterCreateJson(any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(groupClusterRepository.save(any())).thenReturn(groupClusterEntity);
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity)).thenReturn(groupClusterJson);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(groupClusterJson)));

        /* If the json is invalid, the corresponding status code is returned */
        when(clusterUtil.checkGroupClusterCreateJson(any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isIAmATeapot());

        /* If the user is not an admin of the course, the corresponding status code is returned */
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetCluster() throws Exception {
        /* If the user has acces to the cluster and it isn't an individual cluster, the cluster is returned */
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity)).thenReturn(groupClusterJson);
        when(clusterUtil.getGroupClusterEntityIfNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(groupClusterJson)));

        /* If any check fails, the corresponding status code is returned */
        when(clusterUtil.getGroupClusterEntityIfNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isIAmATeapot());
    }

    //This function also tests doGroupClusterUpdate
    @Test
    public void testUpdateCluster() throws Exception {
        String request = "{\"name\": \"newclustername\", \"capacity\": 20}";
        /* If the user is an admin of the cluster, the cluster isn't individual and the json is valid, the cluster is updated */
        GroupClusterEntity copy = new GroupClusterEntity(1L, 20, "newclustername", 5);
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", copy));
        when(clusterUtil.checkGroupClusterUpdateJson(any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        copy.setName("newclustername");
        GroupClusterJson updated = new GroupClusterJson(1L, "newclustername", 20, 5, OffsetDateTime.now(), Collections.emptyList(), "");
        when(entityToJsonConverter.clusterEntityToClusterJson(copy)).thenReturn(updated);
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));

        /* If the json is invalid, the corresponding status code is returned */
        when(clusterUtil.checkGroupClusterUpdateJson(any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        /* If the user is not an admin of the cluster or the cluster is individual, the corresponding status code is returned */
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPatchCluster() throws Exception {

        /* If the user is an admin of the cluster and the json is valid, the cluster is updated */
        String originalname = groupClusterEntity.getName();
        Integer originalcapacity = groupClusterEntity.getMaxSize();
            /* If fields are null they are not updated */
        String request = "{\"name\": null, \"capacity\": null}";
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(clusterUtil.checkGroupClusterUpdateJson(any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(groupClusterRepository.save(groupClusterEntity)).thenReturn(groupClusterEntity);
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity)).thenReturn(groupClusterJson);
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(groupClusterJson)));
        assertEquals(originalname, groupClusterEntity.getName());
        assertEquals(originalcapacity, groupClusterEntity.getMaxSize());

            /* If fields are not null they are updated */
        request = "{\"name\": \"newclustername\", \"capacity\": 22}";
        GroupClusterEntity copy = new GroupClusterEntity(1L, 20, "newclustername", 5);
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        GroupClusterJson updated = new GroupClusterJson(1L, "newclustername", 22, 5, OffsetDateTime.now(), Collections.emptyList(), "");
        when(groupClusterRepository.save(groupClusterEntity)).thenReturn(copy);
        when(entityToJsonConverter.clusterEntityToClusterJson(copy)).thenReturn(updated);
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(updated)));
        assertNotEquals(originalname, groupClusterEntity.getName());
        assertNotEquals(originalcapacity, groupClusterEntity.getMaxSize());

        /* If the json is invalid, the corresponding status code is returned */
        when(clusterUtil.checkGroupClusterUpdateJson(any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        /* If the user is not an admin of the cluster or the cluster is individual, the corresponding status code is returned */
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testDeleteCluster() throws Exception {
        /* If the user can delete the cluster, the cluster is deleted */
        when(clusterUtil.canDeleteCluster(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(commonDatabaseActions.deleteClusterById(anyLong())).thenReturn(new CheckResult<>(HttpStatus.OK,"", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isNoContent());

        /* If the delete fails, the corresponding status code is returned */
        when(commonDatabaseActions.deleteClusterById(anyLong())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT,"", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isIAmATeapot());

        /* If the user can't delete the cluster, the corresponding status code is returned */
        when(clusterUtil.canDeleteCluster(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN,"", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateGroupForCluster() throws Exception {
        String request = "{\"name\": \"test\"}";
        /* If the user is an admin of the cluster and the json is valid, the group is created */
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(groupRepository.save(any())).thenReturn(groupEntity);
        when(entityToJsonConverter.groupEntityToJson(groupEntity)).thenReturn(groupJson);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.CLUSTER_BASE_PATH + "/1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(groupJson)));

        /* if the user is not an admin or the cluster is individual, the corresponding status code is returned */
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.CLUSTER_BASE_PATH + "/1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        /* If the json is invalid, the corresponding status code is returned */
        request = "{\"name\": \"\"}";
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.CLUSTER_BASE_PATH + "/1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
        request = "{\"name\": null}";
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.CLUSTER_BASE_PATH + "/1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}
