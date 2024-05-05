package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
    @Query(value = "SELECT * FROM tests WHERE docker_image = ?1", nativeQuery = true)
    boolean imageIsUsed(String image);
    @Query(value ="SELECT t FROM ProjectEntity p JOIN TestEntity t ON p.testId = t.id WHERE p.id = ?1")
    Optional<TestEntity> findByProjectId(long projectId);
}
