<template>
  <div class="recommendations-container">
    <!-- 页面标题 -->
    <div class="page-header glass">
      <h1 class="page-title gradient-text">为你推荐</h1>
      <p class="page-subtitle">基于你的兴趣和偏好，为你精选的运动分享</p>
    </div>

    <!-- 推荐设置 -->
    <div class="settings-section glass">
      <div class="settings-header">
        <h3 class="settings-title">推荐设置</h3>
        <button @click="refreshRecommendations" class="refresh-btn">
          <span class="refresh-icon">🔄</span>
          刷新推荐
        </button>
      </div>
      <div class="settings-content">
        <div class="setting-item">
          <label>推荐数量</label>
          <select v-model="recommendationLimit" @change="refreshRecommendations" class="limit-select">
            <option value="5">5条</option>
            <option value="10">10条</option>
            <option value="15">15条</option>
            <option value="20">20条</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 推荐算法可视化 -->
    <RecommendationVisualizer 
      :userTraits="userTraits"
      :contentScores="averageContentScores"
      :similarUserCount="5"
      :collaborativeScore="75"
      :finalScore="Math.round(averageRecommendationScore * 100)"
    />

    <!-- 推荐列表 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-card glass">
        <div class="loading-spinner"></div>
        <p class="loading-text">正在为你生成个性化推荐...</p>
      </div>
    </div>

    <div v-else-if="recommendations.length" class="recommendations-list">
      <div 
        v-for="(share, index) in recommendations"
        :key="share.id"
        class="recommendation-card glass"
      >
        <!-- 推荐分数标签 -->
        <div class="recommendation-score">
          <span class="score-label">推荐度</span>
          <span class="score-value">{{ Math.round(share.recommendationScore * 100) }}%</span>
          <button @click="showScoreDetails(share)" class="score-details-btn">
            <span class="details-icon">ℹ️</span>
          </button>
        </div>

        <!-- 推荐分数详情弹窗 -->
        <div v-if="share.showScoreDetails" class="score-details-modal" @click.self="hideScoreDetails(share)">
          <div class="score-details-content glass">
            <div class="details-header">
              <h3 class="details-title">推荐分数详情</h3>
              <button @click="hideScoreDetails(share)" class="close-btn">
                <span class="close-icon">✕</span>
              </button>
            </div>
            
            <div class="score-breakdown">
              <div class="total-score">
                <span class="score-label">总推荐分数</span>
                <span class="score-value">{{ Math.round(share.recommendationScore * 100) }}%</span>
              </div>
              
              <div class="score-components">
                <h4 class="components-title">分数构成</h4>
                
                <div class="component-item">
                  <div class="component-header">
                    <span class="component-name">内容匹配度</span>
                    <span class="component-weight">70%</span>
                  </div>
                  <div class="component-details">
                    <div class="detail-item">
                      <span class="detail-label">运动类型匹配</span>
                      <span class="detail-value">{{ getSportTypeMatch(share) ? '✓' : '✗' }}</span>
                      <span class="detail-score">+{{ getSportTypeMatch(share) ? '30' : '0' }}%</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">难度等级匹配</span>
                      <span class="detail-value">{{ getDifficultyMatch(share) }}</span>
                      <span class="detail-score">+{{ Math.round(getDifficultyScore(share) * 20) }}%</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">标签匹配度</span>
                      <span class="detail-value">{{ Math.round(getTagMatchScore(share) * 100) }}%</span>
                      <span class="detail-score">+{{ Math.round(getTagMatchScore(share) * 25) }}%</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">目标人群匹配</span>
                      <span class="detail-value">{{ getTargetMatch(share) ? '✓' : '✗' }}</span>
                      <span class="detail-score">+{{ getTargetMatch(share) ? '15' : '0' }}%</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">热度分数</span>
                      <span class="detail-value">{{ Math.round(getPopularityScore(share) * 100) }}%</span>
                      <span class="detail-score">+{{ Math.round(getPopularityScore(share) * 10) }}%</span>
                    </div>
                  </div>
                </div>
                
                <div class="component-item">
                  <div class="component-header">
                    <span class="component-name">协同过滤</span>
                    <span class="component-weight">30%</span>
                  </div>
                  <div class="component-details">
                    <div class="detail-item">
                      <span class="detail-label">相似用户偏好</span>
                      <span class="detail-value">{{ Math.round(getCollaborativeScore(share) * 100) }}%</span>
                      <span class="detail-score">+{{ Math.round(getCollaborativeScore(share) * 30) }}%</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="score-explanation">
                <h4 class="explanation-title">计算说明</h4>
                <div class="explanation-content">
                  <p><strong>内容匹配度 (70%)</strong>：基于您的兴趣、目标、年龄等个人特征计算</p>
                  <p><strong>协同过滤 (30%)</strong>：基于与您相似的用户偏好计算</p>
                  <p><strong>最终分数</strong>：内容匹配度 × 0.7 + 协同过滤 × 0.3</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="share-header">
          <div class="user-info">
            <div class="avatar-container">
              <img 
                :src="'http://localhost:8000/uploads/'+share.authorAvatar"
                class="user-avatar"
                @error="handleAvatarError"
              >
              <div class="avatar-status"></div>
            </div>
            <div class="user-details">
              <h3 class="author-name">{{ share.authorName }}</h3>
              <time class="share-time">{{ formatTime(share.createdAt) }}</time>
            </div>
          </div>
          <div class="share-actions">
            <button class="action-btn share-btn" @click="sharePost(share)">
              <span class="action-icon">📤</span>
            </button>
          </div>
        </div>

        <div class="share-content" @click="openShareDetail(share.id)">
          <h4 class="share-title">{{ share.title }}</h4>
          <p class="content-preview">{{ truncateContent(share.content) }}</p>
          
          <!-- 分享标签 -->
          <div v-if="share.tags && share.tags.length" class="share-tags">
            <span 
              v-for="tag in share.tags.slice(0, 5)" 
              :key="tag"
              class="tag-item"
            >
              {{ tag }}
            </span>
            <span v-if="share.tags.length > 5" class="more-tags">
              +{{ share.tags.length - 5 }}
            </span>
          </div>

          <!-- 分享属性 -->
          <div class="share-attributes">
            <span v-if="share.sportType" class="attribute-item">
              <span class="attribute-icon">🏃‍♂️</span>
              {{ share.sportType }}
            </span>
            <span v-if="share.difficultyLevel" class="attribute-item">
              <span class="attribute-icon">📊</span>
              {{ share.difficultyLevel }}
            </span>
            <span v-if="share.targetAudience" class="attribute-item">
              <span class="attribute-icon">🎯</span>
              {{ share.targetAudience }}
            </span>
          </div>

          <div v-if="share.images && share.images.length" class="image-grid">
            <div 
              v-for="(img, idx) in share.images.slice(0, 3)"
              :key="idx"
              class="image-item"
            >
              <img
                :src="'http://localhost:8000/uploads/' + img"
                class="preview-image"
                @error="handleImageError"
              >
            </div>
            <div v-if="share.images.length > 3" class="more-images">
              <span class="more-count">+{{ share.images.length - 3 }}</span>
            </div>
          </div>
        </div>

        <div class="action-bar">
          <button 
            @click="toggleLike(share)"
            :class="['like-btn', { liked: share.isLiked }]"
          >
            <span class="like-icon">{{ share.isLiked ? '❤️' : '🤍' }}</span>
            <span class="like-count">{{ share.likes }}</span>
          </button>
          <button class="comment-btn" @click="toggleComments(share)">
            <span class="comment-icon">💬</span>
            <span class="comment-count">{{ share.comments || 0 }}</span>
          </button>
          <div class="views-info">
            <span class="views-icon">👁️</span>
            <span class="views-count">{{ share.views }}</span>
          </div>
        </div>

        <!-- 评论区域 -->
        <div v-if="share.showComments" class="comments-section">
          <div class="comments-header">
            <h5 class="comments-title">评论 ({{ share.comments || 0 }})</h5>
          </div>
          
          <!-- 评论列表 -->
          <div v-if="share.commentList && share.commentList.length > 0" class="comments-list">
            <div 
              v-for="comment in share.commentList" 
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-header">
                <img 
                  :src="'http://localhost:8000/uploads/'+comment.authorAvatar" 
                  class="comment-avatar"
                  @error="handleAvatarError"
                >
                <div class="comment-info">
                  <span class="comment-author">{{ comment.authorName }}</span>
                  <time class="comment-time">{{ formatTime(comment.createdAt) }}</time>
                </div>
                <button 
                  v-if="isCommentAuthor(comment)"
                  @click="deleteComment(comment.id, share.id)"
                  class="delete-comment-btn"
                >
                  <span class="delete-icon">🗑️</span>
                </button>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
            </div>
          </div>
          
          <div v-else class="no-comments">
            <p>暂无评论，快来发表第一条评论吧！</p>
          </div>
          
          <!-- 发表评论 -->
          <div v-if="isLoggedIn" class="comment-form">
            <div class="comment-input-wrapper">
              <textarea
                v-model="share.newComment"
                placeholder="写下你的评论..."
                class="comment-input"
                rows="2"
                maxlength="500"
              ></textarea>
              <div class="comment-actions">
                <span class="char-count">{{ (share.newComment || '').length }}/500</span>
                <button 
                  @click="submitComment(share)"
                  :disabled="!share.newComment || share.newComment.trim().length === 0"
                  class="submit-comment-btn"
                >
                  发表评论
                </button>
              </div>
            </div>
          </div>
          
          <div v-else class="login-prompt">
            <p>请先 <button @click="goToLogin" class="login-link">登录</button> 后发表评论</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state glass">
      <div class="empty-icon">🎯</div>
      <h3 class="empty-title">暂无推荐</h3>
      <p class="empty-text">完善你的个人资料，获得更精准的推荐</p>
      <button @click="goToProfile" class="profile-btn">
        <span class="btn-icon">👤</span>
        完善资料
      </button>
    </div>

    <!-- 分享详情浮层 -->
    <ShareDetail
      v-if="selectedShareId"
      :shareId="selectedShareId"
      @close="closeShareDetail"
    />
  </div>
