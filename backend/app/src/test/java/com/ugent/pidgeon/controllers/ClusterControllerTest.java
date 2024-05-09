package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.json.GroupClusterJson;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.util.*;
import java.time.OffsetDateTime;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ClusterControllerTest extends ControllerTest{

    @Mock
    GroupClusterRepository groupClusterRepository;
    @Mock
    GroupRepository groupRepository;


    @Mock
    private ClusterUtil clusterUtil;
    @Mock
    private EntityToJsonConverter entityToJsonConverter;
    @Mock
    private CourseUtil courseUtil;
    @Mock
    private CommonDatabaseActions commonDatabaseActions;
    @Mock
    private GroupMemberController groupMemberController;
    @InjectMocks
    private ClusterController clusterController;

    private CourseEntity courseEntity;
    private GroupClusterEntity groupClusterEntity;
    private GroupEntity groupEntity;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(clusterController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> {
                            request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
                            return request;
                        }))
                .build();

        courseEntity = new CourseEntity("name", "description",2024);
        groupClusterEntity = new GroupClusterEntity(1L, 3, "clustername", 5);
        groupEntity = new GroupEntity("groupName", 1L);
    }

    @Test
    public void testGetClustersForCourse() throws Exception {
        when(courseUtil.getCourseIfUserInCourse(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(courseEntity, CourseRelation.enrolled)));
        when(groupClusterRepository.findClustersWithoutInvidualByCourseId(anyLong())).thenReturn(List.of(groupClusterEntity));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/clusters"))
                .andExpect(status().isOk());

        when(courseUtil.getCourseIfUserInCourse(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.COURSE_BASE_PATH + "/1/clusters"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateClusterForCourse() throws Exception {
        String request = "{\"name\": \"test\", \"capacity\": 20, \"groupCount\": 5}";
        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", courseEntity));
        when(clusterUtil.checkGroupClusterCreateJson(any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(groupClusterRepository.save(any())).thenReturn(groupClusterEntity);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        when(clusterUtil.checkGroupClusterCreateJson(any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isIAmATeapot());

        when(courseUtil.getCourseIfAdmin(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.COURSE_BASE_PATH + "/1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetCluster() throws Exception {
        when(clusterUtil.getGroupClusterEntityIfNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isOk());

        when(clusterUtil.getGroupClusterEntityIfNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isIAmATeapot());
    }

    //This function also tests doGroupClusterUpdate
    @Test
    public void testUpdateCluster() throws Exception {
        String request = "{\"name\": \"clustername\", \"capacity\": 20}";
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(clusterUtil.checkGroupClusterUpdateJson(any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        when(clusterUtil.checkGroupClusterUpdateJson(any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testFillCluster() throws Exception {
        String request = "{\"clusterGroupMembers\":{\"1\":[1,2,3],\"2\":[],\"3\":[4]}}";

        List<GroupJson> groupJsons = List.of(new GroupJson(3, 1L, "group 1", "groupclusterurl"));
        GroupClusterJson groupClusterJson = new GroupClusterJson(1L, "test cluster",
            3, 5, OffsetDateTime.now(), groupJsons, "courseurl");
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(clusterUtil.getGroupClusterEntityIfNotIndividual(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity))
            .thenReturn(groupClusterJson);
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH+"/1/fill")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isOk());

        when(commonDatabaseActions.removeGroup(anyLong()))
            .thenThrow(new RuntimeException("TEST ERROR"));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH+"/1/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isInternalServerError());

        // a group that is too big
        request = "{\"clusterGroupMembers\":{\"1\":[1,2,3,6],\"2\":[],\"3\":[4]}}";
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH+"/1/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
        // too many groups
        request = "{\"clusterGroupMembers\":{\"1\":[1,2,3],\"2\":[],\"3\":[4],\"4\":[],\"5\":[6],\"6\":[]}}";
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH+"/1/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());

        when(entityToJsonConverter.clusterEntityToClusterJson(groupClusterEntity))
            .thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH+"/1/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isNotFound());

        when(clusterUtil.getGroupClusterEntityIfNotIndividual(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH+"/1/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isIAmATeapot());

        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.UNAUTHORIZED, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.CLUSTER_BASE_PATH+"/1/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPatchCluster() throws Exception {
        String request = "{\"name\": null, \"capacity\": null}";
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any()))
                .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(clusterUtil.checkGroupClusterUpdateJson(any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.CLUSTER_BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isIAmATeapot());
    }

    @Test
    public void testDeleteCluster() throws Exception {
        when(clusterUtil.canDeleteCluster(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(commonDatabaseActions.deleteClusterById(anyLong())).thenReturn(new CheckResult<>(HttpStatus.OK,"", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isNoContent());

        when(commonDatabaseActions.deleteClusterById(anyLong())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT,"", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isIAmATeapot());

        when(clusterUtil.canDeleteCluster(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN,"", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.CLUSTER_BASE_PATH + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateGroupForCluster() throws Exception {
        String request = "{\"name\": \"test\"}";
        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupClusterEntity));
        when(groupRepository.save(any())).thenReturn(groupEntity);
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.CLUSTER_BASE_PATH + "/1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(anyLong(), any())).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "", null));
        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.CLUSTER_BASE_PATH + "/1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

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
