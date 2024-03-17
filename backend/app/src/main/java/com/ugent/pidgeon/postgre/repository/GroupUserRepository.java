package com.ugent.pidgeon.postgre.repository;

import com.ugent.pidgeon.postgre.models.GroupUserEntity;
import com.ugent.pidgeon.postgre.models.GroupUserId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupUserRepository extends JpaRepository<GroupUserEntity, GroupUserId> {
}
