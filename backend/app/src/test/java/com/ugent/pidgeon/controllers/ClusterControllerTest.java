package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.GroupUserRepository;
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

        courseEntity = new CourseEntity("name", "description");
        groupClusterEntity = new GroupClusterEntity(1L, 20, "clustername", 5);
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
