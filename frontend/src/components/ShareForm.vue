<template>
  <div class="share-form-overlay" @click.self="close">
    <div class="share-form">
      <h2>创建分享</h2>
      <input v-model="form.title" placeholder="标题" required>
      <textarea v-model="form.content" placeholder="分享你的运动心得..." required></textarea>
      
      <ImageUpload 
        @uploaded="handleImageUpload"
        @clear="clearImages"
        class="uploader"
      />
      
      <div class="preview-grid">
        <div 
          v-for="(img, index) in form.images" 
          :key="index" 
          class="preview-item"
          @mouseenter="hoverIndex = index"
          @mouseleave="hoverIndex = -1"
        >
          <img :src="img" alt="预览">
          <button 
            v-show="hoverIndex === index"
            @click="removeImage(index)"
            class="delete-btn"
          >
            ×
          </button>
        </div>
      </div>
      
      <div class="actions">
        <button 
          @click="submit" 
          class="primary"
          :disabled="isSubmitting"
        >
          {{ isSubmitting ? '发布中...' : '发布' }}
        </button>
        <button @click="close" class="cancel-btn">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
import ImageUpload from './ImageUpload.vue';
import axios from 'axios';

export default {
  name: "ShareForm",
  components: { ImageUpload },
  emits: ['close', 'created'],
  data() {
    return {
      form: {
        title: '',
        content: '',
        images: []
      },
      isSubmitting: false,
      hoverIndex: -1
    }
  },
  methods: {
    handleImageUpload(url) {
      this.form.images.push(url);
    },
    async removeImage(index) {
      try {
        // await axios.delete(`/api/files/delete?url=${encodeURIComponent(this.form.images[index])}`);
        this.form.images.splice(index, 1);
      } catch (error) {
        alert('图片删除失败');
      }
    },
    async submit() {
      if (!this.form.title.trim() || !this.form.content.trim()) {
        alert('标题和内容不能为空');
        return;
      }
      
      this.isSubmitting = true;
      try {
        // console.log("提交用户ID:",this.$store.state.user.id);
        // console.log("提交数据:",this.form);
        await axios.post(`http://localhost:8000/api/shares/create?userId=${this.$store.state.user.id}`, this.form);
        alert('分享成功！');
        this.$emit('created');
        this.close();
      } catch (error) {
        alert('发布失败: ' + error.message);
      } finally {
        this.isSubmitting = false;
      }
    },
    close() {
      this.form = { title: '', content: '', images: [] };
      this.$emit('close');
    }
  }
}
</script>

<style scoped>
/* 优化后的样式 */
.share-form {
  background: #fff;
  padding: 2rem;
  border-radius: 16px;
  width: 600px;
  max-width: 90%;
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}

input, textarea {
  width: 100%;
  padding: 12px;
  margin-bottom: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus, textarea:focus {
  border-color: #007bff;
  outline: none;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin: 1rem 0;
}

.preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}

.preview-item:hover {
  transform: scale(1.02);
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-weight: bold;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 1rem;
}

.primary {
  background: #007bff;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}

.primary:hover {
  background: #0056b3;
}

.primary:disabled {
  background: #94c3ff;
  cursor: not-allowed;
}

.cancel-btn {
  background: #f0f0f0;
  color: #666;
  padding: 12px 24px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}

.cancel-btn:hover {
  background: #e0e0e0;
}
</style>