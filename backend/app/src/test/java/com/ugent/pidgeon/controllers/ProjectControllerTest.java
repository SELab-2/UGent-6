package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.repository.DeadlineRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectControllerTest {

    private MockMvc mockMvc;

    private final User user = new User("Tester De Test", "Tester", "De Test",
            "test.testing@gtest.test", "123456");
    private final List<SimpleGrantedAuthority> authLijst = List.of(new SimpleGrantedAuthority("READ_AUTHORITY"));
    private final Auth auth = new Auth(user, authLijst);

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
    }

    @Test
    @WithMockUser(username="testuser", roles={"STUDENT"})
    void testGetProjects() throws Exception {
        // Mock data
        List<ProjectEntity> projects = new ArrayList<>();
        ProjectEntity project = new ProjectEntity();
        project.setId(1L);
        project.setName("Project 1");
        projects.add(project);

        // Mock repository behavior
        when(projectRepository.findProjectsByUserId(anyLong())).thenReturn(projects);

        // Perform GET request with the mock Auth object
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response content
        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).isNotNull();

        // Add assertions to verify the response content
        assertThat(responseContent).contains("\"name\":\"Project 1\"");
    }

    @Test
    void testGetProjectById() throws Exception {
        // Mock data
        ProjectEntity project = new ProjectEntity();
        project.setId(1L);
        project.setName("Project 1");

        // Mock repository behavior
        when(projectRepository.findById(anyLong())).thenReturn(java.util.Optional.of(project));

        // Perform GET request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response content
        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).isNotNull();

        // Add assertions to verify the response content
        assertThat(responseContent).contains("\"name\":\"Project 1\"");
    }

    // Add more tests for other controller methods as needed
}
