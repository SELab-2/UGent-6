package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FileUtilTest {

  @Mock
  private FileRepository fileRepository;

  @InjectMocks
  private FileUtil fileUtil;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testSaveFileEntity() throws IOException {
    Path filePath = Paths.get("testPath");
    long projectId = 1L;
    long userId = 1L;
    FileEntity fileEntity = new FileEntity(filePath.getFileName().toString(), filePath.toString(), userId);
    when(fileRepository.save(any(FileEntity.class))).thenReturn(fileEntity);
    FileEntity result = fileUtil.saveFileEntity(filePath, projectId, userId);
    assertEquals(fileEntity, result);
  }

  @Test
  public void testDeleteFileById() {
    long fileId = 1L;
    FileEntity fileEntity = new FileEntity("testName", "testPath", 1L);
    when(fileRepository.findById(fileId)).thenReturn(java.util.Optional.of(fileEntity));
    CheckResult<Void> result = fileUtil.deleteFileById(fileId);
    assertEquals(HttpStatus.OK, result.getStatus());
  }
}