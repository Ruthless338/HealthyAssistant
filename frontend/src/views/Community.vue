<template>
  <div class="community-container">
    <!-- 页面标题 -->
    <div class="page-header glass">
      <h1 class="page-title gradient-text">运动社区</h1>
      <p class="page-subtitle">分享你的运动心得，与志同道合的朋友交流</p>
    </div>

    <!-- 推荐系统说明 -->
    <div class="recommendation-info glass">
      <div class="info-header">
        <span class="info-icon">🎯</span>
        <h3 class="info-title">个性化推荐</h3>
        <button @click="showRecommendationInfo = !showRecommendationInfo" class="info-toggle">
          <span class="toggle-icon">{{ showRecommendationInfo ? '▼' : '▶' }}</span>
        </button>
      </div>
      <div v-if="showRecommendationInfo" class="info-content">
        <p class="info-text">
          我们的推荐系统基于您的个人特征和偏好，为您精选最相关的运动分享内容。
        </p>
        <div class="recommendation-factors">
          <div class="factor-item">
            <span class="factor-icon">🏃‍♂️</span>
            <span class="factor-text">运动兴趣匹配</span>
          </div>
          <div class="factor-item">
            <span class="factor-icon">📊</span>
            <span class="factor-text">难度等级适配</span>
          </div>
          <div class="factor-item">
            <span class="factor-icon">🏷️</span>
            <span class="factor-text">标签相似度</span>
          </div>
          <div class="factor-item">
            <span class="factor-icon">🎯</span>
            <span class="factor-text">健身目标匹配</span>
          </div>
          <div class="factor-item">
            <span class="factor-icon">👥</span>
            <span class="factor-text">相似用户偏好</span>
          </div>
        </div>
        <div class="info-actions">
          <router-link to="/Recommendations" class="recommendation-link">
            <span class="link-icon">🎯</span>
            查看个性化推荐
          </router-link>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section glass">
      <div class="search-bar">
        <div class="search-input-wrapper">
          <span class="search-icon">🔍</span>
          <input
            v-model="searchKeyword"
            type="search"
            placeholder="搜索运动分享..."
            @keyup.enter="searchShares"
            class="search-input"
          >
        </div>
        <button @click="searchShares" class="search-button">
          <span class="search-btn-icon">🔍</span>
          搜索
        </button>
        <button v-if="searchKeyword" @click="clearSearch" class="clear-button">
          <span class="clear-btn-icon">✕</span>
          清除
        </button>
      </div>
    </div>

    <!-- 分享列表 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-card glass">
        <div class="loading-spinner"></div>
        <p class="loading-text">正在加载分享内容...</p>
      </div>
    </div>

    <div v-else class="share-list">
      <div 
        v-for="share in shares"
        :key="share.id"
        class="share-card glass"
      >
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

    <!-- 分享详情浮层 -->
    <ShareDetail
      v-if="selectedShareId"
      :shareId="selectedShareId"
      @close="closeShareDetail"
    />

    <!-- 新建分享按钮 -->
    <button 
      class="fab glass"
      @click="showShareForm = true"
    >
      <span class="fab-icon">✏️</span>
      <span class="fab-text">分享</span>
    </button>

    <ShareForm
      v-if="showShareForm"
      @close="showShareForm = false"
      @created="handleNewShare"
    />
  </div>
</template>

<script>
import axios from 'axios'
import ShareForm from '../components/ShareForm.vue'
import ShareDetail from '../components/ShareDetail.vue'
import { format } from 'date-fns'
import { mapState } from 'vuex'

