package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.model.json.ClusterFillJson;
import com.ugent.pidgeon.model.json.GroupClusterCreateJson;
import com.ugent.pidgeon.model.json.GroupClusterUpdateJson;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import java.util.Optional;
import org.hibernate.annotations.Check;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class ClusterUtilTest {

  @Mock
  private GroupClusterRepository groupClusterRepository;
  @Mock
  private CourseUserRepository courseUserRepository;
  @Mock
  private CourseUtil courseUtil;

  @Spy
  @InjectMocks
  private ClusterUtil clusterUtil;

  private GroupClusterEntity clusterEntity;

  private UserEntity mockUser;

  @BeforeEach
  public void setUp() {
    clusterEntity = new GroupClusterEntity(1L, 20, "clustername", 5);
    clusterEntity.setId(4L);
    mockUser = new UserEntity("name", "surname", "email", UserRole.student, "azureid");
  }

  @Test
  void testIsIndividualCluster() {
    when(groupClusterRepository.findById(clusterEntity.getId())).thenReturn(Optional.of(clusterEntity));
    // Test if the cluster is an individual cluster
    clusterEntity.setMaxSize(1);
    assertTrue(clusterUtil.isIndividualCluster(clusterEntity));
    assertTrue(clusterUtil.isIndividualCluster(clusterEntity.getId()));


    // Test if the cluster is not an individual cluster
    clusterEntity.setMaxSize(2);
    assertFalse(clusterUtil.isIndividualCluster(clusterEntity));
    assertFalse(clusterUtil.isIndividualCluster(clusterEntity.getId()));

    // Test if the cluster is null
    when(groupClusterRepository.findById(clusterEntity.getId())).thenReturn(Optional.empty());
    assertFalse(clusterUtil.isIndividualCluster(null));
    assertFalse(clusterUtil.isIndividualCluster(clusterEntity.getId()));
  }

  @Test
  void testCanDeleteCluster() {
    /* All checks succeed */
    doReturn(new CheckResult<>(HttpStatus.OK, "", clusterEntity))
        .when(clusterUtil)
        .getGroupClusterEntityIfAdminAndNotIndividual(clusterEntity.getId(), mockUser);

    when(groupClusterRepository.usedInProject(clusterEntity.getId())).thenReturn(false);

    CheckResult<Void> result = clusterUtil.canDeleteCluster(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* usedInProject returns true */
    when(groupClusterRepository.usedInProject(clusterEntity.getId())).thenReturn(true);
    result = clusterUtil.canDeleteCluster(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* getGroupClusterEntity fails */
    when(clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(clusterEntity.getId(), mockUser))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "Group cluster does not exist", null));

    result = clusterUtil.canDeleteCluster(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

  }

  @Test
  void testGetGroupClusterEntityIfNotIndividual() {
    /* All checks succeed */
    when(groupClusterRepository.findById(clusterEntity.getId())).thenReturn(Optional.of(clusterEntity));
    when(courseUtil.getCourseIfUserInCourse(clusterEntity.getCourseId(), mockUser))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

    doReturn(false).when(clusterUtil).isIndividualCluster(clusterEntity);
    CheckResult<GroupClusterEntity> result =
        clusterUtil.getGroupClusterEntityIfNotIndividual(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(clusterEntity, result.getData());

    /* Group cluster is individual cluster */
    doReturn(true).when(clusterUtil).isIndividualCluster(clusterEntity);
    result = clusterUtil.getGroupClusterEntityIfNotIndividual(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Course check fails, return corresponding status */
    when(courseUtil.getCourseIfUserInCourse(clusterEntity.getCourseId(), mockUser))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "Course does not exist", null));
    result = clusterUtil.getGroupClusterEntityIfNotIndividual(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* Group cluster does not exist */
    when(groupClusterRepository.findById(clusterEntity.getId())).thenReturn(Optional.empty());
    result = clusterUtil.getGroupClusterEntityIfNotIndividual(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }

  @Test
  void testGetGroupClusterEntityIfAdminAndNotIndividual() {
    /* All checks succeed */
    when(clusterUtil.getGroupClusterEntityIfNotIndividual(clusterEntity.getId(), mockUser))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", clusterEntity));

    when(courseUtil.getCourseIfAdmin(clusterEntity.getCourseId(), mockUser))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

    CheckResult<GroupClusterEntity> result =
        clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(clusterEntity, result.getData());

    /* Course check fails, return corresponding status */
    when(courseUtil.getCourseIfAdmin(clusterEntity.getCourseId(), mockUser))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    result = clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* getGroupClusterEntityIfNotIndividual fails */
    when(clusterUtil.getGroupClusterEntityIfNotIndividual(clusterEntity.getId(), mockUser))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "", null));
    result = clusterUtil.getGroupClusterEntityIfAdminAndNotIndividual(clusterEntity.getId(), mockUser);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());
  }

  @Test
  void testPartOfCourse() {
    /* All checks succeed */
    when(groupClusterRepository.findById(clusterEntity.getId())).thenReturn(Optional.of(clusterEntity));

    CheckResult<Void> result = clusterUtil.partOfCourse(clusterEntity.getId(), clusterEntity.getCourseId());
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Group cluster not linked to course */
    result = clusterUtil.partOfCourse(clusterEntity.getId(), clusterEntity.getCourseId() + 1);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Group cluster does not exist */
    when(groupClusterRepository.findById(clusterEntity.getId())).thenReturn(Optional.empty());
    result = clusterUtil.partOfCourse(clusterEntity.getId(), clusterEntity.getCourseId());
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }

  @Test
  void testGetClusterIfExists() {
    /* All checks succeed */
    when(groupClusterRepository.findById(clusterEntity.getId())).thenReturn(Optional.of(clusterEntity));

    CheckResult<GroupClusterEntity> result = clusterUtil.getClusterIfExists(clusterEntity.getId());
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(clusterEntity, result.getData());

    /* Group cluster does not exist */
    when(groupClusterRepository.findById(clusterEntity.getId())).thenReturn(Optional.empty());
    result = clusterUtil.getClusterIfExists(clusterEntity.getId());
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }

  @Test
  void testCheckGroupClusterUpdateJson() {
    GroupClusterUpdateJson json = new GroupClusterUpdateJson();
    /* All checks succeed */
    json.setCapacity(5);
    json.setName("clustername");
    CheckResult<Void> result = clusterUtil.checkGroupClusterUpdateJson(json);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Capacity is smaller than 1 */
    json.setCapacity(0);
    result = clusterUtil.checkGroupClusterUpdateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Name is empty */
    json.setName("");
    result = clusterUtil.checkGroupClusterUpdateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Capacity is null */
    json.setCapacity(null);
    result = clusterUtil.checkGroupClusterUpdateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Name is null */
    json.setCapacity(5);
    json.setName(null);
    result = clusterUtil.checkGroupClusterUpdateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  void testCheckGroupClusterCreateJson() {
    GroupClusterCreateJson json = new GroupClusterCreateJson("clustername", 5, 5);
    /* All checks succeed */
    CheckResult<Void> result = clusterUtil.checkGroupClusterCreateJson(json);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* GroupCount is negative */
    json = new GroupClusterCreateJson("clustername", 5, -5);
    result = clusterUtil.checkGroupClusterCreateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Capacity is smaller than 1 */
    json = new GroupClusterCreateJson("clustername", 0, 5);
    result = clusterUtil.checkGroupClusterCreateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Name is empty */
    json = new GroupClusterCreateJson("", 5, 5);
    result = clusterUtil.checkGroupClusterCreateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Capacity is null */
    json = new GroupClusterCreateJson("clustername", null, 5);
    result = clusterUtil.checkGroupClusterCreateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Name is null */
    json = new GroupClusterCreateJson(null, 5, 5);
    result = clusterUtil.checkGroupClusterCreateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* GroupCount is null */
    json = new GroupClusterCreateJson("clustername", 5, null);
    result = clusterUtil.checkGroupClusterCreateJson(json);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  public void testCheckFillClusterJson() {
    ClusterFillJson fillJson = new ClusterFillJson();
    CourseUserEntity enrolledCU = new CourseUserEntity(22L, 5L, CourseRelation.enrolled);
    fillJson.addClusterGroupMembers("Group1", new Long[]{5L, 2L});
    fillJson.addClusterGroupMembers("Group2", new Long[]{3L, 10L});

    when(courseUserRepository.findById(any())).thenReturn(Optional.of(enrolledCU));

    CheckResult<Void> result = clusterUtil.checkFillClusterJson(fillJson, clusterEntity);
    assertEquals(HttpStatus.OK, result.getStatus());

    verify(courseUserRepository, times(1)).findById(argThat(
        arg -> arg.getCourseId() == clusterEntity.getCourseId() && arg.getUserId() == 5L));
    verify(courseUserRepository, times(1)).findById(argThat(
        arg -> arg.getCourseId() == clusterEntity.getCourseId() && arg.getUserId() == 2L));
    verify(courseUserRepository, times(1)).findById(argThat(
        arg -> arg.getCourseId() == clusterEntity.getCourseId() && arg.getUserId() == 3L));
    verify(courseUserRepository, times(1)).findById(argThat(
        arg -> arg.getCourseId() == clusterEntity.getCourseId() && arg.getUserId() == 10L));

    /* User admin in course */
    CourseUserEntity courseAdminCU = new CourseUserEntity(22L, 5L, CourseRelation.course_admin);
    when(courseUserRepository.findById(argThat(
        arg -> arg.getCourseId() == clusterEntity.getCourseId() && arg.getUserId() == 5L)))
        .thenReturn(Optional.of(courseAdminCU));

    result = clusterUtil.checkFillClusterJson(fillJson, clusterEntity);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* User not found in course */
    reset(courseUserRepository);
    when(courseUserRepository.findById(any())).thenReturn(Optional.of(enrolledCU));
    when(courseUserRepository.findById(argThat(
        arg -> arg.getCourseId() == clusterEntity.getCourseId() && arg.getUserId() == 3L)))
        .thenReturn(Optional.empty());

    result = clusterUtil.checkFillClusterJson(fillJson, clusterEntity);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* trying to add user twice */
    reset(courseUserRepository);
    when(courseUserRepository.findById(any())).thenReturn(Optional.of(enrolledCU));
    fillJson.addClusterGroupMembers("Group3", new Long[]{5L, 4L});

    result = clusterUtil.checkFillClusterJson(fillJson, clusterEntity);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

  }


}
