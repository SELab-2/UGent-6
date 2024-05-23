package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.TestEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
    @Query(value = """
      SELECT CASE WHEN EXISTS (SELECT t FROM TestEntity  t WHERE t.dockerImage = ?1)
      THEN true
      ELSE false
      END
      """)
    boolean imageIsUsed(String image);


    @Query(value ="SELECT t FROM ProjectEntity p JOIN TestEntity t ON p.testId = t.id WHERE p.id = ?1")
    Optional<TestEntity> findByProjectId(long projectId);
}
