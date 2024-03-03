package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    List<ProjectEntity> findByCourseId(long courseId);
}
