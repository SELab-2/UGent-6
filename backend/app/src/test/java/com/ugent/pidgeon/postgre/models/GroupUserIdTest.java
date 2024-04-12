package com.ugent.pidgeon.postgre.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupUserIdTest {

  private GroupUserId groupUserId;

  @BeforeEach
  public void setUp() {
    groupUserId = new GroupUserId();
  }

  @Test
  public void testGroupId() {
    long groupId = 1L;
    groupUserId.setGroupId(groupId);
    assertEquals(groupId, groupUserId.getGroupId());
  }

  @Test
  public void testUserId() {
    long userId = 1L;
    groupUserId.setUserId(userId);
    assertEquals(userId, groupUserId.getUserId());
  }

  @Test
  public void testConstructor() {
    long groupId = 1L;
    long userId = 1L;
    GroupUserId groupUser = new GroupUserId(groupId, userId);
    assertEquals(groupId, groupUser.getGroupId());
    assertEquals(userId, groupUser.getUserId());
  }
}