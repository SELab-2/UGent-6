package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.model.json.CourseJson;
import com.ugent.pidgeon.model.json.CourseMemberRequestJson;
import com.ugent.pidgeon.model.json.UserIdJson;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public class CourseUtilTest {

  @Mock
  private CourseUserRepository courseUserRepository;

  @Mock
  private CourseRepository courseRepository;

  @InjectMocks
  private CourseUtil courseUtil;

  private UserEntity user;
  private CourseEntity course;
  private CourseUserEntity cuEnrolled;
  private CourseUserEntity cuAdmin;
  private CourseUserEntity cuCreator;

  @BeforeEach
  public void setUp() {
    user = new UserEntity("name", "surname", "email", UserRole.student, "azureid");
    user.setId(1L);
    course = new CourseEntity("name", "description");
    course.setId(1L);
    course.setJoinKey("key");
    cuEnrolled = new CourseUserEntity(1L, 1L, CourseRelation.enrolled);
    cuAdmin = new CourseUserEntity(1L, 2L, CourseRelation.course_admin);
    cuCreator = new CourseUserEntity(1L, 3L, CourseRelation.creator);

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetCourseIfAdmin() throws Exception {
    when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuAdmin));
    CheckResult<CourseEntity> result = courseUtil.getCourseIfAdmin(1L, user);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(course, result.getData());

    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuEnrolled));
    result = courseUtil.getCourseIfAdmin(1L, user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    assertEquals("User is not an admin of the course", result.getMessage());
  }

  @Test
  public void testGetCourseIfExists() throws Exception {
    when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
    CheckResult<CourseEntity> check = courseUtil.getCourseIfExists(1L);
    assertEquals(HttpStatus.OK, check.getStatus());
    assertEquals(course, check.getData());

    when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
    check = courseUtil.getCourseIfExists(1L);
    assertEquals(HttpStatus.NOT_FOUND, check.getStatus());
    assertEquals("Course not found", check.getMessage());
    assertNull(check.getData());
  }

  @Test
  public void testCanUpdateUserInCourse() throws Exception {
    CourseMemberRequestJson request = new CourseMemberRequestJson();
    request.setUserId(2L);
    request.setRelation(String.valueOf(CourseRelation.enrolled));
    when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuAdmin));
    CheckResult<CourseUserEntity> checkResult = courseUtil.canUpdateUserInCourse(
        1L, request, user, HttpMethod.PATCH
    );
    assertEquals(HttpStatus.OK, checkResult.getStatus());
  }

  @Test
  public void testCanLeaveCourse() throws Exception {
    when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuAdmin));
    CheckResult<CourseRelation> checkResult = courseUtil.canLeaveCourse(1L, user);
    assertEquals(HttpStatus.OK, checkResult.getStatus());
    assertEquals(CourseRelation.course_admin, checkResult.getData());
  }

  @Test
  public void testCanDeleteUser() throws Exception {
    UserIdJson request = new UserIdJson();
    request.setUserId(5L);
    when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuAdmin));
    CheckResult<CourseRelation> checkResult = courseUtil.canDeleteUser(
        1L, request, user
    );
    assertEquals(HttpStatus.OK, checkResult.getStatus());
    assertEquals(CourseRelation.course_admin, checkResult.getData());
  }

  @Test
  public void testGetJoinLink() throws Exception {
    String link = courseUtil.getJoinLink("key", "1");
    assertEquals("/api/courses/1/join/key", link);
    link = courseUtil.getJoinLink(null, "1");
    assertEquals("/api/courses/1/join", link);
  }

  @Test
  public void testCheckJoinLink() throws Exception {
    when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
    when(courseUserRepository.findById(any())).thenReturn(Optional.empty());
    CheckResult<CourseEntity> result = courseUtil.checkJoinLink(1L, "key", user);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(course, result.getData());

    result = courseUtil.checkJoinLink(1L, null, user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    assertEquals("Course requires a join key. Use /api/courses/1/join/{courseKey}",
        result.getMessage());
  }

  @Test
  public void testCheckCourseJson() throws Exception {
    CourseJson courseJson = new CourseJson("name", "description");
    CheckResult<Void> result = courseUtil.checkCourseJson(courseJson);
    assertEquals(HttpStatus.OK, result.getStatus());

    courseJson.setDescription(null);
    result = courseUtil.checkCourseJson(courseJson);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    assertEquals("name and description are required", result.getMessage());

    courseJson.setDescription("description");
    courseJson.setName("");
    result = courseUtil.checkCourseJson(courseJson);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    assertEquals("Name cannot be empty", result.getMessage());
  }
}
