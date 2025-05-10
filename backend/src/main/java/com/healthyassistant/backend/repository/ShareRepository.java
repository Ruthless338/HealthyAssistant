package com.healthyassistant.backend.repository;

import com.healthyassistant.backend.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShareRepository extends JpaRepository<Share, Long> {

    // 根据关键词搜索（标题或内容）
    @Query("SELECT s FROM Share s WHERE " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY s.createdAt DESC")
    List<Share> searchShares(String keyword);

    // 获取用户的所有分享
    List<Share> findByAuthorIdOrderByCreatedAtDesc(Long userId);

    // 获取所有分享，按创建时间降序排列
    List<Share> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE Share s SET s.views = s.views + 1 WHERE s.id = :id")
    void updateViewCount(@Param("id") Long id);

}