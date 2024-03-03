package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
    TestEntity findById(long id);
}
