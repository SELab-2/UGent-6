package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
