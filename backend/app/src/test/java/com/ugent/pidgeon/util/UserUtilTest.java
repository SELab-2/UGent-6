package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.json.UserUpdateJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserUtilTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserUtil userUtil;

  private UserEntity user;

  @BeforeEach
  public void setUp() {
    user = new UserEntity("name", "surname", "email", UserRole.student, "azureid");
    user.setId(1L);
  }

  @Test
  public void testUserExists() {
    when(userRepository.existsById(anyLong())).thenReturn(true);
    assertTrue(userUtil.userExists(1L));

    when(userRepository.existsById(anyLong())).thenReturn(false);
    assertFalse(userUtil.userExists(1L));
  }

  @Test
  public void testGetUserIfExists() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
    assertEquals(user, userUtil.getUserIfExists(1L));

    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
    assertNull(userUtil.getUserIfExists(1L));
  }

  @Test
  public void testCheckForUserUpdateJson() {
    UserUpdateJson json = new UserUpdateJson();
    json.setName("newName");
    json.setSurname("newSurname");
    json.setEmail("newEmail@example.com");
    json.setRole("student");

    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
    CheckResult<UserEntity> result = userUtil.checkForUserUpdateJson(1L, json);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(user, result.getData());

    json.setEmail("invalidEmail");
    result = userUtil.checkForUserUpdateJson(1L, json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    assertEquals("Email is not valid", result.getMessage());
  }
}