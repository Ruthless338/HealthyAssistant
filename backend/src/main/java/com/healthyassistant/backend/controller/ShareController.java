package com.healthyassistant.backend.controller;

import com.healthyassistant.backend.model.Share;
import com.healthyassistant.backend.service.ShareService;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shares")
public class ShareController {
    
    @Autowired
    private ShareService shareService;
    
    // 创建分享
    @PostMapping
    public ResponseEntity<Share> createShare(@RequestBody Share share) {
        Share createdShare = shareService.createShare(share);
        return ResponseEntity.ok(createdShare);
    }
    
    // 获取所有分享
    @GetMapping
    public ResponseEntity<List<Share>> getAllShares() {
        List<Share> shares = shareService.getAllShares();
        return ResponseEntity.ok(shares);
    }
    
    // 获取用户的所有分享
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Share>> getSharesByUserId(@PathVariable Long userId) {
        List<Share> shares = shareService.getSharesByUserId(userId);
        return ResponseEntity.ok(shares);
    }
    
    // 获取单个分享详情
    @GetMapping("/{id}")
    public ResponseEntity<Share> getShareById(@PathVariable Long id) {
        Optional<Share> share = shareService.getShareById(id);
        return share.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // 更新分享
    @PutMapping("/{id}")
    public ResponseEntity<Share> updateShare(@PathVariable Long id, @RequestBody Share shareDetails) {
        Share updatedShare = shareService.updateShare(id, shareDetails);
        return ResponseEntity.ok(updatedShare);
    }
    
    // 删除分享
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShare(@PathVariable Long id) {
        shareService.deleteShare(id);
        return ResponseEntity.noContent().build();
    }
    
    // 点赞分享
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeShare(@PathVariable Long id) {
        shareService.likeShare(id);
        return ResponseEntity.ok().build();
    }
    
    // 评论分享（这里只是增加评论计数，实际评论功能需要单独的评论表）
    @PostMapping("/{id}/comment")
    public ResponseEntity<Void> commentShare(@PathVariable Long id) {
        shareService.commentShare(id);
        return ResponseEntity.ok().build();
    }
}