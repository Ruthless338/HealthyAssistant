<template>
  <div class="user-profile-container">
    <!-- 用户信息头部 -->
    <div class="profile-header">
      <div class="avatar-section">
        <img :src="user.avatar" class="user-avatar">
        <button class="edit-btn" @click="handleEdit">编辑资料</button>
      </div>
      
      <div class="basic-info">
        <h1 class="username">{{ user.username }}</h1>
        <div class="meta-info">
          <span class="gender-tag">{{ user.gender }}</span>
          <span>年龄 {{ user.age }}</span>
          <span>身高 {{ user.height }}cm</span>
          <span>体重 {{ user.weight }}kg</span>
        </div>
        
        <div class="tags-group">
          <div class="tag" v-for="(goal, index) in user.goal" :key="'goal'+index">
            🎯 {{ goal }}
          </div>
          <div class="tag" v-for="(interest, index) in user.interest" :key="'interest'+index">
            ⚡ {{ interest }}
          </div>
          <div class="tag" v-for="(part, index) in user.part" :key="'part'+index">
            💪 {{ part }}
          </div>
        </div>
      </div>
    </div>

    <!-- 用户分享列表 -->
    <div class="shares-section">
      <h2 class="section-title">我的运动分享 ({{ shares.length }})</h2>
      
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
      </div>

      <div v-else-if="shares.length" class="share-grid">
        <div 
          v-for="share in shares"
          :key="share.id"
          class="share-card"
          @click="openShareDetail(share.id)"
        >
          <div class="card-header">
            <time>{{ formatTime(share.createdAt) }}</time>
            <span class="views-count">
              <i class="fas fa-eye"></i> {{ share.views }}
            </span>
          </div>
          
          <h4 class="share-title">{{ share.title }}</h4>
          <p class="content-preview">{{ truncateContent(share.content) }}</p>
          
          <div v-if="share.images.length" class="image-preview">
            <img 
              :src="share.images[0]" 
              class="main-image"
              alt="分享主图"
            >
            <div v-if="share.images.length > 1" class="more-images">
              +{{ share.images.length - 1 }}
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <!-- <img src="@/assets/no-content.svg" class="empty-illustration"> -->
        <p>还没有任何分享，快去创建你的第一篇吧！</p>
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
      const maxLength = 40
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
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

.user-avatar {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #007bff;
  box-shadow: 0 8px 16px rgba(0,123,255,0.2);
}

.basic-info {
  padding-top: 1rem;
}

.username {
  font-size: 2.2rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.meta-info {
  display: flex;
  gap: 1.5rem;
  color: #4a5568;
  margin-bottom: 1.5rem;
}

.gender-tag {
  background: #007bff;
  color: white;
  padding: 0.2rem 0.8rem;
  border-radius: 20px;
  font-size: 0.9rem;
}

.tags-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.tag {
  background: rgba(0,123,255,0.1);
  color: #007bff;
  padding: 0.4rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  border: 1px solid rgba(0,123,255,0.2);
}

.shares-section {
  margin-top: 3rem;
}

.section-title {
  font-size: 1.5rem;
  color: #2c3e50;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.share-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.share-card {
  background: white;
  border-radius: 12px;
  padding: 1.2rem;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid rgba(0,0,0,0.05);
}

.share-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  color: #718096;
  font-size: 0.9rem;
  margin-bottom: 0.8rem;
}

.share-title {
  color: #2c3e50;
  margin-bottom: 0.6rem;
  font-size: 1.1rem;
}

.content-preview {
  color: #4a5568;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.image-preview {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 16/9;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.more-images {
  position: absolute;
  right: 0.5rem;
  bottom: 0.5rem;
  background: rgba(0,0,0,0.7);
  color: white;
  padding: 0.2rem 0.8rem;
  border-radius: 20px;
  font-size: 0.9rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 0;
  color: #718096;
}

.empty-illustration {
  width: 200px;
  opacity: 0.8;
  margin-bottom: 1rem;
}

.edit-btn {
  display: block;
  width: 100%;
  margin-top: 1rem;
  padding: 0.6rem;
  background: rgba(0,123,255,0.1);
  color: #007bff;
  border: 1px solid rgba(0,123,255,0.3);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn:hover {
  background: #007bff;
  color: white;
}

.loading {
  text-align: center;
  padding: 2rem;
}

</style>