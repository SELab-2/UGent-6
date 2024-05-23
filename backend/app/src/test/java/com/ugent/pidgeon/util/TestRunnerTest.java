package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.model.submissionTesting.DockerOutput;
import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.model.submissionTesting.DockerTemplateTestOutput;
import com.ugent.pidgeon.model.submissionTesting.DockerTestOutput;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel.SubmissionResult;
import com.ugent.pidgeon.postgre.models.TestEntity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestRunnerTest {

  @Mock
  private SubmissionTemplateModel structureModel;
  @Mock
  private DockerSubmissionTestModel dockerModel;
  @Mock
  private ZipFile file;
  @Mock
  private File artifactFile;




  private List<File> artifacts;

  private TestEntity testEntity;
  private SubmissionResult submissionResult;
  private DockerTestOutput dockerTestOutput;
  private DockerTemplateTestOutput dockerTemplateTestOutput;
  private final long projectId = 876L;

  @BeforeEach
  public void setUp() {
    testEntity = new TestEntity(
        "dockerImageBasic",
        "dockerTestScriptBasic",
        "dockerTestTemplateBasic",
        "structureTemplateBasic"
    );
    testEntity.setId(38L);

    submissionResult = new SubmissionResult(
        true, "submissionResultBasic"
    );

    dockerTestOutput = new DockerTestOutput(
        List.of("logs"), true
    );

    dockerTemplateTestOutput = new DockerTemplateTestOutput(
        Collections.emptyList(), true
    );

    artifacts = List.of(artifactFile);
  }

  @Test
  public void testRunStructureTest() throws IOException {
    /* The test exists */
    when(structureModel.checkSubmission(file)).thenReturn(submissionResult);
    SubmissionResult result = new TestRunner().runStructureTest(file, testEntity, structureModel);
    assertEquals(submissionResult, result);
    verify(structureModel).parseSubmissionTemplate(testEntity.getStructureTemplate());

    /* Structure template is null */
    testEntity.setStructureTemplate(null);
    result = new TestRunner().runStructureTest(file, testEntity, structureModel);
    assertNull(result);

    /* Test entity is null */
    result = new TestRunner().runStructureTest(file, null, structureModel);
    assertNull(result);
  }

  @Test
  public void testRunDockerTest() throws IOException {
    Path outputPath = Path.of("outputPath");
    Path extraFilesPath = Path.of("extraFilesPath");
    Path extraFilesPathResolved = extraFilesPath.resolve(Filehandler.EXTRA_TESTFILES_FILENAME);
    
    try (MockedStatic<Filehandler> filehandler = org.mockito.Mockito.mockStatic(Filehandler.class)) {
      
      AtomicInteger filehandlerCalled = new AtomicInteger();
      filehandlerCalled.set(0);
      filehandler.when(() -> Filehandler.copyFilesAsZip(artifacts, outputPath)).thenAnswer(
          invocation -> {
            filehandlerCalled.getAndIncrement();
            return null;
          });
      filehandler.when(() -> Filehandler.getTestExtraFilesPath(projectId)).thenReturn(extraFilesPath);
      when(dockerModel.runSubmissionWithTemplate(testEntity.getDockerTestScript(), testEntity.getDockerTestTemplate()))
          .thenReturn(dockerTemplateTestOutput);
      when(dockerModel.getArtifacts()).thenReturn(artifacts);

      DockerOutput result = new TestRunner().runDockerTest(file, testEntity, outputPath, dockerModel, projectId);
      assertEquals(dockerTemplateTestOutput, result);

      verify(dockerModel, times(1)).addZipInputFiles(file);
      verify(dockerModel, times(1)).cleanUp();
      assertEquals(1, filehandlerCalled.get());

      /* artifacts are empty */
      when(dockerModel.getArtifacts()).thenReturn(Collections.emptyList());
      result = new TestRunner().runDockerTest(file, testEntity, outputPath, dockerModel, projectId);
      assertEquals(dockerTemplateTestOutput, result);
      verify(dockerModel, times(2)).addZipInputFiles(file);
      verify(dockerModel, times(2)).cleanUp();
      assertEquals(1, filehandlerCalled.get());

      /* aritifacts are null */
      when(dockerModel.getArtifacts()).thenReturn(null);
      result = new TestRunner().runDockerTest(file, testEntity, outputPath, dockerModel, projectId);
      assertEquals(dockerTemplateTestOutput, result);
      verify(dockerModel, times(3)).addZipInputFiles(file);
      verify(dockerModel, times(3)).cleanUp();
      assertEquals(1, filehandlerCalled.get());

      /* No template */
      testEntity.setDockerTestTemplate(null);
      when(dockerModel.runSubmission(testEntity.getDockerTestScript())).thenReturn(dockerTestOutput);
      result = new TestRunner().runDockerTest(file, testEntity, outputPath, dockerModel, projectId);
      assertEquals(dockerTestOutput, result);
      verify(dockerModel, times(4)).addZipInputFiles(file);
      verify(dockerModel, times(4)).cleanUp();

      /* Error gets thrown */
      when(dockerModel.runSubmission(testEntity.getDockerTestScript())).thenThrow(new RuntimeException("Error"));
      assertThrows(Exception.class, () -> new TestRunner().runDockerTest(file, testEntity, outputPath, dockerModel, projectId));
      verify(dockerModel, times(5)).cleanUp();

      /* No script */
      testEntity.setDockerTestScript(null);
      result = new TestRunner().runDockerTest(file, testEntity, outputPath, dockerModel, projectId);
      assertNull(result);
    }


  }
}
