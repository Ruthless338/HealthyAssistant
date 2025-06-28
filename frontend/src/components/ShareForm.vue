<template>
  <div class="share-form-overlay" @click.self="close">
    <div class="share-form glass">
      <div class="form-header">
        <h2 class="form-title gradient-text">创建分享</h2>
        <p class="form-subtitle">分享你的运动心得，激励更多人</p>
      </div>
      
      <div class="form-content">
        <div class="input-group">
          <label class="input-label">标题</label>
          <input 
            v-model="form.title" 
            placeholder="给你的分享起个标题..." 
            class="form-input"
            required
          >
        </div>
        
        <div class="input-group">
          <label class="input-label">内容</label>
          <textarea 
            v-model="form.content" 
            placeholder="分享你的运动心得、经验或感悟..." 
            class="form-textarea"
            required
          ></textarea>
        </div>
        
        <div class="upload-section">
          <label class="input-label">图片</label>
          <ImageUpload 
            @uploaded="handleImageUpload"
            @clear="clearImages"
            class="uploader"
          />
        </div>
        
        <div v-if="form.images.length" class="preview-section">
          <label class="input-label">图片预览</label>
          <div class="preview-grid">
            <div 
              v-for="(img, index) in form.images" 
              :key="index" 
              class="preview-item"
              @mouseenter="hoverIndex = index"
              @mouseleave="hoverIndex = -1"
            >
              <img :src="img" alt="预览" class="preview-image">
              <div class="preview-overlay" v-show="hoverIndex === index">
                <button 
                  @click="removeImage(index)"
                  class="delete-btn"
                >
                  <span class="delete-icon">✕</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="form-actions">
        <button 
          @click="submit" 
          class="submit-btn primary-btn"
          :disabled="isSubmitting"
        >
          <span v-if="!isSubmitting" class="btn-content">
            <span class="btn-icon">🚀</span>
            发布分享
          </span>
          <span v-else class="btn-content">
            <span class="loading-spinner"></span>
            发布中...
          </span>
        </button>
        <button @click="close" class="cancel-btn secondary-btn">
          <span class="btn-content">
            <span class="btn-icon">✕</span>
            取消
          </span>
        </button>
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
.share-form-overlay {
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
  z-index: 2000;
  padding: 1rem;
}

.share-form {
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  padding: 2rem;
  width: 600px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.form-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.5rem;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.form-subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
  margin: 0;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.input-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  font-size: 1rem;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
  font-family: inherit;
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.uploader {
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  text-align: center;
  transition: all 0.3s ease;
}

.uploader:hover {
  border-color: var(--primary-color);
  background: var(--bg-secondary);
}

.preview-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 1rem;
}

.preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 0.3s ease;
  border: 2px solid var(--border-color);
}

.preview-item:hover {
  transform: scale(1.02);
  box-shadow: var(--shadow-md);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.delete-btn {
  background: var(--error-color);
  color: white;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  transform: scale(1.1);
  box-shadow: var(--shadow-md);
}

.delete-icon {
  font-size: 1rem;
  font-weight: bold;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.primary-btn, .secondary-btn {
  padding: 0.75rem 1.5rem;
  border-radius: var(--radius-lg);
  border: none;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.875rem;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.primary-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.secondary-btn {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}

.secondary-btn:hover {
  background: var(--bg-tertiary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-icon {
  font-size: 1rem;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .share-form {
    padding: 1.5rem;
    width: 95%;
  }
  
  .form-title {
    font-size: 1.5rem;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .primary-btn, .secondary-btn {
    width: 100%;
    justify-content: center;
  }
  
  .preview-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 480px) {
  .share-form {
    padding: 1rem;
  }
  
  .form-title {
    font-size: 1.25rem;
  }
  
  .preview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>