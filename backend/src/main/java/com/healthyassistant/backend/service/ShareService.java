package com.healthyassistant.backend.service;

import com.healthyassistant.backend.dto.ShareDTO;
import com.healthyassistant.backend.model.Share;
import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.repository.ShareRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShareService {

    private final ShareRepository shareRepository;
    private final UserService userService;
    private final CommentService commentService;
    private final RecommendationService recommendationService;

    public ShareService(ShareRepository shareRepository, UserService userService,
            CommentService commentService, RecommendationService recommendationService) {
        this.shareRepository = shareRepository;
        this.userService = userService;
        this.commentService = commentService;
        this.recommendationService = recommendationService;
    }

    @Transactional
    public Share createShare(Share share, Long userId) {
        User author = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        share.setAuthor(author);
        return shareRepository.save(share);
    }

    public List<ShareDTO> getAllShares() {
        List<Share> shares = shareRepository.findAllByOrderByCreatedAtDesc();
        // System.out.println(shares);
        return shares.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ShareDTO> searchShares(String keyword) {
        List<Share> shares = shareRepository.searchShares(keyword);
        return shares.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<Share> getSharesByUserId(Long userId) {
        return shareRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public ShareDTO likeShare(Long shareId, Long userId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (share.getLikedUsers().contains(user)) {
            share.getLikedUsers().remove(user);
            share.setLikes(share.getLikes() - 1);
        } else {
            share.getLikedUsers().add(user);
            share.setLikes(share.getLikes() + 1);
            // 更新用户画像
            recommendationService.updateUserProfile(userId, share, "like");
        }

        Share saved = shareRepository.save(share);
        return convertToDTO(saved, user.getId());
    }

    @Transactional
    public void incrementViews(Long id) {
        shareRepository.updateViewCount(id);
        // 这里可以添加用户画像更新逻辑
    }

    public ShareDTO getShareById(Long shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));
        return convertToDTO(share);
    }

    public ShareDTO getShareById(Long shareId, Long userId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));
        return convertToDTO(share, userId);
    }

    /**
     * 获取推荐分享
     */
    public List<ShareDTO> getRecommendedShares(Long userId, int limit) {
        return recommendationService.getRecommendedShares(userId, limit);
    }

    private ShareDTO convertToDTO(Share share) {
        ShareDTO dto = new ShareDTO();
        dto.setId(share.getId());
        dto.setTitle(share.getTitle());
        dto.setContent(share.getContent());
        dto.setAuthorName(share.getAuthor().getUsername());
        dto.setAuthorAvatar(share.getAuthor().getAvatar());
        dto.setImages(share.getImages());
        dto.setTags(share.getTags());
        dto.setSportType(share.getSportType());
        dto.setDifficultyLevel(share.getDifficultyLevel());
        dto.setTargetAudience(share.getTargetAudience());
        dto.setLikes(share.getLikes());
        dto.setViews(share.getViews());
        dto.setCreatedAt(share.getCreatedAt());
        dto.setComments(commentService.getCommentCountByShareId(share.getId()).intValue());
        dto.setIsLiked(false); // 默认设置为false，因为没有用户ID
        return dto;
    }

    private ShareDTO convertToDTO(Share share, Long userId) {
        ShareDTO dto = convertToDTO(share);
        dto.setIsLiked(share.getLikedUsers().stream().anyMatch(u -> u.getId().equals(userId)));
        return dto;
    }
}