package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import java.util.Optional;
import java.util.logging.FileHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileUtilTest {

  @Mock
  private FileRepository fileRepository;

  @InjectMocks
  private FileUtil fileUtil;

  private FileEntity fileEntity;

  @BeforeEach
  public void setUp() {
    fileEntity = new FileEntity("testName", "testPath", 5L);
    fileEntity.setId(2L);
  }


  @Test
  public void testDeleteFileById() {
    when(fileRepository.findById(fileEntity.getId())).thenReturn(Optional.of(fileEntity));
    try (MockedStatic<Filehandler> mockedFileHandler = Mockito.mockStatic(Filehandler.class)) {
      mockedFileHandler.when(() -> Filehandler.deleteLocation(argThat(
          path -> path.toString().equals(fileEntity.getPath()))
      )).thenAnswer(invocation -> {
        // Do nothing
        return null;
      });
      CheckResult<Void> result = fileUtil.deleteFileById(fileEntity.getId());
      assertEquals(HttpStatus.OK, result.getStatus());
      verify(fileRepository, times(1)).delete(fileEntity);

      // Error when file is being deleted
      mockedFileHandler.when(() -> Filehandler.deleteLocation(argThat(
          path -> path.toString().equals(fileEntity.getPath()))
      )).thenThrow(new IOException());
      result = fileUtil.deleteFileById(fileEntity.getId());
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());

      // File not found
      when(fileRepository.findById(fileEntity.getId())).thenReturn(Optional.empty());
      result = fileUtil.deleteFileById(fileEntity.getId());
      assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}