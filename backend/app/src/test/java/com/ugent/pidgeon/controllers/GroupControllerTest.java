package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.json.GroupJson;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    private ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();

    @InjectMocks
    private GroupController groupController;

    private GroupEntity groupEntity;
    private GroupJson groupJson;
    private Integer capacity = 40;


    @BeforeEach
    public void setup() {
        setUpController(groupController);
        groupEntity = new GroupEntity("Group test", 1L);
        groupEntity.setId(5L);
        groupJson = new GroupJson(
            capacity,
            groupEntity.getId(),
            groupEntity.getName(),
            ""
        );
    }

    @Test
    public void testGetGroupById() throws Exception {
        String url = ApiRoutes.GROUP_BASE_PATH + "/" + groupEntity.getId();
        /* If group exists and users has acces, return groupJson */
        when(groupUtil.getGroupIfExists(groupEntity.getId()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));
        when(groupUtil.canGetGroup(groupEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        /* User is admin, student number should not be hidden */
        when(groupUtil.isAdminOfGroup(groupEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
        when(entityToJsonConverter.groupEntityToJson(groupEntity, false)).thenReturn(groupJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(groupJson)));
        verify(entityToJsonConverter, times(1)).groupEntityToJson(groupEntity, false);

        /* User is not admin, student number should be hidden */
        when(groupUtil.isAdminOfGroup(groupEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        when(entityToJsonConverter.groupEntityToJson(groupEntity, true)).thenReturn(groupJson);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(groupJson)));
        verify(entityToJsonConverter, times(1)).groupEntityToJson(groupEntity, true);


        /* If the user doesn't have acces to group, return forbidden */
        when(groupUtil.canGetGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isBadRequest());

        /* If group doesn't exist, return not found */
        when(groupUtil.getGroupIfExists(anyLong()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(status().isIAmATeapot());
    }

    //this function also fully tests doGroupNameUpdate
    @Test
    public void testUpdateGroupName() throws Exception {
        String url = ApiRoutes.GROUP_BASE_PATH + "/" + groupEntity.getId();
        /* If all checks pass, update and return groupJson */
        String request = "{\"name\":\"Test Group\"}\n";
        when(groupUtil.canUpdateGroup(groupEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk());
        assertEquals(groupEntity.getName(), "Test Group");

        /* If user can't update group, return corresponding status */
        when(groupUtil.canUpdateGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isIAmATeapot());

        /* If name isn't provided, return bad request */
        request = "{\"name\":\"\"}\n";
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());

        request = "{\"name\":null}\n";
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testPatchGroupName() throws Exception {
        String url = ApiRoutes.GROUP_BASE_PATH + "/" + groupEntity.getId();
        /* If all checks pass, update and return groupJson */
        String request = "{\"name\":\"Test Group\"}\n";
        when(groupUtil.canUpdateGroup(groupEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk());
        assertEquals(groupEntity.getName(), "Test Group");

        /* If user can't update group, return corresponding status */
        when(groupUtil.canUpdateGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isIAmATeapot());

        /* If name isn't provided, return bad request */
        request = "{\"name\":\"\"}\n";
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());

        request = "{\"name\":null}\n";
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteGroup() throws Exception {
        String url = ApiRoutes.GROUP_BASE_PATH + "/" + groupEntity.getId();
        /* If all checks pass, delete and return groupJson */
        when(groupUtil.canUpdateGroup(groupEntity.getId(), getMockUser()))
            .thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));
        when(commonDatabaseActions.removeGroup(groupEntity.getId())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isNoContent());

        /* If something goes wrong while deleting, return internal server error */
        when(commonDatabaseActions.removeGroup(groupEntity.getId())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isInternalServerError());

        /* If user can't update group, return corresponding status */
        when(groupUtil.canUpdateGroup(anyLong(), any()))
            .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
            .andExpect(status().isIAmATeapot());
    }
}

























