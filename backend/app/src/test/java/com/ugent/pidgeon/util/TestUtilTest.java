package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestUtilTest {

  @Mock
  private TestRepository testRepository;

  @Mock
  private ProjectUtil projectUtil;

  @InjectMocks
  private TestUtil testUtil;

  private TestEntity testEntity;
  private ProjectEntity projectEntity;
  private UserEntity userEntity;

  @BeforeEach
  public void setUp() {
    testEntity = new TestEntity();
    testEntity.setId(1L);
    projectEntity = new ProjectEntity();
    projectEntity.setId(1L);
    userEntity = new UserEntity();
    userEntity.setId(1L);
  }

  @Test
  public void testGetTestIfExists() {
    when(testRepository.findByProjectId(anyLong())).thenReturn(Optional.of(testEntity));
    assertEquals(testEntity, testUtil.getTestIfExists(1L));

    when(testRepository.findByProjectId(anyLong())).thenReturn(Optional.empty());
    assertNull(testUtil.getTestIfExists(1L));
  }

  @Test
  public void testCheckForTestUpdate() {
    // Mock the projectUtil.getProjectIfAdmin method to return a CheckResult with HttpStatus.OK
    when(projectUtil.getProjectIfAdmin(anyLong(), any(UserEntity.class)))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", projectEntity));

    // Mock the testRepository.findByProjectId method to return an Optional of testEntity
    when(testRepository.findByProjectId(anyLong())).thenReturn(Optional.of(testEntity));

    // Call the checkForTestUpdate method
    CheckResult<Pair<TestEntity, ProjectEntity>> result = testUtil.checkForTestUpdate(1L,
        userEntity, "dockerImage", "", null, null, HttpMethod.POST);

    // Assert the result
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(testEntity, result.getData().getFirst());
    assertEquals(projectEntity, result.getData().getSecond());
  }

  @Test
  public void testGetTestIfAdmin() {
    // Mock the testRepository.findByProjectId method to return an Optional of testEntity
    when(testRepository.findByProjectId(anyLong())).thenReturn(Optional.of(testEntity));

    // Mock the projectUtil.isProjectAdmin method to return a CheckResult with HttpStatus.OK
    when(projectUtil.isProjectAdmin(anyLong(), any(UserEntity.class)))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

    // Call the getTestIfAdmin method
    CheckResult<TestEntity> result = testUtil.getTestIfAdmin(1L, userEntity);

    // Assert the result
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(testEntity, result.getData());
  }
}