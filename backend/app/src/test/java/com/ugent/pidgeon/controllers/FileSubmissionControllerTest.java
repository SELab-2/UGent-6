package com.ugent.pidgeon.controllers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.sql.Timestamp;

@SpringBootTest
@AutoConfigureMockMvc
public class FileSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "testUser", roles = {"teacher"})
    public void testSubmitFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/project/1/submit")
                        .file(file)
                        .param("submissionTime", timestamp.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}