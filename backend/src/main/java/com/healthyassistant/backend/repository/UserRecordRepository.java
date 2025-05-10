package com.healthyassistant.backend.repository;

import com.healthyassistant.backend.model.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {
    // 通过关联的User实体查询
    @Query("SELECT r FROM UserRecord r WHERE r.user.username = :username")
    List<UserRecord> findByUsername(@Param("username") String username);
}
