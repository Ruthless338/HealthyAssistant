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

    public ShareService(ShareRepository shareRepository, UserService userService) {
        this.shareRepository = shareRepository;
        this.userService = userService;
    }

    @Transactional
    public Share createShare(Share share, Long userId) {
        User author = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        share.setAuthor(author);
        return shareRepository.save(share);
    }

    public List<ShareDTO> getAllShares() {
        List<Share> shares = shareRepository.findAllByOrderByCreatedAtDesc();
        return shares.stream().map(share -> {
            ShareDTO dto = new ShareDTO();
            dto.setId(share.getId());
            dto.setTitle(share.getTitle());
            dto.setContent(share.getContent());
            dto.setAuthorName(share.getAuthor().getUsername()); // 确保加载作者信息
            dto.setImages(share.getImages());
            dto.setLikes(share.getLikes());
            dto.setViews(share.getViews());
            dto.setCreatedAt(share.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Share> searchShares(String keyword) {
        return shareRepository.searchShares(keyword);
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
        }

        Share saved = shareRepository.save(share);
        return convertToDTO(saved, user.getId());
    }

    @Transactional
    public void incrementViews(Long id) {
        shareRepository.updateViewCount(id);
    }

    private ShareDTO convertToDTO(Share share, Long userId) {
        ShareDTO dto = new ShareDTO();
        dto.setIsLiked(share.getLikedUsers().stream()
                .anyMatch(u -> u.getId().equals(userId)));
        dto.setLikes(share.getLikes());
        return dto;
    }

}