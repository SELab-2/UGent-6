package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.submissionTesting.DockerOutput;
import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel;
import com.ugent.pidgeon.postgre.models.TestEntity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import org.springframework.stereotype.Component;

@Component
public class TestRunner {

  public SubmissionTemplateModel.SubmissionResult runStructureTest(
      ZipFile file, TestEntity testEntity, SubmissionTemplateModel model) throws IOException {
    // There is no structure test for this project
    if(testEntity == null || testEntity.getStructureTemplate() == null){
      return null;
    }
    String structureTemplateString = testEntity.getStructureTemplate();

    // Parse the file
    model.parseSubmissionTemplate(structureTemplateString);
    return model.checkSubmission(file);
  }

  public DockerOutput runDockerTest(ZipFile file, TestEntity testEntity, Path outputPath, DockerSubmissionTestModel model) throws IOException {
    // Get the test file from the server
    String testScript = testEntity.getDockerTestScript();
    String testTemplate = testEntity.getDockerTestTemplate();

    // The first script must always be null, otherwise there is nothing to run on the container
    if (testScript == null) {
      return null;
    }

    // Init container and add input files
    try {

      model.addZipInputFiles(file);
      DockerOutput output;

      if (testTemplate == null) {
        // This docker test is configured in the simple mode (store test console logs)
        output = model.runSubmission(testScript);
      } else {
        // This docker test is configured in the template mode (store json with feedback)
        output = model.runSubmissionWithTemplate(testScript, testTemplate);
      }
      // Get list of artifact files generated on submission
      List<File> artifacts = model.getArtifacts();

      // Copy all files as zip into the output directory
      if (artifacts != null && !artifacts.isEmpty()) {
        Filehandler.copyFilesAsZip(artifacts, outputPath);
      }

      // Cleanup garbage files and container
      model.cleanUp();

      return output;
    } catch (Exception e) {
      model.cleanUp();
      throw new IOException("Error while running docker tests: " + e.getMessage());
    }
  }

}
