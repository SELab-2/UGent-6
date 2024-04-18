package com.ugent.pidgeon.postgre.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileEntityTest {

  private FileEntity fileEntity;

  @BeforeEach
  public void setUp() {
    fileEntity = new FileEntity();
  }

  @Test
  public void testName() {
    String name = "name";
    fileEntity.setName(name);
    assertEquals(name, fileEntity.getName());
  }

  @Test
  public void testPath() {
    String path = "path";
    fileEntity.setPath(path);
    assertEquals(path, fileEntity.getPath());
  }

  @Test
  public void testUploadedBy() {
    long uploadedBy = 1L;
    fileEntity.setUploadedBy(uploadedBy);
    assertEquals(uploadedBy, fileEntity.getUploadedBy());
  }

  @Test
  public void testConstructor() {
    String name = "name";
    String path = "path";
    long uploadedBy = 1L;
    FileEntity file = new FileEntity(name, path, uploadedBy);
    assertEquals(name, file.getName());
    assertEquals(path, file.getPath());
    assertEquals(uploadedBy, file.getUploadedBy());
  }
}
