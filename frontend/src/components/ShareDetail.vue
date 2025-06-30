<template>
  <div class="share-detail-overlay" @click.self="close">
    <div class="share-detail-container glass">
      <button class="close-btn" @click="close">
        <span class="close-icon">✕</span>
      </button>
      
      <!-- 用户信息区域 -->
      <div class="user-info-section">
        <div class="user-avatar-container">
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
        <div class="share-actions">
          <button class="action-btn share-btn" @click="sharePost">
            <span class="action-icon">📤</span>
          </button>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-section">
        <h2 class="share-title">{{ share.title }}</h2>
        <p class="share-content">{{ share.content }}</p>
        
        <!-- 图片画廊 -->
        <div v-if="share.images && share.images.length" class="image-gallery">
          <div 
            v-for="(img, idx) in share.images"
            :key="idx"
            class="gallery-item"
            @click="openLightbox('http://localhost:8000/uploads/'+img)"
          >
            <img 
            :src="'http://localhost:8000/uploads/'+img"
              class="gallery-image"
              @error="handleImageError"
          >
          </div>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <button 
          @click="toggleLike"
          :class="['like-btn', { liked: share.isLiked }]"
        >
          <span class="like-icon">{{ share.isLiked ? '❤️' : '🤍' }}</span>
          <span class="like-count">{{ share.likes }}</span>
        </button>
        <button class="comment-btn" @click="toggleComments">
          <span class="comment-icon">💬</span>
          <span class="comment-count">{{ share.comments || 0 }}</span>
        </button>
        <div class="views-info">
          <span class="views-icon">👁️</span>
          <span class="views-count">{{ share.views }}</span>
        </div>
      </div>

      <!-- 评论区域 -->
      <div v-if="showComments" class="comments-section">
        <div class="comments-header">
          <h4 class="comments-title">评论 ({{ share.comments || 0 }})</h4>
        </div>
        
        <!-- 评论列表 -->
        <div v-if="commentList && commentList.length > 0" class="comments-list">
          <div 
            v-for="comment in commentList" 
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
                @click="deleteComment(comment.id)"
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
              v-model="newComment"
              placeholder="写下你的评论..."
              class="comment-input"
              rows="3"
              maxlength="500"
            ></textarea>
            <div class="comment-actions">
              <span class="char-count">{{ (newComment || '').length }}/500</span>
              <button 
                @click="submitComment"
                :disabled="!newComment || newComment.trim().length === 0"
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
  
  <!-- 灯箱 -->
  <transition name="fade">
    <div v-if="lightboxVisible" class="lightbox" @click="closeLightbox">
      <img :src="selectedImage" class="lightbox-image">
      <button class="lightbox-close" @click="closeLightbox">
        <span class="close-icon">✕</span>
      </button>
    </div>
  </transition>
</template>

<script>
import axios from 'axios'
import { format } from 'date-fns'
import { mapState } from 'vuex'