</template>

<script>
import axios from 'axios'
import ShareDetail from '../components/ShareDetail.vue'
import { format } from 'date-fns'
import { mapState } from 'vuex'
import RecommendationVisualizer from '../components/RecommendationVisualizer.vue'

export default {
  components: { 
    ShareDetail,
    RecommendationVisualizer
  },
  data() {
    return {
      recommendations: [],
      loading: true,
      recommendationLimit: 10,
      selectedShareId: null,
      showRecommendationInfo: false
    }
  },
  computed: {
    ...mapState(['user']),
    isLoggedIn() {
      return this.user && this.user.isLoggedIn;
    },
    userTraits() {
      if (!this.user) return [];
      const traits = [];
      if (this.user.interest && this.user.interest.length > 0) {
        traits.push(...this.user.interest.slice(0, 2));
      }
      if (this.user.goal && this.user.goal.length > 0) {
        traits.push(this.user.goal[0]);
      }
      if (this.user.age) {
        traits.push(`${this.user.age}岁`);
      }
      if (this.user.gender) {
        traits.push(this.user.gender === 'male' ? '男性' : '女性');
      }
      return traits;
    },
    averageContentScores() {
      if (this.recommendations.length === 0) {
        return {
          sportType: 0,
          difficulty: 0,
          tags: 0,
          target: 0
        };
      }
      
      const scores = this.recommendations.map(share => ({
        sportType: this.getSportTypeMatch(share) ? 100 : 0,
        difficulty: this.getDifficultyScore(share) * 100,
        tags: this.getTagMatchScore(share) * 100,
        target: this.getTargetMatch(share) ? 100 : 0
      }));
      
      return {
        sportType: Math.round(scores.reduce((sum, s) => sum + s.sportType, 0) / scores.length),
        difficulty: Math.round(scores.reduce((sum, s) => sum + s.difficulty, 0) / scores.length),
        tags: Math.round(scores.reduce((sum, s) => sum + s.tags, 0) / scores.length),
        target: Math.round(scores.reduce((sum, s) => sum + s.target, 0) / scores.length)
      };
    },
    averageRecommendationScore() {
      if (this.recommendations.length === 0) return 0;
      return this.recommendations.reduce((sum, share) => sum + (share.recommendationScore || 0), 0) / this.recommendations.length;
    }
  },
  async mounted() {
    await this.fetchRecommendations()
  },
  methods: {
    async fetchRecommendations() {
      try {
        this.loading = true;
        const { data } = await axios.get(
          `http://localhost:8000/api/shares/recommended?userId=${this.user.id}&limit=${this.recommendationLimit}`
        );
        this.recommendations = data.map(share => ({
          ...share,
          isLiked: share.isLiked,
          showComments: false,
          commentList: [],
          newComment: ''
        }));
      } catch (error) {
        console.error('获取推荐失败:', error);
      } finally {
        this.loading = false;
      }
    },

    async refreshRecommendations() {
      await this.fetchRecommendations();
    },

    async toggleLike(share) {
      if (!this.isLoggedIn) {
        alert('请先登录');
        return;
      }
      
      try {
        const { data } = await axios.post(`http://localhost:8000/api/shares/like?shareId=${share.id}&userId=${this.user.id}`);
        share.likes = data.likes;
        share.isLiked = data.isLiked;
      } catch (error) {
        console.error('操作失败:', error);
      }
    },

    async toggleComments(share) {
      if (!this.isLoggedIn) {
        alert('请先登录');
        return;
      }
      
      share.showComments = !share.showComments;
      
      if (share.showComments && share.commentList.length === 0) {
        await this.loadComments(share);
      }
    },

    async loadComments(share) {
      try {
        const { data } = await axios.get(`http://localhost:8000/api/comments/share/${share.id}`);
        share.commentList = data;
      } catch (error) {
        console.error('加载评论失败:', error);
      }
    },

    async submitComment(share) {
      if (!this.isLoggedIn) {
        alert('请先登录');
        return;
      }
      
      if (!share.newComment || share.newComment.trim().length === 0) {
        return;
      }
      
      try {
        const { data } = await axios.post('http://localhost:8000/api/comments/create', null, {
          params: {
            content: share.newComment.trim(),
            shareId: share.id,
            userId: this.user.id
          }
        });
        
        share.commentList.unshift(data);
        share.comments = (share.comments || 0) + 1;
        share.newComment = '';
      } catch (error) {
        console.error('发表评论失败:', error);
        alert('发表评论失败，请重试');
      }
    },

    async deleteComment(commentId, shareId) {
      if (!this.isLoggedIn) {
        alert('请先登录');
        return;
      }
      
      if (!confirm('确定要删除这条评论吗？')) {
        return;
      }
      
      try {
        await axios.delete(`http://localhost:8000/api/comments/${commentId}?userId=${this.user.id}`);
        
        const share = this.recommendations.find(s => s.id === shareId);
        if (share) {
          share.commentList = share.commentList.filter(c => c.id !== commentId);
          share.comments = Math.max(0, (share.comments || 0) - 1);
        }
      } catch (error) {
        console.error('删除评论失败:', error);
        alert('删除评论失败，请重试');
      }
    },

    isCommentAuthor(comment) {
      return this.isLoggedIn && this.user.id === comment.authorId;
    },

    openShareDetail(shareId) {
      this.selectedShareId = shareId;
    },

    closeShareDetail() {
      this.selectedShareId = null;
      this.fetchRecommendations(); // 刷新推荐
    },

    truncateContent(text) {
      return text.length > 100 ? text.substring(0, 100) + '...' : text;
    },

    formatTime(date) {
      return format(new Date(date), 'yyyy-MM-dd HH:mm');
    },

    handleAvatarError(event) {
      event.target.src = 'https://via.placeholder.com/40x40/10b981/ffffff?text=U';
    },

    handleImageError(event) {
      event.target.src = 'https://via.placeholder.com/300x200/f3f4f6/9ca3af?text=图片加载失败';
    },

    sharePost(share) {
      console.log('分享帖子:', share.title);
    },

    goToLogin() {
      this.$router.push('/LoginRegister');
    },

    goToProfile() {
      this.$router.push('/UserProfile');
    },

    showScoreDetails(share) {
      share.showScoreDetails = true;
    },

    hideScoreDetails(share) {
      share.showScoreDetails = false;
    },

    getSportTypeMatch(share) {
      if (!this.user || !this.user.interest || !share.sportType) return false;
      return this.user.interest.includes(share.sportType);
    },

    getDifficultyMatch(share) {
      if (!share.difficultyLevel) return '未知';
      return share.difficultyLevel;
    },

    getDifficultyScore(share) {
      if (!this.user || !share.difficultyLevel) return 0.5;
      
      const age = this.user.age || 25;
      
      switch (share.difficultyLevel.toLowerCase()) {
        case '初级':
          return 1.0;
        case '中级':
          return age >= 18 && age <= 50 ? 0.8 : 0.6;
        case '高级':
          return age >= 20 && age <= 40 ? 0.7 : 0.4;
        default:
          return 0.5;
      }
    },

    getTagMatchScore(share) {
      if (!this.user || !this.user.interest || !share.tags || share.tags.length === 0) return 0;
      
      const userInterests = new Set(this.user.interest);
      const shareTags = new Set(share.tags);
      
      const intersection = new Set([...userInterests].filter(x => shareTags.has(x)));
      const union = new Set([...userInterests, ...shareTags]);
      
      return union.size > 0 ? intersection.size / union.size : 0;
    },

    getTargetMatch(share) {
      if (!this.user || !this.user.goal || !share.targetAudience) return false;
      return this.user.goal.includes(share.targetAudience);
    },

    getPopularityScore(share) {
      const likeScore = Math.min((share.likes || 0) / 100.0, 1.0);
      const viewScore = Math.min((share.views || 0) / 1000.0, 1.0);
      const commentScore = Math.min((share.comments || 0) / 50.0, 1.0);
      
      return likeScore * 0.4 + viewScore * 0.4 + commentScore * 0.2;
    },

    getCollaborativeScore(share) {
      // 这里简化处理，实际应该从后端获取
      // 基于推荐分数的剩余部分估算协同过滤分数
      const contentScore = this.calculateContentScore(share);
      const totalScore = share.recommendationScore || 0;
      const collaborativeScore = (totalScore - contentScore * 0.7) / 0.3;
      
      return Math.max(0, Math.min(1, collaborativeScore));
    },

    calculateContentScore(share) {
      const sportTypeScore = this.getSportTypeMatch(share) ? 0.3 : 0;
      const difficultyScore = this.getDifficultyScore(share) * 0.2;
      const tagScore = this.getTagMatchScore(share) * 0.25;
      const targetScore = this.getTargetMatch(share) ? 0.15 : 0;
      const popularityScore = this.getPopularityScore(share) * 0.1;
      
      return sportTypeScore + difficultyScore + tagScore + targetScore + popularityScore;
    }
  }
}
</script>

