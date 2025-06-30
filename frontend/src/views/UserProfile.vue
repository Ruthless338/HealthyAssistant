<template>
  <div class="user-profile-container">
    <!-- 用户信息头部 -->
    <div class="profile-header glass">
      <div class="avatar-section">
        <div class="avatar-wrapper">
        <img :src="'http://localhost:8000/uploads/'+user.avatar" class="user-avatar">
          <div class="avatar-status"></div>
        </div>
        <button class="edit-btn" @click="handleEdit">
          <span class="edit-icon">✏️</span>
          编辑资料
        </button>
      </div>
      
      <div class="basic-info">
        <h1 class="username gradient-text">{{ user.username }}</h1>
        <div class="meta-info">
          <span class="gender-tag">{{ user.gender === 'male' ? '👨 男性' : '👩 女性' }}</span>
          <span class="age-info">🎂 {{ user.age }}岁</span>
          <span class="height-info">📏 {{ user.height }}cm</span>
          <span class="weight-info">⚖️ {{ user.weight }}kg</span>
        </div>
        
        <div class="tags-group">
          <div class="tag goal-tag" v-for="(goal, index) in user.goal" :key="'goal'+index">
            🎯 {{ goal }}
          </div>
          <div class="tag interest-tag" v-for="(interest, index) in user.interest" :key="'interest'+index">
            ⚡ {{ interest }}
          </div>
          <div class="tag part-tag" v-for="(part, index) in user.part" :key="'part'+index">
            💪 {{ part }}
          </div>
        </div>
      </div>
    </div>

    <!-- 用户分享列表 -->
    <div class="shares-section">
      <div class="section-header glass">
        <h2 class="section-title gradient-text">我的运动分享</h2>
        <span class="share-count">({{ shares.length }})</span>
      </div>
      
      <div v-if="loading" class="loading-container">
        <div class="loading-card glass">
          <div class="loading-spinner"></div>
          <p class="loading-text">正在加载分享内容...</p>
        </div>
      </div>

      <div v-else-if="shares.length" class="share-grid">
        <div 
          v-for="share in shares"
          :key="share.id"
          class="share-card glass"
          @click="openShareDetail(share.id)"
        >
          <div class="card-header">
            <time class="share-time">{{ formatTime(share.createdAt) }}</time>
            <span class="views-count">
              <span class="views-icon">👁️</span> {{ share.views }}
            </span>
          </div>
          
          <h4 class="share-title">{{ share.title }}</h4>
          <p class="content-preview">{{ truncateContent(share.content) }}</p>
          
          <div v-if="share.images.length" class="image-preview">
            <img 
              :src="'http://localhost:8000/uploads/'+share.images[0]" 
              class="main-image"
              alt="分享主图"
            >
            <div v-if="share.images.length > 1" class="more-images">
              +{{ share.images.length - 1 }}
            </div>
          </div>
          
          <div class="card-stats">
            <span class="stat-item">
              <span class="stat-icon">❤️</span>
              {{ share.likes || 0 }}
            </span>
            <span class="stat-item">
              <span class="stat-icon">💬</span>
              {{ share.comments || 0 }}
            </span>
          </div>
        </div>
      </div>

      <div v-else class="empty-state glass">
        <div class="empty-icon">📝</div>
        <h3 class="empty-title">还没有任何分享</h3>
        <p class="empty-text">快去创建你的第一篇运动分享吧！</p>
        <button class="create-share-btn" @click="createShare">
          <span class="btn-icon">✏️</span>
          创建分享
        </button>
      </div>
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

export default {
  name: 'UserProfile',
  components: { ShareDetail },
  data() {
    return {
      user: {},
      shares: [],
      loading: true,
      selectedShareId: null
    }
  },
  async mounted() {
    await this.fetchUserData()
    await this.fetchUserShares()
  },
  methods: {
    async fetchUserData() {
      this.user = this.$store.state.user
    },

    async fetchUserShares() {
      try {
        const userId = this.$store.state.user.id
        const { data } = await axios.get(
          `http://localhost:8000/api/shares/get?userId=${userId}`
        )
        this.shares = data
      } catch (error) {
        console.error('获取分享失败:', error)
      } finally {
        this.loading = false
      }
    },

    truncateContent(text) {
      const maxLength = 60
      return text.length > maxLength ? 
        text.substring(0, maxLength) + '...' : 
        text
    },

    formatTime(date) {
      return format(new Date(date), 'yyyy/MM/dd HH:mm')
    },

    openShareDetail(shareId) {
      this.selectedShareId = shareId
    },

    closeShareDetail() {
      this.selectedShareId = null
      this.fetchUserShares() // 刷新数据
    },

    handleEdit() {
      this.$router.push({name:"Selector"});
    },

    createShare() {
      this.$router.push('/Community');
    }
  }
}
</script>

