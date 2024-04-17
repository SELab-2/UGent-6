package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.CommonDatabaseActions;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.GroupUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class GroupControllerTest extends ControllerTest {
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupUtil groupUtil;
    @Mock
    private EntityToJsonConverter entityToJsonConverter;
    @Mock
    private CommonDatabaseActions commonDatabaseActions;

    @InjectMocks
    private GroupController groupController;

    private GroupEntity groupEntity;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
        groupEntity = new GroupEntity("Group test", 1L);
    }

    @Test
    public void testGetGroupById() throws Exception {
        when(groupUtil.getGroupIfExists(anyLong()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));
        when(groupUtil.canGetGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.GROUP_BASE_PATH + "/1"))
            .andExpect(status().isOk());

        when(groupUtil.canGetGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.GROUP_BASE_PATH + "/1"))
            .andExpect(status().isBadRequest());

        when(groupUtil.getGroupIfExists(anyLong()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.GROUP_BASE_PATH + "/1"))
            .andExpect(status().isIAmATeapot());
    }

    //this function also fully tests doGroupNameUpdate
    @Test
    public void testUpdateGroupName() throws Exception {
        String request = "{\"name\":\"Test Group\"}\n";
        when(groupUtil.canUpdateGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.GROUP_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk());

        when(groupUtil.canUpdateGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.GROUP_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isIAmATeapot());

        request = "{\"name\":\"\"}\n";
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.GROUP_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());

        request = "{\"name\":null}\n";
        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.GROUP_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testPatchGroupName() throws Exception {
        String request = "{\"name\":\"Test Group\"}\n";
        when(groupUtil.canUpdateGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));
        mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.GROUP_BASE_PATH + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk());
    }

    @Test
    public void testDeleteGroup() throws Exception {
        when(groupUtil.canUpdateGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.GROUP_BASE_PATH + "/1"))
            .andExpect(status().isNoContent());

        when(groupUtil.canUpdateGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.GROUP_BASE_PATH + "/1"))
            .andExpect(status().isBadRequest());
    }
}


























