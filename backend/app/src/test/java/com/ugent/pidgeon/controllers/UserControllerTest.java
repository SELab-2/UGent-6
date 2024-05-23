package com.ugent.pidgeon.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.json.UserJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.UserUtil;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest extends ControllerTest {

  @Mock
  private UserUtil userUtil;

  @InjectMocks
  private UserController userController;

  private UserEntity userEntity;
  private UserJson userJson;
  private UserJson mockUserJson;
  private final ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();

  @BeforeEach
  public void setup() {
    setUpController(userController);
    userEntity = new UserEntity("Bob", "Testman", "email", UserRole.student, "azureId", "");
    userEntity.setId(74L);
    mockUserJson = new UserJson(getMockUser());
    userJson = new UserJson(userEntity);
  }

  @Test
  public void testGetUserById() throws Exception {
    String url = ApiRoutes.USERS_BASE_PATH + "/" + getMockUser().getId();
    String urlSomeoneElse = ApiRoutes.USERS_BASE_PATH + "/" + userEntity.getId();
    /* Can get ur own user information */
    when(userUtil.getUserIfExists(getMockUser().getId())).thenReturn(getMockUser());
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(mockUserJson)));

    /* Can't get someone else's user information */
    when(userUtil.getUserIfExists(anyLong())).thenReturn(userEntity);
    mockMvc.perform(MockMvcRequestBuilders.get(urlSomeoneElse))
        .andExpect(status().isForbidden());

    /* Admin can get someone else's user information */
    getMockUser().setRole(UserRole.admin);
    mockMvc.perform(MockMvcRequestBuilders.get(urlSomeoneElse))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(userJson)));

    /* If user not found return 404 */
    when(userUtil.getUserIfExists(anyLong())).thenReturn(null);
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USERS_BASE_PATH + "/999"))
        .andExpect(status().isNotFound());
  }

  private String createGetUsersUrl(String name, String surname, String email) {
    String start = ApiRoutes.USERS_BASE_PATH;
    boolean first = true;
    if (name != null) {
      start += "?name=" + name;
      first = false;
    }
    if (surname != null) {
      if (first) {
        start += "?surname=" + surname;
        first = false;
      } else {
        start += "&surname=" + surname;
      }
    }
    if (email != null) {
      if (first) {
        start += "?email=" + email;
        first = false;
      } else {
        start += "&email=" + email;
      }
    }
    return start;
  }

  @Test
  public void testGetUsersByNameOrSurname() throws Exception {
    setMockUserRoles(UserRole.admin);
    /* If email is present in the url, user gets returned based on email */
    String url = createGetUsersUrl(null, null, "email");
    when(userRepository.findByEmail("email")).thenReturn(userEntity);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    /* If email and name are present they need to match case insensitive */
    url = createGetUsersUrl("name", null, "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("[]"));

    url = createGetUsersUrl(userEntity.getName(), null, "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    url = createGetUsersUrl(userEntity.getName().toUpperCase(), null, "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    /* If email and surname are present they need to match case insensitive */
    url = createGetUsersUrl(null, "surname", "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("[]"));

    url = createGetUsersUrl(null, userEntity.getSurname(), "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    /* If all three are present they need to match case insensitive */
    url = createGetUsersUrl("name", "surname", "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("[]"));

    url = createGetUsersUrl(userEntity.getName(), userEntity.getSurname(), "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    url = createGetUsersUrl(userEntity.getName().toUpperCase(), userEntity.getSurname().toUpperCase(), "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    url = createGetUsersUrl(null, userEntity.getSurname().toUpperCase(), "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    /* If no user with email return empty list */
    when(userRepository.findByEmail("email")).thenReturn(null);
    url = createGetUsersUrl(null, null, "email");
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("[]"));

    /* If email isn't present in the url, users get returned based on name and surname */
    url = createGetUsersUrl("name", "surname", null);
    when(userRepository.findByName("name", "surname")).thenReturn(List.of(userEntity));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    /* If both name and surname are less than 3 characters, return empty list */
    url = createGetUsersUrl("na", "su", null);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("[]"));

    /* If one of the two is long enough, return the user */
    url = createGetUsersUrl("name", "su", null);
    when(userRepository.findByName("name", "su")).thenReturn(List.of(userEntity));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    /* If only name, return based on name, needs to be longer then 3 characters */
    url = createGetUsersUrl("name", null, null);
    when(userRepository.findByName("name", "")).thenReturn(List.of(userEntity));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    url = createGetUsersUrl("na", null, null);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("[]"));

    /* If only surname, return based on surname, needs to be longer then 3 characters */
    url = createGetUsersUrl(null, "surname", null);
    when(userRepository.findByName("", "surname")).thenReturn(List.of(userEntity));
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(List.of(userJson))));

    url = createGetUsersUrl(null, "su", null);
    mockMvc.perform(MockMvcRequestBuilders.get(url))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("[]"));

    /* Only admin can use this route */
    setMockUserRoles(UserRole.student);
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USERS_BASE_PATH))
        .andExpect(status().isForbidden());

    setMockUserRoles(UserRole.teacher);
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USERS_BASE_PATH))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testGetLoggedInUser() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.LOGGEDIN_USER_PATH))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(mockUserJson)));
  }

  @Test
  public void testUpdateUserById() throws Exception {
    setMockUserRoles(UserRole.admin);
    String url = ApiRoutes.USERS_BASE_PATH + "/" + userEntity.getId();
    String request = "{\"name\":\"John\",\"surname\":\"Doe\",\"email\":\"john@example.com\",\"role\":\"teacher\"}";
    UserEntity updateUserEntity = new UserEntity("John", "Doe", "john@example.com", UserRole.teacher, "azureId", "");
    updateUserEntity.setId(userEntity.getId());
    UserJson updatedUserJson = new UserJson(updateUserEntity);

    when(userUtil.checkForUserUpdateJson(eq(userEntity.getId()), argThat(
        json -> json.getName().equals("John") &&
            json.getSurname().equals("Doe") &&
            json.getEmail().equals("john@example.com") &&
            json.getRoleAsEnum().equals(UserRole.teacher)))
    )
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", userEntity));
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(updatedUserJson)));
    verify(userRepository, times(1)).save(userEntity);
    assertEquals("John", userEntity.getName());
    assertEquals("Doe", userEntity.getSurname());
    assertEquals("john@example.com", userEntity.getEmail());
    assertEquals(UserRole.teacher, userEntity.getRole());

    /* If updatecheck fails return corresponding status */
    reset(userUtil);
    when(userUtil.checkForUserUpdateJson(anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isIAmATeapot());

    /* Only admin can update user */
    setMockUserRoles(UserRole.student);
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isForbidden());

    setMockUserRoles(UserRole.teacher);
    mockMvc.perform(MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testPatchUserById() throws Exception {
    setMockUserRoles(UserRole.admin);
    String url = ApiRoutes.USERS_BASE_PATH + "/" + userEntity.getId();
    String request = "{\"name\":\"John\",\"surname\":\"Doe\",\"email\":\"john@example.com\",\"role\":\"teacher\"}";
    UserEntity updateUserEntity = new UserEntity("John", "Doe", "john@example.com", UserRole.teacher, "azureId", "");
    updateUserEntity.setId(userEntity.getId());
    UserJson updatedUserJson = new UserJson(updateUserEntity);
    String originalName = userEntity.getName();
    String originalSurname = userEntity.getSurname();
    String originalEmail = userEntity.getEmail();
    UserRole originalRole = userEntity.getRole();

    /* If all fields are present, update them all */
    when(userUtil.getUserIfExists(userEntity.getId())).thenReturn(userEntity);
    when(userUtil.checkForUserUpdateJson(eq(userEntity.getId()), argThat(
        json -> json.getName().equals("John") &&
            json.getSurname().equals("Doe") &&
            json.getEmail().equals("john@example.com") &&
            json.getRoleAsEnum().equals(UserRole.teacher)))
    )
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", userEntity));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(updatedUserJson)));
    verify(userRepository, times(1)).save(userEntity);
    assertEquals("John", userEntity.getName());
    assertEquals("Doe", userEntity.getSurname());
    assertEquals("john@example.com", userEntity.getEmail());
    assertEquals(UserRole.teacher, userEntity.getRole());
    userEntity.setName(originalName);
    userEntity.setSurname(originalSurname);
    userEntity.setEmail(originalEmail);
    userEntity.setRole(originalRole);


    /* If not all fields are present, update only the ones that are */
    request = "{\"name\":\"Tom\"}";
    reset(userUtil);
    when(userUtil.getUserIfExists(userEntity.getId())).thenReturn(userEntity);
    when(userUtil.checkForUserUpdateJson(eq(userEntity.getId()), argThat(
        json -> json.getName().equals("Tom") &&
            json.getSurname().equals(userEntity.getSurname()) &&
            json.getEmail().equals(userEntity.getEmail()) &&
            json.getRoleAsEnum().equals(userEntity.getRole())))
    )
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", userEntity));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());

    verify(userRepository, times(2)).save(userEntity);
    assertEquals("Tom", userEntity.getName());
    assertEquals(originalSurname, userEntity.getSurname());
    assertEquals(originalEmail, userEntity.getEmail());
    assertEquals(originalRole, userEntity.getRole());
    userEntity.setName(originalName);

    request = "{\"surname\":\"Riddle\"}";
    reset(userUtil);
    when(userUtil.getUserIfExists(userEntity.getId())).thenReturn(userEntity);
    when(userUtil.checkForUserUpdateJson(eq(userEntity.getId()), argThat(
        json -> json.getName().equals(userEntity.getName()) &&
            json.getSurname().equals("Riddle") &&
            json.getEmail().equals(userEntity.getEmail()) &&
            json.getRoleAsEnum().equals(userEntity.getRole())))
    )
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", userEntity));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk());

    verify(userRepository, times(3)).save(userEntity);
    assertEquals(originalName, userEntity.getName());
    assertEquals("Riddle", userEntity.getSurname());
    assertEquals(originalEmail, userEntity.getEmail());
    assertEquals(originalRole, userEntity.getRole());

    /* If updatecheck fails return corresponding status */
    reset(userUtil);
    when(userUtil.getUserIfExists(userEntity.getId())).thenReturn(userEntity);
    when(userUtil.checkForUserUpdateJson(anyLong(), any()))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isIAmATeapot());

    /* If user doesn't exist return 404 */
    when(userUtil.getUserIfExists(userEntity.getId())).thenReturn(null);
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isNotFound());

    /* Only admin can update user */
    setMockUserRoles(UserRole.student);
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isForbidden());

    setMockUserRoles(UserRole.teacher);
    mockMvc.perform(MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isForbidden());
  }

}





















