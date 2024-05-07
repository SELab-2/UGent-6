package com.ugent.pidgeon.global;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ugent.pidgeon.controllers.ControllerTest;
import com.ugent.pidgeon.controllers.UserController;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import com.ugent.pidgeon.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
      when(userUtil.getUserIfExists(anyLong())).thenThrow(new HttpMessageNotReadableException("test"));
      mockMvc.perform(MockMvcRequestBuilders.get("/api/users/1"))
          .andExpect(status().isBadRequest());
    }

}
