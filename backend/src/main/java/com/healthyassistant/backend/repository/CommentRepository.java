package com.healthyassistant.backend.repository;

import com.healthyassistant.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.share.id = :shareId ORDER BY c.createdAt DESC")
    List<Comment> findByShareIdOrderByCreatedAtDesc(@Param("shareId") Long shareId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.share.id = :shareId")
    Long countByShareId(@Param("shareId") Long shareId);
}