<style scoped>
.recommendations-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-header {
  text-align: center;
  padding: 3rem 2rem;
  margin-bottom: 2rem;
  border-radius: var(--radius-2xl);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(59, 130, 246, 0.1));
  z-index: -1;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 1rem;
  letter-spacing: -0.025em;
}

.page-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin: 0;
  font-weight: 400;
}

.settings-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  border-radius: var(--radius-xl);
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.settings-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.refresh-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-lg);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.refresh-icon {
  font-size: 1rem;
}

.settings-content {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.setting-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.setting-item label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.limit-select {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 0.875rem;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.loading-card {
  text-align: center;
  padding: 3rem;
  border-radius: var(--radius-xl);
  min-width: 300px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1.5rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: var(--text-secondary);
  font-size: 1rem;
  margin: 0;
}

.recommendations-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.recommendation-card {
  border-radius: var(--radius-xl);
  padding: 1.5rem;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
}

.recommendation-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: rgba(255, 255, 255, 0.3);
}

.recommendation-score {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
  font-weight: 600;
  box-shadow: var(--shadow-md);
}

.score-label {
  font-size: 0.75rem;
  opacity: 0.9;
}

.score-value {
  font-size: 1rem;
}

.score-details-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
  margin-top: 0.25rem;
}

.score-details-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

