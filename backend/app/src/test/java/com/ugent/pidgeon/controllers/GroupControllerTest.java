package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class GroupControllerTest extends ControllerTest {
    @Mock
    private GroupRepository groupRepository;

    @Mock
    GroupClusterRepository groupClusterRepository;

    @InjectMocks
    private GroupController groupController;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
    }


    private GroupEntity mockGroup() {
        GroupEntity group = new GroupEntity();
         group.setId(1L);
         group.setName("New Group Name");
        group.setClusterId(1);
        return group;
    }


//    @Test
//    public void getGroupByIdReturnsGroupWhenGroupExistsAndUserHasAccess() throws Exception {
//        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(mockGroup()));
//        when(groupRepository.userAccessToGroup(anyLong(), anyLong())).thenReturn(true);
//
//        long groupId = 1L;
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.GROUP_BASE_PATH + "/" + groupId))
//                .andExpect(status().isOk())
//                .andExpect(content().string(Matchers.not(Matchers.emptyString())));
//    }
//
//    @Test
//    public void getGroupByIdReturnsNotFoundWhenGroupDoesNotExist() throws Exception {
//        when(groupRepository.findById(anyLong())).thenReturn(Optional.empty());
//
//        long groupId = 1L;
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.GROUP_BASE_PATH + "/" + groupId))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void getGroupByIdReturnsForbiddenWhenUserDoesNotHaveAccess() throws Exception {
//        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(mockGroup()));
//        when(groupRepository.userAccessToGroup(anyLong(), anyLong())).thenReturn(false);
//
//        long groupId = 1L;
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.GROUP_BASE_PATH + "/" + groupId))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    public void updateGroupNameReturnsUpdatedGroupWhenGroupExistsAndUserHasAccess() throws Exception {
//        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(mockGroup()));
//        when(groupRepository.userAccessToGroup(anyLong(), anyLong())).thenReturn(true);
//
//        long groupId = 1L;
//        mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.GROUP_BASE_PATH + "/" + groupId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"name\":\"New Group Name\"}"))
//                .andExpect(status().isOk())
//                .andExpect(content().string(Matchers.not(Matchers.emptyString())));
//    }
//
//    @Test
//    public void deleteGroupReturnsNoContentWhenGroupExistsAndUserHasAccess() throws Exception {
//        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(mockGroup()));
//        when(groupRepository.userAccessToGroup(anyLong(), anyLong())).thenReturn(true);
//
//        long groupId = 1L;
//        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.GROUP_BASE_PATH + "/" + groupId))
//                .andExpect(status().isNoContent());
//    }

}
