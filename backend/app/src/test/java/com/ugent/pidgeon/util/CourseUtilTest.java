package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

public class CourseUtilTest {

  @InjectMocks
  private CourseUtil courseUtil;

  @Mock
  private CourseRepository courseRepository;

  @Mock
  private CourseUserRepository courseUserRepository;

  @Mock
  private CourseUserEntity courseUserEntity;


  @BeforeEach
  public void setUp() {
    courseUtil = new CourseUtil(); // Create a new CourseUtil instance
    courseRepository = Mockito.mock(CourseRepository.class);
    courseUserRepository = Mockito.mock(CourseUserRepository.class);
    courseUserEntity = new CourseUserEntity(1L, 1L ,CourseRelation.course_admin);
    MockitoAnnotations.openMocks(this); // Initialize mocks
  }

  @Test
  public void testGetCourseIfAdmin_Success() {
    // Mock data
    long courseId = 1L;
    UserEntity user = new UserEntity(); // Create a user entity
    user.setId(1L);
    user.setRole(UserRole.admin); // Set user role to admin
    CourseUserId courseUserId = new CourseUserId(1L,1L);
    CourseEntity courseEntity = new CourseEntity(); // Create a course entity

    // Mock repository behavior
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
    when(courseUserRepository.findById(courseUserId)).thenReturn(Optional.of(courseUserEntity));
    when(courseUserRepository.findByCourseIdAndUserId(courseId, user.getId()))
        .thenReturn(Optional.ofNullable(courseUserEntity));

    // Call the method
    CheckResult<CourseEntity> result = courseUtil.getCourseIfAdmin(courseId, user);

    // Assert results
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(courseEntity, result.getData());
  }

  // Add more test cases for different scenarios (e.g., not admin, course not found)
}







