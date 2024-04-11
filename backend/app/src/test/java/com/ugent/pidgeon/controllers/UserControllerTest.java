package com.ugent.pidgeon.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import com.ugent.pidgeon.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest extends ControllerTest {

  @Mock
  private UserUtil userUtil;

  @InjectMocks
  private UserController userController;

  private UserEntity userEntity;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController)
        .defaultRequest(MockMvcRequestBuilders.get("/**")
            .with(request -> {
              request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
              return request;
            }))
        .build();
    userEntity = new UserEntity("name", "surname", "email", UserRole.student, "azureId");
  }

  @Test
  public void testGetUserById() throws Exception {
    when(userUtil.getUserIfExists(anyLong())).thenReturn(userEntity);
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USER_BASE_PATH + "/1"))
        .andExpect(status().isOk());

    when(userUtil.getUserIfExists(anyLong())).thenReturn(null);
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USER_BASE_PATH + "/1"))
        .andExpect(status().isNotFound());

    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USER_BASE_PATH + "/2"))
        .andExpect(status().isForbidden());
  }
}