package com.healthyassistant.backend.service;

import com.healthyassistant.backend.model.Share;
import com.healthyassistant.backend.repository.ShareRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ShareService {
    
    @Autowired
    private ShareRepository shareRepository;
    
    // 创建分享
    @Transactional
    public Share createShare(Share share) {
        share.setCreateTime(LocalDateTime.now());
        share.setUpdateTime(LocalDateTime.now());
        share.setLikeCount(0);
        share.setCommentCount(0);
        return shareRepository.save(share);
    }
    
    // 获取用户的所有分享
    public List<Share> getSharesByUserId(Long userId) {
        return shareRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
    
    // 获取所有分享
    public List<Share> getAllShares() {
        return shareRepository.findAllByOrderByCreateTimeDesc();
    }
    
    // 获取单个分享详情
    public Optional<Share> getShareById(Long id) {
        return shareRepository.findById(id);
    }
    
    // 更新分享
    @Transactional
    public Share updateShare(Long id, Share shareDetails) {
        Share share = shareRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Share not found with id: " + id));
        
        share.setTitle(shareDetails.getTitle());
        share.setContent(shareDetails.getContent());
        share.setImageUrl(shareDetails.getImageUrl());
        share.setUpdateTime(LocalDateTime.now());
        
        return shareRepository.save(share);
    }
    
    // 删除分享
    @Transactional
    public void deleteShare(Long id) {
        Share share = shareRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Share not found with id: " + id));
        
        shareRepository.delete(share);
    }
    
    // 点赞分享
    @Transactional
    public void likeShare(Long id) {
        shareRepository.increaseLikeCount(id);
    }
    
    // 评论分享
    @Transactional
    public void commentShare(Long id) {
        shareRepository.increaseCommentCount(id);
    }
}