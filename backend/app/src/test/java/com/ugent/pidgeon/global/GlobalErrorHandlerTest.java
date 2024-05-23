package com.ugent.pidgeon.global;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.controllers.ControllerTest;
import com.ugent.pidgeon.controllers.UserController;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;



@ExtendWith(MockitoExtension.class)
public class GlobalErrorHandlerTest extends ControllerTest {

    @Mock
    private UserUtil userUtil;

    @InjectMocks
    private UserController userController;


    @BeforeEach
    public void setUp() {
      setUpController(userController);
    }

    @Test
    public void testHandleHttpMessageNotReadableException() throws Exception {
      setMockUserRoles(UserRole.admin);
      mockMvc.perform(MockMvcRequestBuilders.put(ApiRoutes.USERS_BASE_PATH + "/1")
          .contentType("application/json")
          .content("")
      ).andExpect(status().isBadRequest());
    }

    @Test
    public void testHandleNoHandlerFound() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get("/api/doesntexist", 1L)
      ).andExpect(status().isNotFound());
    }

    @Test
    public void testHandleMethodNotSupportedException() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.multipart(ApiRoutes.USERS_BASE_PATH + "/1")
      ).andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testHandleMethodArgumentTypeMismatchException() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USERS_BASE_PATH + "/string")
      ).andExpect(status().isBadRequest());
    }

    @Test
    public void testUnexpectedException() throws Exception {
      when(userUtil.getUserIfExists(anyLong())).thenThrow(new RuntimeException("Unexpected exception"));
      mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USERS_BASE_PATH + "/1")
      ).andExpect(status().isInternalServerError());
    }

}
