package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id", nullable = false)
    private long id;

    @Column(name = "file_name", nullable = false)
    private String name;

    @Column(name = "file_path", nullable = false)
    private String path;

    @Column(name = "uploaded_by", nullable = false)
    private long uploadedBy;

    public FileEntity() {
    }

    public FileEntity(String name, String path, long uploadedBy) {
        this.name = name;
        this.path = path;
        this.uploadedBy = uploadedBy;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}
