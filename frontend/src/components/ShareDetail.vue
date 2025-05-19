<template>
  <div class="share-detail-overlay" @click.self="close">
    <div class="share-detail-container">
      <button class="close-btn" @click="close">&times;</button>
      
      <div class="user-info">
        <img :src="share.authorAvatar" class="user-avatar">
        <div>
          <h3>{{ share.authorName }}</h3>
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
            :src="'http://localhost:8000/uploads/'+img"
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

    </div>
  </div>
  <transition name="fade">
    <div v-if="lightboxVisible" class="lightbox" @click="lightboxVisible = false">
      <img :src="selectedImage" class="lightbox-image">
    </div>
  </transition>
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
      try {
        await axios.get(`http://localhost:8000/api/shares/${this.shareId}/view`)
        
      } catch (error) {
        console.error('增加浏览量失败:', error)
      }
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
  border-radius: 24px;
  padding: 3rem;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 16px 48px rgba(0,0,0,0.2);
}

.content h2 {
  font-size: 2rem;
  margin: 1.5rem 0;
  color: #2c3e50;
}

.content p {
  font-size: 1.1rem;
  line-height: 1.8; /* 增加行高 */
  color: #444;
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
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin: 1rem 0;
}

.image-gallery img {
  width: 100%;
  height: 250px;
  object-fit: cover;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.3s;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
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

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.user-avatar {
  width: 65px;
  height: 65px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #007bff;
}

.user-info div {
  flex: 1;
}

.user-info h3 {
  margin: 0;
  font-size: 1.4rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

/* 灯箱样式 */
.lightbox {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.lightbox-image {
  max-width: 90%;
  max-height: 90vh;
  border-radius: 8px;
  box-shadow: 0 16px 48px rgba(0,0,0,0.3);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>