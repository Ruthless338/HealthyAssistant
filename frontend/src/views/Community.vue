<!-- src/views/Community.vue -->
<template>
    <div class="community">
      <h1>运动社区</h1>
      
      <!-- 搜索框 -->
      <div class="search-container">
        <input type="text" v-model="searchKeyword" placeholder="搜索分享内容...">
        <button @click="searchShares">搜索</button>
      </div>
      
      <!-- 分享列表 -->
      <div class="shares-list">
        <div class="share-card" v-for="(share, index) in shares" :key="index">
          <div class="share-header">
            <img :src="share.author.avatar" :alt="share.author.username" class="avatar">
            <div class="author-info">
              <h3>{{ share.author.username }}</h3>
              <p>{{ formatDate(share.createdAt) }}</p>
            </div>
          </div>
          <div class="share-content">
            <h4>{{ share.title }}</h4>
            <p>{{ share.content }}</p>
            <div class="share-images">
              <img v-for="(image, imgIndex) in share.images" :key="imgIndex" :src="image" :alt="share.title" class="share-image">
            </div>
          </div>
          <div class="share-footer">
            <button @click="likeShare(share.id)">
              <i class="fas fa-heart"></i>
              <span>{{ share.likes }}</span>
            </button>
            <button @click="viewShare(share.id)">
              <i class="fas fa-eye"></i>
              <span>{{ share.views }}</span>
            </button>
            <button v-if="share.author.id === currentUser.id" @click="editShare(share.id)">
              <i class="fas fa-edit"></i>
            </button>
            <button v-if="share.author.id === currentUser.id" @click="deleteShare(share.id)">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </div>
      </div>
      
      <!-- 分享表单 -->
      <div class="share-form" v-if="showShareForm">
        <h2>创建分享</h2>
        <input type="text" v-model="newShare.title" placeholder="标题">
        <textarea v-model="newShare.content" placeholder="分享内容..."></textarea>
        <div class="image-upload">
          <label for="images">上传图片:</label>
          <input type="file" id="images" multiple @change="handleImageUpload">
        </div>
        <button @click="createShare">发布</button>
        <button @click="cancelShare">取消</button>
      </div>
      
      <!-- 添加分享按钮 -->
      <button class="add-share-btn" @click="showShareForm = !showShareForm">
        <i class="fas fa-plus"></i>
      </button>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    name: 'Community',
    data() {
      return {
        shares: [],
        searchKeyword: '',
        currentUser: {
          id: 1,
          username: 'current_user'
        },
        showShareForm: false,
        newShare: {
          title: '',
          content: '',
          images: []
        }
      };
    },
    methods: {
      async fetchShares() {
        try {
          const response = await axios.get('/api/shares');
          this.shares = response.data;
        } catch (error) {
          console.error('获取分享失败:', error);
        }
      },
      async searchShares() {
        try {
          const response = await axios.get(`/api/shares/search?keyword=${this.searchKeyword}`);
          this.shares = response.data;
        } catch (error) {
          console.error('搜索分享失败:', error);
        }
      },
      async likeShare(shareId) {
        try {
          await axios.post(`/api/shares/${shareId}/like`);
          // 更新点赞数
          const shareIndex = this.shares.findIndex(share => share.id === shareId);
          if (shareIndex !== -1) {
            this.shares[shareIndex].likes++;
          }
        } catch (error) {
          console.error('点赞失败:', error);
        }
      },
      async viewShare(shareId) {
        // 这里可以实现查看分享详情的逻辑
      },
      editShare(shareId) {
        // 这里可以实现编辑分享的逻辑
      },
      async deleteShare(shareId) {
        try {
          await axios.delete(`/api/shares/${shareId}`);
          // 从列表中移除分享
          this.shares = this.shares.filter(share => share.id !== shareId);
        } catch (error) {
          console.error('删除分享失败:', error);
        }
      },
      handleImageUpload(event) {
        const files = event.target.files;
        for (let i = 0; i < files.length; i++) {
          const file = files[i];
          // 这里可以添加图片预览和上传逻辑
          this.newShare.images.push(URL.createObjectURL(file));
        }
      },
      async createShare() {
        try {
          const formData = new FormData();
          formData.append('title', this.newShare.title);
          formData.append('content', this.newShare.content);
          // 添加图片文件
          const imageFiles = document.getElementById('images').files;
          for (let i = 0; i < imageFiles.length; i++) {
            formData.append('images', imageFiles[i]);
          }
          await axios.post('/api/shares', formData);
          // 重新获取分享列表
          this.fetchShares();
          // 重置表单
          this.newShare = {
            title: '',
            content: '',
            images: []
          };
          this.showShareForm = false;
        } catch (error) {
          console.error('创建分享失败:', error);
        }
      },
      cancelShare() {
        this.newShare = {
          title: '',
          content: '',
          images: []
        };
        this.showShareForm = false;
      },
      formatDate(dateString) {
        const date = new Date(dateString);
        return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
      }
    },
    mounted() {
      this.fetchShares();
    }
  };
  </script>
  
  <style scoped>
  .community {
    max-width: 1000px;
    margin: 0 auto;
    padding: 20px;
  }
  
  .search-container {
    display: flex;
    margin-bottom: 20px;
  }
  
  .search-container input {
    flex: 1;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px 0 0 4px;
  }
  
  .search-container button {
    padding: 10px 15px;
    background-color: #4CAF50;
    color: white;
    border: none;
    border-radius: 0 4px 4px 0;
    cursor: pointer;
  }
  
  .shares-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  
  .share-card {
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    padding: 15px;
    background-color: white;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }
  
  .share-header {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }
  
  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    margin-right: 10px;
  }
  
  .author-info h3 {
    margin: 0;
    font-size: 16px;
  }
  
  .author-info p {
    margin: 0;
    font-size: 12px;
    color: #666;
  }
  
  .share-content h4 {
    margin-top: 0;
    margin-bottom: 10px;
  }
  
  .share-images {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 15px;
  }
  
  .share-image {
    width: calc(33.33% - 10px);
    height: 150px;
    object-fit: cover;
    border-radius: 4px;
  }
  
  .share-footer {
    display: flex;
    gap: 15px;
  }
  
  .share-footer button {
    background: none;
    border: none;
    color: #666;
    cursor: pointer;
    display: flex;
    align-items: center;
  }
  
  .share-footer button i {
    margin-right: 5px;
  }
  
  .share-form {
    margin-top: 30px;
    padding: 20px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    background-color: white;
  }
  
  .share-form h2 {
    margin-top: 0;
  }
  
  .share-form input,
  .share-form textarea {
    width: 100%;
    padding: 10px;
    margin-bottom: 15px;
    border: 1px solid #ddd;
    border-radius: 4px;
  }
  
  .share-form textarea {
    min-height: 150px;
  }
  
  .image-upload {
    margin-bottom: 15px;
  }
  
  .add-share-btn {
    position: fixed;
    bottom: 30px;
    right: 30px;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background-color: #4CAF50;
    color: white;
    border: none;
    font-size: 24px;
    cursor: pointer;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  }
  </style>