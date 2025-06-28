package com.healthyassistant.backend.controller;

import com.healthyassistant.backend.dto.CommentDTO;
import com.healthyassistant.backend.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/create")
    public ResponseEntity<CommentDTO> createComment(
            @RequestParam String content,
            @RequestParam Long shareId,
            @RequestParam Long userId) {
        CommentDTO created = commentService.createComment(content, shareId, userId);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/share/{shareId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByShareId(@PathVariable Long shareId) {
        List<CommentDTO> comments = commentService.getCommentsByShareId(shareId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok().build();
    }
}