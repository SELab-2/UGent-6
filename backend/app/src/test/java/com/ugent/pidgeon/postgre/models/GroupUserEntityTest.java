package com.ugent.pidgeon.postgre.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupUserEntityTest {

  private GroupUserEntity groupUserEntity;

  @BeforeEach
  public void setUp() {
    groupUserEntity = new GroupUserEntity();
  }

  @Test
  public void testGroupId() {
    long groupId = 1L;
    groupUserEntity.setGroupId(groupId);
    assertEquals(groupId, groupUserEntity.getGroupId());
  }

  @Test
  public void testUserId() {
    long userId = 1L;
    groupUserEntity.setUserId(userId);
    assertEquals(userId, groupUserEntity.getUserId());
  }

  @Test
  public void testConstructor() {
    long groupId = 1L;
    long userId = 1L;
    GroupUserEntity groupUser = new GroupUserEntity(groupId, userId);
    assertEquals(groupId, groupUser.getGroupId());
    assertEquals(userId, groupUser.getUserId());
  }
}