.details-icon {
  font-size: 0.875rem;
}

/* 推荐分数详情弹窗样式 */
.score-details-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 3000;
  padding: 1rem;
}

.score-details-content {
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  padding: 2rem;
  width: 600px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.details-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--radius-lg);
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.close-icon {
  font-size: 1.25rem;
  font-weight: bold;
}

.score-breakdown {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.total-score {
  text-align: center;
  padding: 1.5rem;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
}

.total-score .score-label {
  font-size: 1rem;
  opacity: 0.9;
  margin-bottom: 0.5rem;
  display: block;
}

.total-score .score-value {
  font-size: 2rem;
  font-weight: 700;
}

.score-components {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.components-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1rem;
}

.component-item {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.component-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.component-name {
  font-weight: 600;
  color: var(--text-primary);
}

.component-weight {
  background: var(--primary-color);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  font-weight: 500;
}

.component-details {
  padding: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--border-color);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  flex: 1;
  color: var(--text-primary);
  font-weight: 500;
}

.detail-value {
  flex: 0 0 80px;
  text-align: center;
  color: var(--text-secondary);
  font-weight: 500;
}

.detail-score {
  flex: 0 0 60px;
  text-align: right;
  color: var(--primary-color);
  font-weight: 600;
}

.score-explanation {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
}

.explanation-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1rem;
}

