package com.ugent.pidgeon.model;

import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileStructureTest {
    final String testDirectory = "src/test/test-cases/FileStructureTestCases/";
    @Test
    void allowAllFiles(){
        assertTrue(runTest("allowAll"));
    }
    @Test
    void hasFiles(){ // also has a comment for testing, comments start with '#'
        assertTrue(runTest("hasFiles"));
    }
    @Test
    void denyAllFiles(){
        assertFalse(runTest("denyAllFiles"));
    }
    @Test
    void doesntHaveFile(){
        assertFalse(runTest("doesntHaveFile"));
    }
    @Test
    void doesntHaveRequiredFile(){
        assertFalse(runTest("hasWrongFile"));
    }
    @Test
    void isEmpty(){
        assertTrue(runTest("isEmpty"));
    }
    private boolean runTest(String testpath){
        SubmissionTemplateModel model = new SubmissionTemplateModel();
        if(testpath.lastIndexOf('/') != testpath.length() - 1){
            testpath += "/";
        }
        try {
            // Read a string from a file
            String content = Files.readString(Path.of(testDirectory + testpath + "template.txt"));
            System.out.println("Template: " + content);
            model.parseSubmissionTemplate(content);
            // Expose all the paths in a zipfile
            return model.checkSubmission(testDirectory + testpath + "test.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
