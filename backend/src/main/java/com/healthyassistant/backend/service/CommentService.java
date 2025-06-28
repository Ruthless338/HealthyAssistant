package com.healthyassistant.backend.service;

import com.healthyassistant.backend.dto.CommentDTO;
import com.healthyassistant.backend.model.Comment;
import com.healthyassistant.backend.model.Share;
import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.repository.CommentRepository;
import com.healthyassistant.backend.repository.ShareRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, ShareRepository shareRepository,
            UserService userService) {
        this.commentRepository = commentRepository;
        this.shareRepository = shareRepository;
        this.userService = userService;
    }

    @Transactional
    public CommentDTO createComment(String content, Long shareId, Long userId) {
        User author = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setShare(share);

        Comment saved = commentRepository.save(comment);
        return convertToDTO(saved);
    }

    public List<CommentDTO> getCommentsByShareId(Long shareId) {
        List<Comment> comments = commentRepository.findByShareIdOrderByCreatedAtDesc(shareId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Long getCommentCountByShareId(Long shareId) {
        return commentRepository.countByShareId(shareId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // 只有评论作者才能删除评论
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthorName(comment.getAuthor().getUsername());
        dto.setAuthorAvatar(comment.getAuthor().getAvatar());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setShareId(comment.getShare().getId());
        return dto;
    }
}