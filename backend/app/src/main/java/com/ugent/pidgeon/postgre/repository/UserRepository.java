package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findById(long id);


}
