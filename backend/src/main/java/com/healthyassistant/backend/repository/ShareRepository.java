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
    
    // 查询当前用户的所有分享
    List<Share> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    // 查询所有分享（按时间降序）
    List<Share> findAllByOrderByCreateTimeDesc();
    
    // 更新点赞数
    @Modifying
    @Query("UPDATE Share s SET s.likeCount = s.likeCount + 1 WHERE s.id = :id")
    int increaseLikeCount(@Param("id") Long id);
    
    // 更新评论数
    @Modifying
    @Query("UPDATE Share s SET s.commentCount = s.commentCount + 1 WHERE s.id = :id")
    int increaseCommentCount(@Param("id") Long id);
}