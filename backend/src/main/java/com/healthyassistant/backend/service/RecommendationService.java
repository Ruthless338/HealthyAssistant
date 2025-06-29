package com.healthyassistant.backend.service;

import com.healthyassistant.backend.dto.ShareDTO;
import com.healthyassistant.backend.model.Share;
import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.model.UserProfile;
import com.healthyassistant.backend.repository.ShareRepository;
import com.healthyassistant.backend.repository.UserProfileRepository;
import com.healthyassistant.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ShareRepository shareRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CommentService commentService;

    // 权重配置
    private static final double SPORT_TYPE_WEIGHT = 0.3;
    private static final double DIFFICULTY_WEIGHT = 0.2;
    private static final double TAGS_WEIGHT = 0.25;
    private static final double TARGET_AUDIENCE_WEIGHT = 0.15;
    private static final double POPULARITY_WEIGHT = 0.1;

    // 协同过滤参数
    private static final int SIMILAR_USER_COUNT = 5;
    private static final double COLLABORATIVE_WEIGHT = 0.3;

    public RecommendationService(ShareRepository shareRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            CommentService commentService) {
        this.shareRepository = shareRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.commentService = commentService;
    }

    /**
     * 获取用户推荐分享
     */
    public List<ShareDTO> getRecommendedShares(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 获取所有分享
        List<Share> allShares = shareRepository.findAllByOrderByCreatedAtDesc();

        // 计算推荐分数
        List<ShareDTO> recommendedShares = allShares.stream()
                .map(share -> {
                    ShareDTO dto = convertToDTO(share, userId);
                    dto.setRecommendationScore(calculateRecommendationScore(user, share));
                    return dto;
                })
                .sorted((a, b) -> Double.compare(b.getRecommendationScore(), a.getRecommendationScore()))
                .limit(limit)
                .collect(Collectors.toList());

        return recommendedShares;
    }

    /**
     * 计算推荐分数（基于内容的推荐 + 协同过滤）
     */
    private double calculateRecommendationScore(User user, Share share) {
        double contentScore = calculateContentBasedScore(user, share);
        double collaborativeScore = calculateCollaborativeScore(user, share);

        // 加权平均
        return contentScore * (1 - COLLABORATIVE_WEIGHT) + collaborativeScore * COLLABORATIVE_WEIGHT;
    }

    /**
     * 基于内容的推荐分数计算
     */
    private double calculateContentBasedScore(User user, Share share) {
        double score = 0.0;

        // 1. 运动类型匹配
        if (share.getSportType() != null && user.getInterest() != null) {
            if (user.getInterest().contains(share.getSportType())) {
                score += SPORT_TYPE_WEIGHT;
            }
        }

        // 2. 难度等级匹配（基于用户年龄和健身目标）
        if (share.getDifficultyLevel() != null) {
            score += calculateDifficultyMatch(user, share.getDifficultyLevel()) * DIFFICULTY_WEIGHT;
        }

        // 3. 标签匹配
        if (share.getTags() != null && user.getInterest() != null) {
            score += calculateTagMatch(user.getInterest(), share.getTags()) * TAGS_WEIGHT;
        }

        // 4. 目标人群匹配
        if (share.getTargetAudience() != null && user.getGoal() != null) {
            if (user.getGoal().contains(share.getTargetAudience())) {
                score += TARGET_AUDIENCE_WEIGHT;
            }
        }

        // 5. 热度分数
        score += calculatePopularityScore(share) * POPULARITY_WEIGHT;

        return Math.min(score, 1.0); // 确保分数不超过1.0
    }

    /**
     * 计算难度匹配度
     */
    private double calculateDifficultyMatch(User user, String difficultyLevel) {
        int age = user.getAge();
        List<String> goals = user.getGoal();

        switch (difficultyLevel.toLowerCase()) {
            case "初级":
                return 1.0; // 所有人都适合初级
            case "中级":
                if (age >= 18 && age <= 50)
                    return 0.8;
                return 0.6;
            case "高级":
                if (age >= 20 && age <= 40)
                    return 0.7;
                return 0.4;
            default:
                return 0.5;
        }
    }

    /**
     * 计算标签匹配度
     */
    private double calculateTagMatch(List<String> userInterests, List<String> shareTags) {
        if (userInterests == null || shareTags == null || userInterests.isEmpty() || shareTags.isEmpty()) {
            return 0.0;
        }

        Set<String> userSet = new HashSet<>(userInterests);
        Set<String> tagSet = new HashSet<>(shareTags);

        // 计算交集大小
        Set<String> intersection = new HashSet<>(userSet);
        intersection.retainAll(tagSet);

        // Jaccard相似度
        Set<String> union = new HashSet<>(userSet);
        union.addAll(tagSet);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 计算热度分数
     */
    private double calculatePopularityScore(Share share) {
        double likeScore = Math.min(share.getLikes() / 100.0, 1.0); // 点赞数归一化
        double viewScore = Math.min(share.getViews() / 1000.0, 1.0); // 浏览数归一化
        double commentScore = Math.min(commentService.getCommentCountByShareId(share.getId()) / 50.0, 1.0); // 评论数归一化

        return (likeScore * 0.4 + viewScore * 0.4 + commentScore * 0.2);
    }

    /**
     * 协同过滤推荐分数计算
     */
    private double calculateCollaborativeScore(User user, Share share) {
        // 找到相似用户
        List<User> similarUsers = findSimilarUsers(user);

        if (similarUsers.isEmpty()) {
            return 0.0;
        }

        // 计算相似用户对该分享的偏好
        double totalScore = 0.0;
        double totalWeight = 0.0;

        for (User similarUser : similarUsers) {
            double similarity = calculateUserSimilarity(user, similarUser);
            double userPreference = getUserPreferenceForShare(similarUser, share);

            totalScore += similarity * userPreference;
            totalWeight += similarity;
        }

        return totalWeight > 0 ? totalScore / totalWeight : 0.0;
    }

    /**
     * 找到相似用户
     */
    private List<User> findSimilarUsers(User user) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .map(u -> new AbstractMap.SimpleEntry<>(u, calculateUserSimilarity(user, u)))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(SIMILAR_USER_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 计算用户相似度
     */
    private double calculateUserSimilarity(User user1, User user2) {
        double similarity = 0.0;
        double totalWeight = 0.0;

        // 兴趣相似度
        if (user1.getInterest() != null && user2.getInterest() != null) {
            double interestSimilarity = calculateListSimilarity(user1.getInterest(), user2.getInterest());
            similarity += interestSimilarity * 0.4;
            totalWeight += 0.4;
        }

        // 目标相似度
        if (user1.getGoal() != null && user2.getGoal() != null) {
            double goalSimilarity = calculateListSimilarity(user1.getGoal(), user2.getGoal());
            similarity += goalSimilarity * 0.3;
            totalWeight += 0.3;
        }

        // 年龄相似度
        double ageSimilarity = 1.0 - Math.abs(user1.getAge() - user2.getAge()) / 50.0;
        similarity += ageSimilarity * 0.2;
        totalWeight += 0.2;

        // 性别相似度
        if (user1.getGender() != null && user2.getGender() != null &&
                user1.getGender().equals(user2.getGender())) {
            similarity += 0.1;
            totalWeight += 0.1;
        }

        return totalWeight > 0 ? similarity / totalWeight : 0.0;
    }

    /**
     * 计算列表相似度
     */
    private double calculateListSimilarity(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
            return 0.0;
        }

        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 获取用户对特定分享的偏好
     */
    private double getUserPreferenceForShare(User user, Share share) {
        // 检查用户是否点赞过该分享
        if (share.getLikedUsers().contains(user)) {
            return 1.0;
        }

        // 检查用户是否浏览过该分享（这里简化处理）
        // 实际应用中可以从用户行为记录中获取
        return 0.0;
    }

    /**
     * 更新用户画像
     */
    public void updateUserProfile(Long userId, Share share, String action) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(userRepository.findById(userId).orElse(null));
                    return newProfile;
                });

        // 更新行为统计
        switch (action) {
            case "like":
                profile.setTotalLikes(profile.getTotalLikes() + 1);
                updatePreferences(profile, share, 0.1); // 点赞权重
                break;
            case "view":
                profile.setTotalViews(profile.getTotalViews() + 1);
                updatePreferences(profile, share, 0.05); // 浏览权重
                break;
            case "share":
                profile.setTotalShares(profile.getTotalShares() + 1);
                updatePreferences(profile, share, 0.15); // 分享权重
                break;
            case "comment":
                profile.setTotalComments(profile.getTotalComments() + 1);
                updatePreferences(profile, share, 0.12); // 评论权重
                break;
        }

        // 更新活跃度分数
        updateActivityScore(profile);

        userProfileRepository.save(profile);
    }

    /**
     * 更新用户偏好
     */
    private void updatePreferences(UserProfile profile, Share share, double weight) {
        // 更新运动类型偏好
        if (share.getSportType() != null) {
            profile.getSportPreferences().merge(share.getSportType(), weight, Double::sum);
        }

        // 更新难度偏好
        if (share.getDifficultyLevel() != null) {
            profile.getDifficultyPreferences().merge(share.getDifficultyLevel(), weight, Double::sum);
        }

        // 更新标签偏好
        if (share.getTags() != null) {
            for (String tag : share.getTags()) {
                profile.getTagPreferences().merge(tag, weight, Double::sum);
            }
        }
    }

    /**
     * 更新活跃度分数
     */
    private void updateActivityScore(UserProfile profile) {
        double score = profile.getTotalLikes() * 0.3 +
                profile.getTotalViews() * 0.2 +
                profile.getTotalShares() * 0.3 +
                profile.getTotalComments() * 0.2;

        profile.setActivityScore(score);
    }

    /**
     * 转换Share为ShareDTO
     */
    private ShareDTO convertToDTO(Share share, Long userId) {
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
        dto.setComments(commentService.getCommentCountByShareId(share.getId()).intValue());
        dto.setCreatedAt(share.getCreatedAt());
        dto.setIsLiked(share.getLikedUsers().stream().anyMatch(u -> u.getId().equals(userId)));
        return dto;
    }
}