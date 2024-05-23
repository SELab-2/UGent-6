package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.json.UserUpdateJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class UserUtilTest {

  @Mock
  private UserRepository userRepository;

  @Spy
  @InjectMocks
  private UserUtil userUtil;

  private UserEntity user;

  @BeforeEach
  public void setUp() {
    user = new UserEntity("name", "surname", "email", UserRole.student, "azureid", "");
    user.setId(87L);
  }

  @Test
  public void testUserExists() {
    /* The user exists */
    when(userRepository.existsById(user.getId())).thenReturn(true);
    assertTrue(userUtil.userExists(user.getId()));

    /* The user does not exist */
    when(userRepository.existsById(user.getId())).thenReturn(false);
    assertFalse(userUtil.userExists(user.getId()));
  }

  @Test
  public void testGetUserIfExists() {
    /* The user exists */
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    assertEquals(user, userUtil.getUserIfExists(user.getId()));

    /* The user does not exist */
    when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
    assertNull(userUtil.getUserIfExists(user.getId()));
  }

  @Test
  public void testCheckForUserUpdateJson() {
    UserUpdateJson json = new UserUpdateJson();
    json.setName("newName");
    json.setSurname("newSurname");
    json.setEmail("newEmail@example.com");
    json.setRole("student");

    /* All checks succeed */
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    CheckResult<UserEntity> result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(user, result.getData());

    /* Not a valid email */
    json.setEmail("invalidEmail");
    result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    json.setEmail("newEmail@example.com");

    /* Surname is blank */
    json.setSurname("");
    result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Name is blank */
    json.setSurname("newSurname");
    json.setName("");
    result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Role is not valid */
    json.setName("newName");
    json.setRole("invalidRole");
    result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Role is null */
    json.setRole(null);
    result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Email is null */
    json.setRole("student");
    json.setEmail(null);
    result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Surname is null */
    json.setEmail("email.email@email.email");
    json.setSurname(null);
    result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Name is null */
    json.setSurname("newSurname");
    json.setName(null);
    result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* User not found */
    when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
    result = userUtil.checkForUserUpdateJson(user.getId(), json);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }
}