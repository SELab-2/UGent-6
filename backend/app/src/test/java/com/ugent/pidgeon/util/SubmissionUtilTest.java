package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupClusterRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
import java.time.OffsetDateTime;
import org.hibernate.validator.constraints.ModCheck.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubmissionUtilTest {

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private ProjectUtil projectUtil;

  @Mock
  private SubmissionRepository submissionRepository;

  @Mock
  private GroupClusterRepository groupClusterRepository;

  @Mock
  private GroupUtil groupUtil;

  @InjectMocks
  private SubmissionUtil submissionUtil;

  private SubmissionEntity submissionEntity;
  private ProjectEntity projectEntity;
  private UserEntity userEntity;
  private GroupEntity groupEntity;

  @BeforeEach
  public void setUp() {
    submissionEntity = new SubmissionEntity(
        22,
        45L,
        99L,
        OffsetDateTime.MIN,
        true,
        true
    );
    submissionEntity.setId(78L);
    projectEntity = new ProjectEntity(
        99L,
        "projectName",
        "projectDescription",
        2L,
        100L,
        true,
        34,
        OffsetDateTime.now()
    );
    projectEntity.setId(64);
    userEntity = new UserEntity(
        "name",
        "surname",
        "email",
        UserRole.student,
        "azureId",
        ""
    );
    userEntity.setId(44L);

    groupEntity = new GroupEntity(
        "groupName",
        projectEntity.getGroupClusterId()
    );
    groupEntity.setId(4L);

  }

  @Test
  public void testCanGetSubmission() {
    /* All checks succeed */
    when(submissionRepository.findById(submissionEntity.getId())).thenReturn(Optional.of(submissionEntity));
    when(groupUtil.canGetProjectGroupData(submissionEntity.getGroupId(), submissionEntity.getProjectId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

    CheckResult<SubmissionEntity> result = submissionUtil.canGetSubmission(submissionEntity.getId(), userEntity);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(submissionEntity, result.getData());

    /* User does not have access to the submission */
    when(groupUtil.canGetProjectGroupData(submissionEntity.getGroupId(), submissionEntity.getProjectId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "User does not have access to get this submission", null));
    result = submissionUtil.canGetSubmission(submissionEntity.getId(), userEntity);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* Submission not found */
    when(submissionRepository.findById(submissionEntity.getId())).thenReturn(Optional.empty());
    result = submissionUtil.canGetSubmission(submissionEntity.getId(), userEntity);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }

  @Test
  public void testCanDeleteSubmission() {
    /* All checks succeed */
    when(submissionRepository.findById(submissionEntity.getId())).thenReturn(Optional.of(submissionEntity));
    when(projectUtil.isProjectAdmin(submissionEntity.getProjectId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

    CheckResult<SubmissionEntity> result = submissionUtil.canDeleteSubmission(submissionEntity.getId(), userEntity);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(submissionEntity, result.getData());

    /* User does not have access to delete the submission */
    when(projectUtil.isProjectAdmin(submissionEntity.getProjectId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "User does not have access to delete this submission", null));
    result = submissionUtil.canDeleteSubmission(submissionEntity.getId(), userEntity);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* Submission not found */
    when(submissionRepository.findById(submissionEntity.getId())).thenReturn(Optional.empty());
    result = submissionUtil.canDeleteSubmission(submissionEntity.getId(), userEntity);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }
  
  @Test
  public void testCheckOnSubmit() {
    /* All checks succeed */
    projectEntity.setDeadline(OffsetDateTime.now().plusDays(1));
    when(projectUtil.userPartOfProject(projectEntity.getId(), userEntity.getId())).thenReturn(true);
    when(groupRepository.groupIdByProjectAndUser(projectEntity.getId(), userEntity.getId())).thenReturn(groupEntity.getId());
    when(groupUtil.getGroupIfExists(groupEntity.getId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));
    when(groupClusterRepository.inArchivedCourse(groupEntity.getClusterId())).thenReturn(false);

    when(projectUtil.getProjectIfExists(projectEntity.getId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", projectEntity));
    CheckResult<Long> result = submissionUtil.checkOnSubmit(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* User not part of group but admin */
    when(groupRepository.groupIdByProjectAndUser(projectEntity.getId(), userEntity.getId())).thenReturn(null);
    when(projectUtil.isProjectAdmin(projectEntity.getId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    result = submissionUtil.checkOnSubmit(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertNull(result.getData());

    /* User not part of group and not admin */
    when(projectUtil.isProjectAdmin(projectEntity.getId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "User is not part of a group for this project", null));
    result = submissionUtil.checkOnSubmit(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    when(groupRepository.groupIdByProjectAndUser(projectEntity.getId(), userEntity.getId())).thenReturn(groupEntity.getId());

    /* Deadline passed */
    projectEntity.setDeadline(OffsetDateTime.now().minusDays(1));
    result = submissionUtil.checkOnSubmit(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* GroupCluster in archived course */
    when(groupClusterRepository.inArchivedCourse(groupEntity.getClusterId())).thenReturn(true);
    result = submissionUtil.checkOnSubmit(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Group not found */
    when(groupUtil.getGroupIfExists(groupEntity.getId())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "Group not found", null));
    result = submissionUtil.checkOnSubmit(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());


    /* User not part of project */
    when(projectUtil.userPartOfProject(projectEntity.getId(), userEntity.getId())).thenReturn(false);
    result = submissionUtil.checkOnSubmit(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Project not found */
    when(projectUtil.getProjectIfExists(projectEntity.getId())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "Project not found", null));
    result = submissionUtil.checkOnSubmit(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());
  }


}