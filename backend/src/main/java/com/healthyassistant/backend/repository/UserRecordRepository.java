package com.healthyassistant.backend.repository;

import com.healthyassistant.backend.model.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {
    List<UserRecord> findByUsername(String username);
}
