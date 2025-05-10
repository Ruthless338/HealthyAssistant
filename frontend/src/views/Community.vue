<template>
  <div class="community-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
        v-model="searchKeyword"
        type="search"
        placeholder="搜索运动分享..."
        @keyup.enter="searchShares"
      >
      <button @click="searchShares">
        <i class="fas fa-search"></i>
      </button>
    </div>

    <!-- 分享列表 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>

    <div v-else class="share-list">
      <!-- <transition-group 
      name="staggered-fade"
      tag="div"
      class="transition-wrapper"
      appear
      > -->
        <div 
          v-for="share in shares"
          :key="share.id"
          class="share-card"
          @click="openShareDetail(share.id)"
        >
          <div class="user-info">
            <img 
              :src="share.author.avatar"
              class="user-avatar"
            >
            <div>
              <h3>{{ share.author.username }}</h3>
              <time>{{ formatTime(share.createdAt) }}</time>
            </div>
          </div>

          <div class="share-content">
            <h4>{{ share.title }}</h4>
            <p class="content-preview">{{ truncateContent(share.content) }}</p>
            <div v-if="share.images.length" class="image-grid">
              <img
                v-for="(img, idx) in share.images.slice(0, 3)"
                :key="idx"
                :src="img"
                class="preview-image"
              >
              <div v-if="share.images.length > 3" class="more-images">
                +{{ share.images.length - 3 }}
              </div>
            </div>
          </div>

          <div class="action-bar">
            <button 
              @click.stop="toggleLike(share)"
              :class="{ liked: share.isLiked }"
            >
              <i class="fas fa-heart"></i>
              {{ share.likes }}
            </button>
            <span class="views-count">
              <i class="fas fa-eye"></i>
              {{ share.views }}
            </span>
          </div>
        </div>
      <!-- </transition-group> -->
    </div>

    <!-- 分享详情浮层 -->
    <ShareDetail
      v-if="selectedShareId"
      :shareId="selectedShareId"
      @close="closeShareDetail"
    />

    <!-- 新建分享按钮 -->
    <button 
      class="fab"
      @click="showShareForm = true"
    >
      <i class="fas fa-plus"></i>
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
      selectedShareId: null
    }
  },
  async mounted() {
    await this.fetchShares()
  },
  methods: {
    async fetchShares() {
      try {
        const { data } = await axios.get('http://localhost:8000/api/shares/all')
        this.shares = data.map(share => ({
          ...share,
          isLiked: share.isLiked
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
        const { data } = await axios.get(
          `http://localhost:8000/api/shares/search?keyword=${encodeURIComponent(this.searchKeyword)}`
        )
        this.shares = data
      } catch (error) {
        console.error('搜索失败:', error)
      }
    },

    async toggleLike(share) {
      try {
        const { data } = await axios.post(`http://localhost:8000/api/shares/like?shareId=${share.id}&userId=${this.$store.state.user.id}`)
        share.likes = data.likes
        share.isLiked = data.isLiked
      } catch (error) {
        console.error('操作失败:', error)
      }
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
    }
  }
}
</script>

<style scoped>
.community-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.search-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.search-bar input {
  flex: 1;
  padding: 0.8rem;
  border: 2px solid #e0e0e0;
  border-radius: 25px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.search-bar input:focus {
  border-color: #007bff;
  outline: none;
}

.search-bar button {
  padding: 0 1.5rem;
  border: none;
  border-radius: 25px;
  background: #007bff;
  color: white;
  cursor: pointer;
  transition: background 0.3s;
}

.search-bar button:hover {
  background: #0056b3;
}

.share-list {
  display: grid;
  gap: 1.5rem;
}

.share-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.share-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.1);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.user-avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  margin-right: 1rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.content-preview {
  color: #666;
  line-height: 1.6;
  margin: 0.8rem 0;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
  margin: 1rem 0;
  position: relative;
}

.preview-image {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.more-images {
  position: absolute;
  right: 0.5rem;
  bottom: 0.5rem;
  background: rgba(0,0,0,0.7);
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.9rem;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.action-bar button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  padding: 0.5rem;
  color: #666;
  cursor: pointer;
  transition: color 0.3s;
}

.liked {
  color: #ff4757 !important;
}

.views-count {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
}

.fab {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #007bff;
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  cursor: pointer;
  font-size: 1.25rem;
  transition: transform 0.3s;
}

.fab:hover {
  transform: scale(1.1);
}

.staggered-fade-move,
.staggered-fade-enter-active,
.staggered-fade-leave-active {
  transition: all 0.3s ease;
  position: absolute;
}

.staggered-fade-enter-from,
.staggered-fade-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.transition-wrapper {
  display: grid;
  gap: 1.5rem;
  position: relative;
}
</style>