export default {
  components: { 
    ShareForm, 
    ShareDetail 
  },
  data() {
    return {
      shares: [],
      searchKeyword: '',
      loading: true,
      showShareForm: false,
      selectedShareId: null,
      showRecommendationInfo: false
    }
  },
  computed: {
    ...mapState(['user']),
    isLoggedIn() {
      return this.user && this.user.isLoggedIn;
    }
  },
  async mounted() {
    await this.fetchShares()
  },
  methods: {
    async fetchShares() {
      try {
        const { data } = await axios.get('http://localhost:8000/api/shares/all');
        console.log('获取分享:', data)
        this.shares = data.map(share => ({
          ...share,
          isLiked: share.isLiked,
          showComments: false,
          commentList: [],
          newComment: ''
        }))
      } catch (error) {
        console.error('获取分享失败:', error)
      } finally {
        this.loading = false
      }
    },

    async searchShares() {
      if (!this.searchKeyword.trim()) return this.fetchShares()
      
      try {
        this.loading = true;
        const { data } = await axios.get(
          `http://localhost:8000/api/shares/search?keyword=${encodeURIComponent(this.searchKeyword)}`
        )
        this.shares = data.map(share => ({
          ...share,
          isLiked: share.isLiked,
          showComments: false,
          commentList: [],
          newComment: ''
        }))
        console.log('搜索结果:', this.shares)
      } catch (error) {
        console.error('搜索失败:', error)
      } finally {
        this.loading = false
      }
    },

    clearSearch() {
      this.searchKeyword = '';
      this.fetchShares();
    },

    async toggleLike(share) {
      if (!this.isLoggedIn) {
        alert('请先登录');
        return;
      }
      
      try {
        const { data } = await axios.post(`http://localhost:8000/api/shares/like?shareId=${share.id}&userId=${this.user.id}`)
        share.likes = data.likes
        share.isLiked = data.isLiked
      } catch (error) {
        console.error('操作失败:', error)
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
        
        // 添加新评论到列表
        share.commentList.unshift(data);
        share.comments = (share.comments || 0) + 1;
        share.newComment = '';
        
        console.log('评论发表成功:', data);
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
        
        // 从列表中移除评论
        const share = this.shares.find(s => s.id === shareId);
        if (share) {
          share.commentList = share.commentList.filter(c => c.id !== commentId);
          share.comments = Math.max(0, (share.comments || 0) - 1);
        }
        
        console.log('评论删除成功');
      } catch (error) {
        console.error('删除评论失败:', error);
        alert('删除评论失败，请重试');
      }
    },

    isCommentAuthor(comment) {
      return this.isLoggedIn && this.user.id === comment.authorId;
    },

    openShareDetail(shareId) {
      this.selectedShareId = shareId
    },

    closeShareDetail() {
      this.selectedShareId = null
      this.fetchShares() // 刷新数据
    },

    handleNewShare() {
      this.showShareForm = false
      this.fetchShares()
    },

    truncateContent(text) {
      return text.length > 100 ? text.substring(0, 100) + '...' : text
    },

    formatTime(date) {
      return format(new Date(date), 'yyyy-MM-dd HH:mm')
    },

    handleAvatarError(event) {
      event.target.src = 'https://via.placeholder.com/40x40/10b981/ffffff?text=U'
    },

    handleImageError(event) {
      event.target.src = 'https://via.placeholder.com/300x200/f3f4f6/9ca3af?text=图片加载失败'
    },

    sharePost(share) {
      console.log('分享帖子:', share.title)
      // 这里可以添加分享功能
    },

    goToLogin() {
      this.$router.push('/LoginRegister');
    }
  }
}
</script>

<style scoped>
.community-container {
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

.recommendation-info {
  margin-bottom: 2rem;
  padding: 1.5rem;
  border-radius: var(--radius-xl);
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.info-icon {
  font-size: 1.5rem;
  color: var(--primary-color);
}

.info-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.info-toggle {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.5rem;
  color: var(--text-secondary);
}

.toggle-icon {
  font-size: 1.5rem;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-text {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.recommendation-factors {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.factor-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.factor-icon {
  font-size: 1.25rem;
  color: var(--primary-color);
}

.factor-text {
  font-size: 1rem;
  color: var(--text-primary);
}

.info-actions {
  text-align: right;
}

.recommendation-link {
  background: none;
  border: none;
  color: var(--primary-color);
  text-decoration: underline;
  cursor: pointer;
  font-weight: 500;
}

.recommendation-link:hover {
  color: var(--primary-dark);
}

.link-icon {
  font-size: 1.25rem;
}

.search-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  border-radius: var(--radius-xl);
}

.search-bar {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 1rem;
  font-size: 1.125rem;
  color: var(--text-secondary);
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 1rem 1rem 1rem 3rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  font-size: 1rem;
  background: var(--bg-primary);
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
  outline: none;
}

.search-button {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border: none;
  padding: 1rem 1.5rem;
  border-radius: var(--radius-lg);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.search-btn-icon {
  font-size: 1rem;
}

.clear-button {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  padding: 1rem 1.5rem;
  border-radius: var(--radius-lg);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.clear-button:hover {
  background: var(--bg-tertiary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.clear-btn-icon {
  font-size: 1rem;
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

.share-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.share-card {
  border-radius: var(--radius-xl);
  padding: 1.5rem;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.share-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: rgba(255, 255, 255, 0.3);
}

.share-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
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

.share-card:hover .user-avatar {
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
  margin: 0 0 1.5rem;
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

.fab {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  box-shadow: var(--shadow-lg);
  z-index: 1000;
}

.fab:hover {
  transform: scale(1.1) translateY(-2px);
  box-shadow: var(--shadow-xl);
}

.fab-icon {
  font-size: 1.25rem;
}

.fab-text {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--text-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .community-container {
    padding: 0 0.5rem;
  }
  
  .page-header {
    padding: 2rem 1rem;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .search-bar {
    flex-direction: column;
    gap: 1rem;
  }
  
  .search-button, .clear-button {
    width: 100%;
    justify-content: center;
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
  
  .fab {
    bottom: 1rem;
    right: 1rem;
    width: 50px;
    height: 50px;
  }
  
  .fab-text {
    display: none;
  }
}

@media (max-width: 480px) {
  .image-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .share-card {
    padding: 1rem;
  }
  
  .user-avatar {
    width: 40px;
    height: 40px;
  }
  
  .share-title {
    font-size: 1.125rem;
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