.explanation-content p {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0.5rem 0;
}

.explanation-content strong {
  color: var(--text-primary);
}

.share-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  margin-top: 2rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar-container {
  position: relative;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.recommendation-card:hover .user-avatar {
  border-color: var(--primary-color);
  transform: scale(1.05);
}

.avatar-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: var(--success-color);
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: var(--shadow-sm);
}

.user-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.25rem;
}

.share-time {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.share-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background: var(--bg-secondary);
  border-color: var(--primary-color);
  transform: scale(1.05);
}

.action-icon {
  font-size: 1rem;
}

.share-content {
  margin-bottom: 1.5rem;
  cursor: pointer;
}

.share-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1rem;
  line-height: 1.4;
}

.content-preview {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 1rem;
}

.share-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag-item {
  background: rgba(16, 185, 129, 0.1);
  color: var(--primary-color);
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-lg);
  font-size: 0.75rem;
  font-weight: 500;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.more-tags {
  color: var(--text-secondary);
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
}

.share-attributes {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.attribute-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-lg);
  font-size: 0.75rem;
  font-weight: 500;
}

.attribute-icon {
  font-size: 0.875rem;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 0.75rem;
  margin-top: 1rem;
}

.image-item {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  aspect-ratio: 4/3;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-item:hover .preview-image {
  transform: scale(1.05);
}

.more-images {
  position: relative;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 4/3;
}

.more-count {
  color: white;
  font-weight: 600;
  font-size: 1.125rem;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.like-btn, .comment-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: transparent;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  color: var(--text-secondary);
}

.like-btn:hover, .comment-btn:hover {
  background: var(--bg-secondary);
  transform: translateY(-1px);
}

.like-btn.liked {
  color: var(--error-color);
}

.like-icon, .comment-icon {
  font-size: 1.125rem;
}

.like-count, .comment-count {
  font-size: 0.875rem;
  font-weight: 600;
}

.views-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-left: auto;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.views-icon {
  font-size: 1rem;
}

/* 评论区域样式 */
.comments-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.comments-header {
  margin-bottom: 1rem;
}

.comments-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.comment-item {
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border-left: 3px solid var(--primary-color);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  position: relative;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.comment-author {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.comment-time {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.delete-comment-btn {
  background: transparent;
  border: none;
  padding: 0.25rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: var(--radius-sm);
}

.delete-comment-btn:hover {
  background: var(--bg-tertiary);
  transform: scale(1.1);
}

.delete-icon {
  font-size: 0.875rem;
}

.comment-content {
  color: var(--text-primary);
  line-height: 1.5;
  margin: 0;
  font-size: 0.875rem;
}

.no-comments {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
  font-style: italic;
}

.comment-form {
  margin-top: 1rem;
}

.comment-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.comment-input {
  width: 100%;
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  background: var(--bg-primary);
  color: var(--text-primary);
  resize: vertical;
  min-height: 60px;
  transition: all 0.3s ease;
}

.comment-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.submit-comment-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-comment-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.submit-comment-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.login-prompt {
  text-align: center;
  padding: 1rem;
  color: var(--text-secondary);
}

.login-link {
  background: none;
  border: none;
  color: var(--primary-color);
  text-decoration: underline;
  cursor: pointer;
  font-weight: 500;
}

.login-link:hover {
  color: var(--primary-dark);
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  border-radius: var(--radius-xl);
  margin-top: 2rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.7;
}

.empty-title {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin: 0 0 0.5rem;
  font-weight: 600;
}

.empty-text {
  color: var(--text-secondary);
  margin: 0 0 2rem;
  font-size: 1rem;
}

.profile-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: var(--radius-lg);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: var(--shadow-md);
}

.profile-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.btn-icon {
  font-size: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .recommendations-container {
    padding: 0 0.5rem;
  }
  
  .page-header {
    padding: 2rem 1rem;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .settings-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .refresh-btn {
    width: 100%;
    justify-content: center;
  }
  
  .recommendation-score {
    position: static;
    align-self: flex-end;
    margin-bottom: 1rem;
  }
  
  .share-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .action-bar {
    flex-wrap: wrap;
    gap: 1rem;
  }
  
  .views-info {
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .image-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .recommendation-card {
    padding: 1rem;
  }
  
  .user-avatar {
    width: 40px;
    height: 40px;
  }
  
  .share-title {
    font-size: 1.125rem;
  }
  
  .share-attributes {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .comment-item {
    padding: 0.75rem;
  }
  
  .comment-avatar {
    width: 28px;
    height: 28px;
  }
}
</style> 