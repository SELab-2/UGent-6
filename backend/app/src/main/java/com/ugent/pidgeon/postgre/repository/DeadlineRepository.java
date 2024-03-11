package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.DeadlineEntity;
import com.ugent.pidgeon.postgre.models.GroupClusterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadlineRepository  extends JpaRepository<DeadlineEntity, Long> {

}