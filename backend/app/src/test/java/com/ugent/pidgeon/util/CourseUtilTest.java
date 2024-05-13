package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.model.json.CourseJson;
import com.ugent.pidgeon.model.json.CourseMemberRequestJson;
import com.ugent.pidgeon.model.json.UserIdJson;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class CourseUtilTest {

  @Mock
  private CourseUserRepository courseUserRepository;

  @Mock
  private CourseRepository courseRepository;

  @Mock
  private UserUtil userUtil;

  @Spy
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
    user.setId(44L);
    course = new CourseEntity("name", "description",2024);
    course.setId(9L);
    course.setJoinKey("key");
    cuEnrolled = new CourseUserEntity(1L, 1L, CourseRelation.enrolled);
    cuAdmin = new CourseUserEntity(1L, 2L, CourseRelation.course_admin);
    cuCreator = new CourseUserEntity(1L, 3L, CourseRelation.creator);
  }

  @Test
  public void testGetCourseIfAdmin() {
    /* All checks succeed */
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair(course, CourseRelation.course_admin)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);

    CheckResult<CourseEntity> check = courseUtil.getCourseIfAdmin(course.getId(), user);
    assertEquals(HttpStatus.OK, check.getStatus());
    assertEquals(course, check.getData());

    /* User is not a course admin */
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair(course, CourseRelation.enrolled)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.getCourseIfAdmin(course.getId(), user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is not a course admin, but a platform admin */
    user.setRole(UserRole.admin);
    check = courseUtil.getCourseIfAdmin(course.getId(), user);
    assertEquals(HttpStatus.OK, check.getStatus());

    /* Get course fails */
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.getCourseIfAdmin(course.getId(), user);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, check.getStatus());
  }

  @Test
  public void testGetCourseIfUserInCourse() {
    /* All checks succeed */
    doReturn(new CheckResult<>(HttpStatus.OK, "", course)).when(courseUtil).getCourseIfExists(course.getId());
    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuEnrolled));
    CheckResult<Pair<CourseEntity, CourseRelation>> check = courseUtil.getCourseIfUserInCourse(course.getId(), user);
    assertEquals(HttpStatus.OK, check.getStatus());
    assertEquals(course, check.getData().getFirst());
    assertEquals(CourseRelation.enrolled, check.getData().getSecond());

    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuAdmin));
    check = courseUtil.getCourseIfUserInCourse(course.getId(), user);
    assertEquals(CourseRelation.course_admin, check.getData().getSecond());

    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuCreator));
    check = courseUtil.getCourseIfUserInCourse(course.getId(), user);
    assertEquals(CourseRelation.creator, check.getData().getSecond());

    /* User isn't in course */
    when(courseUserRepository.findById(any())).thenReturn(Optional.empty());
    check = courseUtil.getCourseIfUserInCourse(course.getId(), user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User isn't in course but is admin */
    user.setRole(UserRole.admin);
    check = courseUtil.getCourseIfUserInCourse(course.getId(), user);
    assertEquals(HttpStatus.OK, check.getStatus());

    /* Get course fails */
    reset(courseUtil);
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null))
        .when(courseUtil).getCourseIfExists(course.getId());
    check = courseUtil.getCourseIfUserInCourse(course.getId(), user);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, check.getStatus());
  }

  @Test
  public void testGetCourseIfExists() {
    reset(courseUtil);
    /* All checks succeed */
    when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
    CheckResult<CourseEntity> check = courseUtil.getCourseIfExists(course.getId());
    assertEquals(HttpStatus.OK, check.getStatus());
    assertEquals(course, check.getData());

    /* Course does not exist */
    when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());
    check = courseUtil.getCourseIfExists(course.getId());
    assertEquals(HttpStatus.NOT_FOUND, check.getStatus());
    assertNull(check.getData());
  }

  @Test
  public void testCanUpdateUserInCourse() {
    CourseMemberRequestJson request = new CourseMemberRequestJson();
    request.setUserId(5L);
    request.setRelation("course_admin");
    /* All checks succeed */
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair(course, CourseRelation.creator)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);

    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuAdmin));

    CheckResult<CourseUserEntity> check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.PATCH);
    assertEquals(HttpStatus.OK, check.getStatus());
    assertEquals(cuAdmin, check.getData());

    /* User is not creator but trying to add admin */
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair(course, CourseRelation.course_admin)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.PATCH);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is not creator but trying to downgrade admin */
    request.setRelation("enrolled");
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.PATCH);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is general admin and trying to add admin */
    request.setRelation("course_admin");
    user.setRole(UserRole.admin);
    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuEnrolled));
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.PATCH);
    assertEquals(HttpStatus.OK, check.getStatus());

    /* User is trying to change the creator */
    request.setRelation("creator");
    user.setRole(UserRole.teacher);
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair(course, CourseRelation.creator)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.PATCH);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is trying to change the creator as admin */
    user.setRole(UserRole.admin);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.PATCH);
    assertEquals(HttpStatus.OK, check.getStatus());
    user.setRole(UserRole.teacher);
    request.setRelation("enrolled");

    /* User is trying to change it's own role */
    request.setUserId(user.getId());
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.PATCH);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is trying to change it's own role as admin */
    user.setRole(UserRole.admin);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.PATCH);
    assertEquals(HttpStatus.OK, check.getStatus());
    user.setRole(UserRole.teacher);

    /* User isn't in course on patch */
    request.setUserId(5L);
    when(courseUserRepository.findById(any())).thenReturn(Optional.empty());
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.PATCH);
    assertEquals(HttpStatus.BAD_REQUEST, check.getStatus());

    /* Post everything succeeds */
    when(userUtil.userExists(request.getUserId())).thenReturn(true);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.POST);
    assertEquals(HttpStatus.OK, check.getStatus());

    /* User doesn't exist */
    when(userUtil.userExists(request.getUserId())).thenReturn(false);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.POST);
    assertEquals(HttpStatus.NOT_FOUND, check.getStatus());

    /* User is already in course on POST */
    when(courseUserRepository.findById(any())).thenReturn(Optional.of(cuEnrolled));
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.POST);
    assertEquals(HttpStatus.BAD_REQUEST, check.getStatus());

    /* Invalid relation */
    request.setRelation("invalid");
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.POST);
    assertEquals(HttpStatus.BAD_REQUEST, check.getStatus());

    /* Relation not present */
    request.setRelation(null);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.POST);
    assertEquals(HttpStatus.BAD_REQUEST, check.getStatus());

    /* UserId not present */
    request.setRelation("enrolled");
    request.setUserId(null);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.POST);
    assertEquals(HttpStatus.BAD_REQUEST, check.getStatus());

    /* User is not an admin */
    request.setUserId(5L);
    when(courseUserRepository.findById(any())).thenReturn(Optional.empty());
    when(userUtil.userExists(request.getUserId())).thenReturn(true);
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(course, CourseRelation.enrolled)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.POST);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is not in course but is admin */
    user.setRole(UserRole.admin);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.POST);
    assertEquals(HttpStatus.OK, check.getStatus());

    /* get course fails */
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.canUpdateUserInCourse(course.getId(), request, user, HttpMethod.POST);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, check.getStatus());

  }

  @Test
  public void testCanLeaveCourse() throws Exception {
    /* All checks succeed */
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(course, CourseRelation.enrolled)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    CheckResult<CourseRelation> check = courseUtil.canLeaveCourse(course.getId(), user);
    assertEquals(HttpStatus.OK, check.getStatus());
    assertEquals(CourseRelation.enrolled, check.getData());

    /* Course is archived */
    course.setArchivedAt(OffsetDateTime.now());
    check = courseUtil.canLeaveCourse(course.getId(), user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is course creator */
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(course, CourseRelation.creator)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.canLeaveCourse(course.getId(), user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* get course fails */
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.canLeaveCourse(course.getId(), user);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, check.getStatus());
  }

  @Test
  public void testCanDeleteUser() throws Exception {
    /* All checks succeed */
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(course, CourseRelation.creator)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    when(courseUserRepository.findById(argThat(
        arg -> arg.getCourseId() == (course.getId()) &&
            arg.getUserId() == cuEnrolled.getUserId())
    )).thenReturn(Optional.of(cuEnrolled));
    CheckResult<CourseRelation> check = courseUtil.canDeleteUser(course.getId(), cuEnrolled.getUserId(), user);
    assertEquals(HttpStatus.OK, check.getStatus());
    assertEquals(CourseRelation.enrolled, check.getData());

    /* User is course admin */
    reset(courseUserRepository);
    when(courseUserRepository.findById(
        argThat(arg -> arg.getCourseId() == (course.getId()) && arg.getUserId() == cuAdmin.getUserId())
    )).thenReturn(Optional.of(cuAdmin));
    check = courseUtil.canDeleteUser(course.getId(), cuAdmin.getUserId(), user);
    assertEquals(HttpStatus.OK, check.getStatus());

    /* User isn't course creator but tries to delete admin */
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(course, CourseRelation.course_admin)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.canDeleteUser(course.getId(), cuAdmin.getUserId(), user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is general admin and tries to delete admin */
    user.setRole(UserRole.admin);
    check = courseUtil.canDeleteUser(course.getId(), cuAdmin.getUserId(), user);
    assertEquals(HttpStatus.OK, check.getStatus());

    /* User tries to delete creator */
    reset(courseUserRepository);
    when(courseUserRepository.findById(
        argThat(arg -> arg.getCourseId() == (course.getId()) && arg.getUserId() == cuCreator.getUserId())
    )).thenReturn(Optional.of(cuCreator));
    check = courseUtil.canDeleteUser(course.getId(), cuCreator.getUserId(), user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User tries to delete itself */
    reset(courseUserRepository);
    when(courseUserRepository.findById(
        argThat(arg -> arg.getCourseId() == (course.getId()) && arg.getUserId() == user.getId())
    )).thenReturn(Optional.of(new CourseUserEntity(1L, 1L, CourseRelation.enrolled)));
    check = courseUtil.canDeleteUser(course.getId(), user.getId(), user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is trying to delete non-existing user */
    reset(courseUserRepository);
    when(courseUserRepository.findById(
        argThat(arg -> arg.getCourseId() == (course.getId()) && arg.getUserId() == cuEnrolled.getUserId())
    )).thenReturn(Optional.empty());
    check = courseUtil.canDeleteUser(course.getId(), cuEnrolled.getUserId(), user);
    assertEquals(HttpStatus.NOT_FOUND, check.getStatus());

    /* User is not an admin */
    user.setRole(UserRole.student);
    doReturn(new CheckResult<>(HttpStatus.OK, "", new Pair<>(course, CourseRelation.enrolled)))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.canDeleteUser(course.getId(), cuEnrolled.getUserId(), user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* User is not in course but is admin */
    user.setRole(UserRole.admin);
    reset(courseUserRepository);
    when(courseUserRepository.findById(argThat(
        arg -> arg.getCourseId() == (course.getId()) &&
            arg.getUserId() == cuEnrolled.getUserId())
    )).thenReturn(Optional.of(cuEnrolled));
    check = courseUtil.canDeleteUser(course.getId(), cuEnrolled.getUserId(), user);
    assertEquals(HttpStatus.OK, check.getStatus());

    /* get course fails */
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot", null))
        .when(courseUtil).getCourseIfUserInCourse(course.getId(), user);
    check = courseUtil.canDeleteUser(course.getId(), cuEnrolled.getUserId(), user);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, check.getStatus());
  }

  @Test
  public void testGetJoinLink() throws Exception {
    /* Link with key */
    String link = courseUtil.getJoinLink("key", "898");
    assertEquals("/api/courses/898/join/key", link);
    /* Link without key */
    link = courseUtil.getJoinLink(null, "334");
    assertEquals("/api/courses/334/join", link);
  }

  @Test
  public void testCheckJoinLink() throws Exception {
    /* All checks succeed */
    when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
    when(courseUserRepository.findById(argThat(
        arg -> arg.getCourseId() == (course.getId()) && arg.getUserId() == user.getId())
    )).thenReturn(Optional.empty());

    /* Course without key */
    course.setJoinKey(null);
    CheckResult<CourseEntity> check = courseUtil.checkJoinLink(course.getId(), null, user);
    assertEquals(HttpStatus.OK, check.getStatus());
    assertEquals(course, check.getData());

    /* Course with key */
    course.setJoinKey("key");
    check = courseUtil.checkJoinLink(course.getId(), "key", user);
    assertEquals(HttpStatus.OK, check.getStatus());
    assertEquals(course, check.getData());

    /* Check fails */
    /* User already in course */
    reset(courseUserRepository);
    when(courseUserRepository.findById(argThat(
        arg -> arg.getCourseId() == (course.getId()) && arg.getUserId() == user.getId())
    )).thenReturn(Optional.of(cuEnrolled));
    check = courseUtil.checkJoinLink(course.getId(), "key", user);
    assertEquals(HttpStatus.BAD_REQUEST, check.getStatus());

    /* Course with key but no key provided */
    check = courseUtil.checkJoinLink(course.getId(), null, user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* Course with key but wrong key provided */
    check = courseUtil.checkJoinLink(course.getId(), "wrong", user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* Course without key but key provided */
    course.setJoinKey(null);
    check = courseUtil.checkJoinLink(course.getId(), "key", user);
    assertEquals(HttpStatus.FORBIDDEN, check.getStatus());

    /* Course does not exist */
    when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());
    check = courseUtil.checkJoinLink(course.getId(), "key", user);
    assertEquals(HttpStatus.NOT_FOUND, check.getStatus());
  }

  @Test
  public void testCheckCourseJson() throws Exception {
    CourseJson courseJson = new CourseJson(
        "name", "description", null, 2024
    );
    /* Creating a course */
    user.setRole(UserRole.teacher);
    when(courseUserRepository.findById(argThat(
        arg -> arg.getCourseId() == (course.getId()) && arg.getUserId() == user.getId())
    )).thenReturn(Optional.of(cuCreator));
    CheckResult<Void> result = courseUtil.checkCourseJson(courseJson, user, null);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Updating a course */
    CheckResult<Void> result2 = courseUtil.checkCourseJson(courseJson, user, course.getId());

    /* Name is empty */
    courseJson.setName("");
    result = courseUtil.checkCourseJson(courseJson, user, null);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* name is null */
    courseJson.setName(null);
    result = courseUtil.checkCourseJson(courseJson, user, null);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* description is null */
    courseJson.setName("name");
    courseJson.setDescription(null);
    result = courseUtil.checkCourseJson(courseJson, user, null);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* year is null */
    courseJson.setDescription("description");
    courseJson.setYear(null);
    result = courseUtil.checkCourseJson(courseJson, user, null);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* creator can (un)archive course */
    courseJson.setYear(2024);
    courseJson.setArchived(true);
    result = courseUtil.checkCourseJson(courseJson, user, course.getId());
    assertEquals(HttpStatus.OK, result.getStatus());

    /* not-creator can't (un)archive course */
    reset(courseUserRepository);
    when(courseUserRepository.findById(argThat(
        arg -> arg.getCourseId() == (course.getId()) && arg.getUserId() == user.getId())
    )).thenReturn(Optional.of(cuAdmin));
    result = courseUtil.checkCourseJson(courseJson, user, course.getId());
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* User has to be in course to update */
    reset(courseUserRepository);
    when(courseUserRepository.findById(argThat(
        arg -> arg.getCourseId() == (course.getId()) && arg.getUserId() == user.getId())
    )).thenReturn(Optional.empty());
    result = courseUtil.checkCourseJson(courseJson, user, course.getId());
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
  }
}
