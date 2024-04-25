package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
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

  @BeforeEach
  public void setUp() {
    submissionEntity = new SubmissionEntity();
    submissionEntity.setId(1L);
    projectEntity = new ProjectEntity();
    projectEntity.setId(1L);
    userEntity = new UserEntity();
    userEntity.setId(1L);
  }

  @Test
  public void testCanGetSubmission() {
    when(submissionRepository.findById(anyLong())).thenReturn(Optional.of(submissionEntity));
    when(groupUtil.canGetProjectGroupData(anyLong(), anyLong(), any(UserEntity.class)))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    assertEquals(submissionEntity, submissionUtil.canGetSubmission(1L, userEntity).getData());

    when(groupUtil.canGetProjectGroupData(anyLong(), anyLong(), any(UserEntity.class)))
        .thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "User does not have access to this submission", null));
    assertNull(submissionUtil.canGetSubmission(1L, userEntity).getData());
  }

  @Test
  public void testCanDeleteSubmission() {
    when(submissionRepository.findById(anyLong())).thenReturn(Optional.of(submissionEntity));
    when(projectUtil.isProjectAdmin(anyLong(), any(UserEntity.class)))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    assertEquals(submissionEntity, submissionUtil.canDeleteSubmission(1L, userEntity).getData());

    when(projectUtil.isProjectAdmin(anyLong(), any(UserEntity.class)))
        .thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "User does not have access to delete this submission", null));
    assertNull(submissionUtil.canDeleteSubmission(1L, userEntity).getData());
  }

  @Test
  public void testCheckOnSubmit() {
    ProjectEntity projectEntity = new ProjectEntity();
    projectEntity.setId(1L);
    projectEntity.setDeadline(OffsetDateTime.now().plusDays(1));
    CheckResult<ProjectEntity> projectCheck = new CheckResult<>(HttpStatus.OK, "", projectEntity);
    when(projectUtil.getProjectIfExists(anyLong())).thenReturn(projectCheck);
    when(groupRepository.groupIdByProjectAndUser(anyLong(), anyLong())).thenReturn(1L);
    when(projectUtil.userPartOfProject(anyLong(), anyLong())).thenReturn(true);
    when(projectUtil.getProjectIfExists(anyLong())).thenReturn(new CheckResult<>(HttpStatus.OK, "", projectEntity));
    when(groupUtil.getGroupIfExists(anyLong())).thenReturn(new CheckResult<>(HttpStatus.OK, "", new GroupEntity()));
    when(groupClusterRepository.inArchivedCourse(anyLong())).thenReturn(false);
    assertEquals(1L, submissionUtil.checkOnSubmit(1L, userEntity).getData());
  }
}