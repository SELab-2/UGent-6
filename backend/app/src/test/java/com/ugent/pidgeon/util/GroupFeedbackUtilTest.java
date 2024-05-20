package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.model.json.UpdateGroupScoreRequest;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
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
public class GroupFeedbackUtilTest {
  @Mock
  private ProjectUtil projectUtil;
  @Mock
  private GroupUtil groupUtil;
  @Mock
  private GroupFeedbackRepository groupFeedbackRepository;

  @Spy
  @InjectMocks
  private GroupFeedbackUtil groupFeedbackUtil;

  private GroupFeedbackEntity groupFeedbackEntity;
  private UserEntity mockUser;
  private ProjectEntity projectEntity;
  private GroupEntity groupEntity;

  @BeforeEach
  public void setup() {
    groupFeedbackEntity = new GroupFeedbackEntity(
        5L,
        10L,
        10.0f,
        "Good job!"
    );
    mockUser = new UserEntity("name", "surname", "email", UserRole.student, "azureid", "");
    mockUser.setId(2L);
    projectEntity = new ProjectEntity(
        13L,
        "projectName",
        "projectDescription",
        21L,
        38L,
        true,
        34,
        OffsetDateTime.now()
    );
    projectEntity.setId(groupFeedbackEntity.getProjectId());
    groupEntity = new GroupEntity("test", projectEntity.getGroupClusterId());
    groupEntity.setId(groupFeedbackEntity.getGroupId());
  }

  @Test
  public void testGetGroupFeedbackIfExists() {
    /* GroupFeedback found */
    when(groupFeedbackRepository.findById(argThat(
        id -> id.getGroupId() == groupFeedbackEntity.getGroupId() && id.getProjectId() == groupFeedbackEntity.getProjectId()
    ))).thenReturn(java.util.Optional.of(groupFeedbackEntity));
    CheckResult<GroupFeedbackEntity> result = groupFeedbackUtil.getGroupFeedbackIfExists(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(groupFeedbackEntity, result.getData());

    /* GroupFeedback not found */
    reset(groupFeedbackRepository);
    when(groupFeedbackRepository.findById(argThat(
        id -> id.getGroupId() == groupFeedbackEntity.getGroupId() && id.getProjectId() == groupFeedbackEntity.getProjectId()
    ))).thenReturn(java.util.Optional.empty());
    result = groupFeedbackUtil.getGroupFeedbackIfExists(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
    assertNull(result.getData());
  }

  @Test
  public void testCheckGroupFeedback() {
    /* All schecks succeed */
    when(projectUtil.getProjectIfExists(groupFeedbackEntity.getProjectId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", projectEntity));
    when(groupUtil.getGroupIfExists(groupFeedbackEntity.getGroupId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", groupEntity));

    CheckResult<Void> result = groupFeedbackUtil.checkGroupFeedback(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Group doesn't belong to project */
    groupEntity.setClusterId(0);
    result = groupFeedbackUtil.checkGroupFeedback(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* Group get fails */
    when(groupUtil.getGroupIfExists(groupFeedbackEntity.getGroupId())).thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "Group not found", null));
    result = groupFeedbackUtil.checkGroupFeedback(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* Project get fails */
    when(projectUtil.getProjectIfExists(groupFeedbackEntity.getProjectId())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "Project not found", null));
    result = groupFeedbackUtil.checkGroupFeedback(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  public void testCheckGroupFeedbackUpdate() {
    /* All checks succeed: patch/put */
    doReturn(new CheckResult<>(HttpStatus.OK, "", null)).when(groupFeedbackUtil).checkGroupFeedback(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());
    when(groupUtil.isAdminOfGroup(groupFeedbackEntity.getGroupId(), mockUser)).thenReturn(new CheckResult<>(HttpStatus.OK, "", null));
    when(groupFeedbackRepository.findById(argThat(
        id -> id.getGroupId() == groupFeedbackEntity.getGroupId() && id.getProjectId() == groupFeedbackEntity.getProjectId()
    ))).thenReturn(Optional.of(groupFeedbackEntity));

    CheckResult<GroupFeedbackEntity> result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.PATCH);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(groupFeedbackEntity, result.getData());
    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.PUT);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertEquals(groupFeedbackEntity, result.getData());

    /* Group already exists: post */
    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.POST);
    assertEquals(HttpStatus.CONFLICT, result.getStatus());

    /* All checks succeed: post */
    reset(groupFeedbackRepository);
    when(groupFeedbackRepository.findById(argThat(
        id -> id.getGroupId() == groupFeedbackEntity.getGroupId() && id.getProjectId() == groupFeedbackEntity.getProjectId()
    ))).thenReturn(Optional.empty());

    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.POST);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertNull(result.getData());

    /* Group doesn't exist: patch/put */
    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.PATCH);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.PUT);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

    /* Admin check fails */
    when(groupUtil.isAdminOfGroup(groupFeedbackEntity.getGroupId(), mockUser)).thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "Not an admin", null));
    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.PATCH);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.PUT);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.POST);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* groupFeedbackCheckFails */
    doReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "Group feedback not found", null)).when(groupFeedbackUtil).checkGroupFeedback(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId());
    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.PATCH);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());
    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.PUT);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());
    result = groupFeedbackUtil.checkGroupFeedbackUpdate(groupFeedbackEntity.getGroupId(), groupFeedbackEntity.getProjectId(), mockUser, HttpMethod.POST);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());
  }

  @Test
  public void testCheckGroupFeedbackUpdateJson() {
    /* All checks succeed */
    UpdateGroupScoreRequest updateGroupScoreRequest = new UpdateGroupScoreRequest();
    updateGroupScoreRequest.setScore(Float.valueOf(projectEntity.getMaxScore()));
    updateGroupScoreRequest.setFeedback("Good job!");
    when(projectUtil.getProjectIfExists(groupFeedbackEntity.getProjectId())).thenReturn(new CheckResult<>(HttpStatus.OK, "", projectEntity));

    CheckResult<Void> result = groupFeedbackUtil.checkGroupFeedbackUpdateJson(updateGroupScoreRequest, groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Score is too high */
    updateGroupScoreRequest.setScore((float) (projectEntity.getMaxScore() + 1));
    result = groupFeedbackUtil.checkGroupFeedbackUpdateJson(updateGroupScoreRequest, groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Maxscore is null while score is too high */
    projectEntity.setMaxScore(null);
    result = groupFeedbackUtil.checkGroupFeedbackUpdateJson(updateGroupScoreRequest, groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.OK, result.getStatus());
    projectEntity.setMaxScore(34);

    /* Score is negative */
    updateGroupScoreRequest.setScore(-1.0f);
    result = groupFeedbackUtil.checkGroupFeedbackUpdateJson(updateGroupScoreRequest, groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Score is null */
    updateGroupScoreRequest.setScore(null);
    result = groupFeedbackUtil.checkGroupFeedbackUpdateJson(updateGroupScoreRequest, groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.OK, result.getStatus());

    /* Feedback is null */
    updateGroupScoreRequest.setScore(Float.valueOf(projectEntity.getMaxScore()));
    updateGroupScoreRequest.setFeedback(null);
    result = groupFeedbackUtil.checkGroupFeedbackUpdateJson(updateGroupScoreRequest, groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    /* Project get fails */
    when(projectUtil.getProjectIfExists(groupFeedbackEntity.getProjectId())).thenReturn(new CheckResult<>(HttpStatus.BAD_REQUEST, "Project not found", null));
    result = groupFeedbackUtil.checkGroupFeedbackUpdateJson(updateGroupScoreRequest, groupFeedbackEntity.getProjectId());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }


}
