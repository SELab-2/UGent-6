package com.ugent.pidgeon.global;


import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.controllers.ControllerTest;
import com.ugent.pidgeon.controllers.UserController;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.util.UserUtil;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(MockitoExtension.class)
public class RolesInterceptorTest  extends ControllerTest {

  @Mock
  private UserUtil userUtil;

  @InjectMocks
  private UserController userController;


  @BeforeEach
  public void setUp() {
    setUpController(userController);
  }

  @Test
  void testEverthingWorks() throws Exception {
    when(userUtil.getUserIfExists(getMockUser().getId())).thenReturn(getMockUser());
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USERS_BASE_PATH + "/1")
    ).andExpect(status().isOk());
  }

  @Test
  void testNotRequiredRole() throws Exception {
    setMockUserRoles(UserRole.student);
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USERS_BASE_PATH)
    ).andExpect(status().isForbidden());
  }

  @Test
  void adminSucceedsAllRoleCheck() throws Exception {
    setMockUserRoles(UserRole.admin);
    when(userUtil.getUserIfExists(getMockUser().getId())).thenReturn(getMockUser());
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USERS_BASE_PATH + "/1")
    ).andExpect(status().isOk());
  }

  @Test
  void testUserDoesntExistYet() throws Exception {
    reset(userRepository);
    when(userUtil.getUserIfExists(getMockUser().getId())).thenReturn(getMockUser());
    when(userRepository.findUserByAzureId(getMockUser().getAzureId())).thenReturn(Optional.empty());
    when(userRepository.save(argThat(
      user -> {
        Duration duration = Duration.between(user.getCreatedAt(), OffsetDateTime.now());
        return user.getRole() == UserRole.student &&
            user.getAzureId().equals(getMockUser().getAzureId()) &&
            user.getName().equals(getMockUser().getName()) &&
            user.getSurname().equals(getMockUser().getSurname()) &&
            user.getEmail().equals(getMockUser().getEmail()) &&
            duration.getSeconds() < 5;
      }
    ))).thenReturn(getMockUser());
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USERS_BASE_PATH + "/" + getMockUser().getId())
    ).andExpect(status().isOk());

  }


}
