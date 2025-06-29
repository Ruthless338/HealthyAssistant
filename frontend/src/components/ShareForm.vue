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

        <!-- 新增：运动类型选择 -->
        <div class="input-group">
          <label class="input-label">运动类型</label>
          <select v-model="form.sportType" class="form-select">
            <option value="">请选择运动类型</option>
            <option value="跑步">跑步</option>
            <option value="健身">健身</option>
            <option value="瑜伽">瑜伽</option>
            <option value="游泳">游泳</option>
            <option value="骑行">骑行</option>
            <option value="跳绳">跳绳</option>
            <option value="跳舞">跳舞</option>
            <option value="拳击">拳击</option>
            <option value="徒手训练">徒手训练</option>
            <option value="其他">其他</option>
          </select>
        </div>

        <!-- 新增：难度等级选择 -->
        <div class="input-group">
          <label class="input-label">难度等级</label>
          <select v-model="form.difficultyLevel" class="form-select">
            <option value="">请选择难度等级</option>
            <option value="初级">初级</option>
            <option value="中级">中级</option>
            <option value="高级">高级</option>
          </select>
        </div>

        <!-- 新增：适用人群选择 -->
        <div class="input-group">
          <label class="input-label">适用人群</label>
          <select v-model="form.targetAudience" class="form-select">
            <option value="">请选择适用人群</option>
            <option value="全身减脂减重">全身减脂减重</option>
            <option value="局部变瘦更紧致">局部变瘦更紧致</option>
            <option value="增肌塑形提升线条">增肌塑形提升线条</option>
            <option value="提升运动能力/成绩">提升运动能力/成绩</option>
            <option value="保持健康">保持健康</option>
            <option value="康复/疼痛缓解">康复/疼痛缓解</option>
          </select>
        </div>

        <!-- 新增：标签输入 -->
        <div class="input-group">
          <label class="input-label">标签</label>
          <div class="tags-input-container">
            <div class="tags-display">
              <span 
                v-for="(tag, index) in form.tags" 
                :key="index" 
                class="tag-item"
              >
                {{ tag }}
                <button @click="removeTag(index)" class="remove-tag">×</button>
              </span>
            </div>
            <div class="tag-input-wrapper">
              <input 
                v-model="newTag" 
                @keyup.enter="addTag"
                placeholder="输入标签后按回车添加..."
                class="tag-input"
              >
              <button @click="addTag" class="add-tag-btn">添加</button>
            </div>
            <div class="tag-suggestions">
              <span class="suggestion-label">推荐标签：</span>
              <button 
                v-for="suggestion in tagSuggestions" 
                :key="suggestion"
                @click="addSuggestedTag(suggestion)"
                class="suggestion-tag"
              >
                {{ suggestion }}
              </button>
            </div>
          </div>
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
        images: [],
        tags: [],
        sportType: '',
        difficultyLevel: '',
        targetAudience: ''
      },
      newTag: '',
      isSubmitting: false,
      hoverIndex: -1,
      tagSuggestions: [
        '减脂', '增肌', '塑形', '健康', '康复', '力量训练', 
        '有氧运动', '拉伸', '核心训练', 'HIIT', '瑜伽', 
        '跑步技巧', '健身器材', '营养', '休息恢复'
      ]
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
    addTag() {
      if (this.newTag.trim() && !this.form.tags.includes(this.newTag.trim())) {
        this.form.tags.push(this.newTag.trim());
        this.newTag = '';
      }
    },
    removeTag(index) {
      this.form.tags.splice(index, 1);
    },
    addSuggestedTag(tag) {
      if (!this.form.tags.includes(tag)) {
        this.form.tags.push(tag);
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
      this.form = { 
        title: '', 
        content: '', 
        images: [], 
        tags: [], 
        sportType: '', 
        difficultyLevel: '', 
        targetAudience: '' 
      };
      this.newTag = '';
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

.form-input, .form-textarea, .form-select {
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

.form-input:focus, .form-textarea:focus, .form-select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
  font-family: inherit;
}

/* 标签输入样式 */
.tags-input-container {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  min-height: 2.5rem;
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: var(--bg-primary);
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.75rem;
  background: var(--primary-color);
  color: white;
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  font-weight: 500;
}

.remove-tag {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.remove-tag:hover {
  background: rgba(255, 255, 255, 0.2);
}

.tag-input-wrapper {
  display: flex;
  gap: 0.5rem;
}

.tag-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.add-tag-btn {
  padding: 0.75rem 1rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-tag-btn:hover {
  background: var(--primary-dark);
  transform: translateY(-1px);
}

.tag-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.suggestion-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.suggestion-tag {
  padding: 0.25rem 0.75rem;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.suggestion-tag:hover {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  transform: translateY(-1px);
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
  
  .tag-input-wrapper {
    flex-direction: column;
  }
  
  .add-tag-btn {
    width: 100%;
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