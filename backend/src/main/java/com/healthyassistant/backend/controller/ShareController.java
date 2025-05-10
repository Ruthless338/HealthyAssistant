package com.healthyassistant.backend.controller;

import com.healthyassistant.backend.dto.ShareDTO;
import com.healthyassistant.backend.model.Share;
import com.healthyassistant.backend.service.ShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shares")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping("/create")
    public ResponseEntity<Share> createShare(
            @RequestBody Share share,
            @RequestParam Long userId) {
        Share created = shareService.createShare(share, userId);
        return ResponseEntity.ok(created);
    }

    // 调用方法: http://localhost:8000/api/shares/search?keyword=${keyword}
    @GetMapping("/search")
    public ResponseEntity<List<Share>> searchShares(
            @RequestParam String keyword) {
        return ResponseEntity.ok(shareService.searchShares(keyword));
    }

    // 调用方法: http://localhost:8000/api/shares/get?userId=${userId}
    @GetMapping("/get")
    public ResponseEntity<List<Share>> getCurrentUserShares(
            @RequestParam Long userId) {
        return ResponseEntity.ok(shareService.getSharesByUserId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ShareDTO>> getAllShares() {
        return ResponseEntity.ok(shareService.getAllShares());
    }

    // 这里的id是分享类的id
    @GetMapping("/{id}/view")
    public ResponseEntity<Void> viewShare(@PathVariable Long id) {
        shareService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    // 修改点赞接口
    @PostMapping("/like")
    public ResponseEntity<ShareDTO> likeShare(@RequestParam Long shareId, @RequestParam Long userId) {
        return ResponseEntity.ok(shareService.likeShare(shareId, userId));
    }
}