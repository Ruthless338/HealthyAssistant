<template>
  <div class="share-detail-overlay" @click.self="close">
    <div class="share-detail-container">
      <button class="close-btn" @click="close">&times;</button>
      
      <div class="user-info">
        <img :src="share.author.avatar || '/default-avatar.png'" class="user-avatar">
        <div>
          <h3>{{ share.author.username }}</h3>
          <time>{{ formatTime(share.createdAt) }}</time>
        </div>
      </div>

      <div class="content">
        <h2>{{ share.title }}</h2>
        <p>{{ share.content }}</p>
        <div class="image-gallery">
          <img 
            v-for="(img, idx) in share.images"
            :key="idx"
            :src="img"
            @click="openLightbox(img)"
          >
        </div>
      </div>

      <div class="stats">
        <button 
          @click="toggleLike"
          :class="{ liked: share.isLiked }"
        >
          <i class="fas fa-heart"></i>
          {{ share.likes }}
        </button>
        <span>
          <i class="fas fa-eye"></i>
          {{ share.views }}
        </span>
      </div>

      <!-- 评论区（待实现） -->
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { format } from 'date-fns'

export default {
  props: ['shareId'],
  data() {
    return {
      share: {},
      loading: true
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
          isLiked: data.isLiked // 需要后端添加这个字段
        }
      } catch (error) {
        console.error('获取详情失败:', error)
      }
    },

    async incrementViews() {
      await axios.post(`http://localhost:8000/api/shares/${this.shareId}/view`)
    },

    async toggleLike() {
      try {
        const endpoint = this.share.isLiked ? 'unlike' : 'like'
        const { data } = await axios.post(
          `http://localhost:8000/api/shares/${this.shareId}/${endpoint}`
        )
        this.share.likes = data.likes
        this.share.isLiked = !this.share.isLiked
      } catch (error) {
        console.error('操作失败:', error)
      }
    },

    formatTime(date) {
      return format(new Date(date), 'yyyy-MM-dd HH:mm')
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
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.share-detail-container {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  font-size: 1.5rem;
  background: none;
  border: none;
  cursor: pointer;
}

.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin: 1rem 0;
}

.image-gallery img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.image-gallery img:hover {
  transform: scale(1.03);
}

.stats {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 1rem 0;
  border-top: 1px solid #eee;
}

.stats button {
  background: none;
  border: none;
  cursor: pointer;
  transition: color 0.2s;
}

.liked {
  color: #ff4757;
}
</style>