<style scoped>
.user-profile-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.profile-header {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 2rem;
  background: linear-gradient(135deg, #f8f9fa, #ffffff);
  padding: 2rem;
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
  margin-bottom: 2rem;
  position: relative;
  overflow: hidden;
}

.profile-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(59, 130, 246, 0.1));
  z-index: -1;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.avatar-wrapper {
  position: relative;
}

.user-avatar {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--primary-color);
  box-shadow: var(--shadow-lg);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-xl);
}

.avatar-status {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  background: var(--success-color);
  border: 3px solid white;
  border-radius: 50%;
  box-shadow: var(--shadow-md);
}

.edit-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: var(--radius-lg);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: var(--shadow-md);
}

.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.edit-icon {
  font-size: 1rem;
}

.basic-info {
  padding-top: 1rem;
}

.username {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 1rem;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.meta-info {
  display: flex;
  gap: 1.5rem;
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.gender-tag, .age-info, .height-info, .weight-info {
  background: var(--bg-secondary);
  padding: 0.5rem 1rem;
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  font-weight: 500;
  border: 1px solid var(--border-color);
}

.tags-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.tag {
  padding: 0.5rem 1rem;
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  font-weight: 500;
  border: 1px solid;
}

.goal-tag {
  background: rgba(16, 185, 129, 0.1);
  color: var(--primary-color);
  border-color: rgba(16, 185, 129, 0.2);
}

.interest-tag {
  background: rgba(59, 130, 246, 0.1);
  color: var(--secondary-color);
  border-color: rgba(59, 130, 246, 0.2);
}

.part-tag {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
  border-color: rgba(245, 158, 11, 0.2);
}

.shares-section {
  margin-top: 2rem;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem 2rem;
  border-radius: var(--radius-xl);
  margin-bottom: 2rem;
  background: var(--bg-primary);
  box-shadow: var(--shadow-md);
}

.section-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.share-count {
  color: var(--text-secondary);
  font-size: 1rem;
  font-weight: 500;
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
  background: var(--bg-primary);
  box-shadow: var(--shadow-md);
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

.share-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.share-card {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: var(--shadow-md);
}

.share-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: rgba(255, 255, 255, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.share-time {
  color: var(--text-secondary);
}

.views-count {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--text-secondary);
}

.views-icon {
  font-size: 0.875rem;
}

.share-title {
  color: var(--text-primary);
  margin-bottom: 0.75rem;
  font-size: 1.125rem;
  font-weight: 600;
  line-height: 1.4;
}

.content-preview {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}

.image-preview {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  aspect-ratio: 16/9;
  margin-bottom: 1rem;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.share-card:hover .main-image {
  transform: scale(1.05);
}

.more-images {
  position: absolute;
  right: 0.5rem;
  bottom: 0.5rem;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  font-weight: 500;
}

.card-stats {
  display: flex;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.stat-icon {
  font-size: 1rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  border-radius: var(--radius-xl);
  margin-top: 2rem;
  background: var(--bg-primary);
  box-shadow: var(--shadow-md);
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

.create-share-btn {
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

.create-share-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.btn-icon {
  font-size: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile-container {
    padding: 0 0.5rem;
}

  .profile-header {
    grid-template-columns: 1fr;
  text-align: center;
    gap: 1.5rem;
    padding: 1.5rem;
  }
  
  .user-avatar {
    width: 150px;
    height: 150px;
  }
  
  .username {
    font-size: 2rem;
}

  .meta-info {
    justify-content: center;
    gap: 1rem;
  }
  
  .tags-group {
    justify-content: center;
  }
  
  .share-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    padding: 1rem 1.5rem;
  }
  
  .section-title {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .profile-header {
    padding: 1rem;
  }
  
  .user-avatar {
    width: 120px;
    height: 120px;
  }
  
  .username {
    font-size: 1.75rem;
  }
  
  .meta-info {
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
  }
  
  .share-card {
    padding: 1rem;
  }
  
  .empty-state {
    padding: 3rem 1rem;
  }
  
  .empty-icon {
    font-size: 3rem;
  }
  
  .empty-title {
    font-size: 1.25rem;
  }
}
</style>