package com.ugent.pidgeon.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.UserUtil;
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

  @Test
  public void testGetUserByAzureId() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USER_AUTH_PATH))
        .andExpect(status().isOk());
  }

  @Test
  public void testUpdateUserById() throws Exception {
    String request = "{\"name\":\"John\",\"surname\":\"Doe\",\"email\":\"john@example.com\",\"role\":\"admin\"}";
    when(userUtil.checkForUserUpdateJson(anyLong(), any())).
        thenReturn(new CheckResult<>(HttpStatus.OK, "", userEntity));
    mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.USER_BASE_PATH + "/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());

    when(userUtil.checkForUserUpdateJson(anyLong(), any())).
        thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.USER_BASE_PATH + "/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testPatchUserById() throws Exception {
    String request = "{\"name\": null,\"surname\": null,\"email\": null,\"role\": null}";
    when(userUtil.getUserIfExists(anyLong())).thenReturn(userEntity);
    when(userUtil.checkForUserUpdateJson(anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", userEntity));
    mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.USER_BASE_PATH + "/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());

    when(userUtil.checkForUserUpdateJson(anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.USER_BASE_PATH + "/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isBadRequest());

    when(userUtil.getUserIfExists(anyLong())).thenReturn(null);
    mockMvc.perform(MockMvcRequestBuilders.patch(ApiRoutes.USER_BASE_PATH + "/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isNotFound());
  }
}






















