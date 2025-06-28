package com.healthyassistant.backend.repository;

import com.healthyassistant.backend.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {

    // 根据关键词搜索（标题或内容）
    @Query("SELECT s FROM Share s WHERE s.title LIKE %:keyword% OR s.content LIKE %:keyword% ORDER BY s.createdAt DESC")
    List<Share> searchShares(@Param("keyword") String keyword);

    // 获取用户的所有分享
    @Query("SELECT s FROM Share s WHERE s.author.id = :userId ORDER BY s.createdAt DESC")
    List<Share> findByAuthorIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 获取所有分享，按创建时间降序排列
    @Query("SELECT s FROM Share s ORDER BY s.createdAt DESC")
    List<Share> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE Share s SET s.views = s.views + 1 WHERE s.id = :id")
    void updateViewCount(@Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.share.id = :shareId")
    Long getCommentCountByShareId(@Param("shareId") Long shareId);

}