export default {
  props: ['shareId'],
  data() {
    return {
      share: {},
      commentList: [],
      showComments: false,
      newComment: '',
      lightboxVisible: false,
      selectedImage: '',
      loading: true
    }
  },
  computed: {
    ...mapState(['user']),
    isLoggedIn() {
      return this.user && this.user.isLoggedIn;
    }
  },
  async created() {
    await this.fetchShare()
    await this.incrementViews()
  },
  methods: {
    async fetchShare() {
      try {
        const { data } = await axios.get(`http://localhost:8000/api/shares/${this.shareId}`)
        this.share = {
          ...data,
          isLiked: data.isLiked
        }
      } catch (error) {
        console.error('获取详情失败:', error)
      } finally {
        this.loading = false
      }
    },

    async incrementViews() {
      try {
        await axios.get(`http://localhost:8000/api/shares/${this.shareId}/view`)
      } catch (error) {
        console.error('增加浏览量失败:', error)
      }
    },

    async toggleLike() {
      if (!this.isLoggedIn) {
        alert('请先登录');
        return;
      }
      
      try {
        const { data } = await axios.post(`http://localhost:8000/api/shares/like?shareId=${this.shareId}&userId=${this.user.id}`)
        this.share.likes = data.likes
        this.share.isLiked = data.isLiked
      } catch (error) {
        console.error('操作失败:', error)
      }
    },

    async toggleComments() {
      if (!this.isLoggedIn) {
        alert('请先登录');
        return;
      }
      
      this.showComments = !this.showComments;
      
      if (this.showComments && this.commentList.length === 0) {
        await this.loadComments();
      }
    },

    async loadComments() {
      try {
        const { data } = await axios.get(`http://localhost:8000/api/comments/share/${this.shareId}`);
        this.commentList = data;
      } catch (error) {
        console.error('加载评论失败:', error);
      }
    },

    async submitComment() {
      if (!this.isLoggedIn) {
        alert('请先登录');
        return;
      }
      
      if (!this.newComment || this.newComment.trim().length === 0) {
        return;
      }
      
      try {
        const { data } = await axios.post('http://localhost:8000/api/comments/create', null, {
          params: {
            content: this.newComment.trim(),
            shareId: this.shareId,
            userId: this.user.id
          }
        });
        
        // 添加新评论到列表
        this.commentList.unshift(data);
        this.share.comments = (this.share.comments || 0) + 1;
        this.newComment = '';
        
        console.log('评论发表成功:', data);
      } catch (error) {
        console.error('发表评论失败:', error);
        alert('发表评论失败，请重试');
      }
    },

    async deleteComment(commentId) {
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
        this.commentList = this.commentList.filter(c => c.id !== commentId);
        this.share.comments = Math.max(0, (this.share.comments || 0) - 1);
        
        console.log('评论删除成功');
      } catch (error) {
        console.error('删除评论失败:', error);
        alert('删除评论失败，请重试');
      }
    },

    isCommentAuthor(comment) {
      return this.isLoggedIn && this.user.id === comment.authorId;
    },

    openLightbox(imageUrl) {
      this.selectedImage = imageUrl;
      this.lightboxVisible = true;
    },

    closeLightbox() {
      this.lightboxVisible = false;
      this.selectedImage = '';
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

    sharePost() {
      console.log('分享帖子:', this.share.title)
    },

    goToLogin() {
      this.$router.push('/LoginRegister');
    },

    close() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.share-detail-overlay {
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
  z-index: 1000;
  padding: 1rem;
}

.share-detail-container {
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  padding: 2rem;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.2);
  width: 100%;
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
}

.close-btn:hover {
  background: var(--bg-tertiary);
  transform: scale(1.1);
}

.close-icon {
  font-size: 1.25rem;
  color: var(--text-secondary);
}

/* 用户信息区域 */
.user-info-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.user-avatar-container {
  position: relative;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--primary-color);
  transition: all 0.3s ease;
}

.avatar-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
  background: var(--success-color);
  border: 2px solid var(--bg-primary);
  border-radius: 50%;
  box-shadow: var(--shadow-sm);
}

.user-details {
  flex: 1;
}

.author-name {
  font-size: 1.25rem;
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

/* 内容区域 */
.content-section {
  margin-bottom: 2rem;
}

.share-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 1rem;
  line-height: 1.3;
}

.share-content {
  font-size: 1.125rem;
  line-height: 1.7;
  color: var(--text-secondary);
  margin: 0 0 1.5rem;
}

/* 图片画廊 */
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-top: 1.5rem;
}

.gallery-item {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  aspect-ratio: 4/3;
  cursor: pointer;
  transition: all 0.3s ease;
}

.gallery-item:hover {
  transform: scale(1.02);
  box-shadow: var(--shadow-md);
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.gallery-item:hover .gallery-image {
  transform: scale(1.05);
}

/* 统计信息 */
.stats-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

.like-btn, .comment-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: transparent;
  border: none;
  padding: 0.75rem 1rem;
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

/* 评论区域 */
.comments-section {
  margin-top: 1.5rem;
}

.comments-header {
  margin-bottom: 1rem;
}

.comments-title {
  font-size: 1.125rem;
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
  min-height: 80px;
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

/* 灯箱样式 */
.lightbox {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 2rem;
}

.lightbox-image {
  max-width: 90%;
  max-height: 90vh;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  object-fit: contain;
}

.lightbox-close {
  position: absolute;
  top: 2rem;
  right: 2rem;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.lightbox-close:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.lightbox-close .close-icon {
  color: white;
  font-size: 1.5rem;
}

/* 动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .share-detail-container {
    padding: 1.5rem;
    margin: 0.5rem;
  }
  
  .user-info-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .share-title {
    font-size: 1.5rem;
  }
  
  .share-content {
    font-size: 1rem;
  }
  
  .image-gallery {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stats-section {
    flex-wrap: wrap;
    gap: 1rem;
  }
  
  .views-info {
    margin-left: 0;
  }
  
  .lightbox {
    padding: 1rem;
  }
  
  .lightbox-close {
    top: 1rem;
    right: 1rem;
    width: 40px;
    height: 40px;
  }
}

@media (max-width: 480px) {
  .image-gallery {
    grid-template-columns: 1fr;
  }
  
  .share-detail-container {
    padding: 1rem;
  }
  
  .user-avatar {
    width: 50px;
    height: 50px;
  }
  
  .share-title {
    font-size: 1.25rem;
  }
}
</style>