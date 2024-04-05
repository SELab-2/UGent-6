package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.repository.GroupMemberRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.UserRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
public class GroupMembersControllerTest extends ControllerTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupMemberController groupMemberController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupMemberController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
    }

//    @Test
//    public void removeMemberFromGroupReturnsNoContentWhenGroupExistsAndUserHasAccess() throws Exception {
//        when(groupRepository.userInGroup(anyLong(), anyLong())).thenReturn(true);
//        when(groupMemberRepository.removeMemberFromGroup(anyLong(), anyLong())).thenReturn(1);
//
//
//
//
//        mockMvc.perform(MockMvcRequestBuilders.delete(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}","1") + "/1"))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    public void addMemberToGroupReturnsOkWhenGroupExistsAndUserHasAccess() throws Exception {
//        when(userRepository.existsById(anyLong())).thenReturn(true);
//        when(groupRepository.userInGroup(anyLong(), anyLong())).thenReturn(false);
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1"))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"memberId\": 1}"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void findAllMembersByGroupIdReturnsOkWhenGroupExists() throws Exception {
//        List<UserEntity> members = Arrays.asList(new UserEntity(), new UserEntity());
//        when(groupMemberRepository.findAllMembersByGroupId(anyLong())).thenReturn(members);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.GROUP_MEMBER_BASE_PATH.replace("{groupid}", "1")))
//                .andExpect(status().isOk());
//    }
}
