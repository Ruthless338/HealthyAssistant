<!-- src/views/Selector.vue -->
<template>
  <div class="selector-container">
    <div class="form-wrapper glass">
      <div class="form-header">
        <h1 class="form-title gradient-text">🏋️‍♀️ 运动Selector</h1>
        <p class="form-subtitle">完善你的个人档案，获得个性化运动计划</p>
      </div>
      
      <form @submit.prevent="submitForm" class="user-form">
        <!-- 头像上传区域 -->
        <div class="form-section">
          <h2 class="section-title">📸 头像设置</h2>
          <div class="avatar-uploader">
            <ImageUpload @uploaded="handleAvatarUpload" />
            <p class="upload-tip">支持JPG/PNG格式，大小不超过5MB</p>
          </div>
        </div>
        
        <!-- 基本信息区 -->
        <div class="form-section">
          <h2 class="section-title">👤 基本信息</h2>
          <div class="grid-group">
            <div class="input-group">
              <label class="input-label">性别</label>
              <div class="gender-select">
                <button 
                  v-for="opt in genderOptions" 
                  :key="opt.value"
                  :class="['gender-btn', { active: form.gender === opt.value }]"
                  @click.prevent="form.gender = opt.value"
                >
                  <span class="gender-icon">{{ opt.value === 'male' ? '👨' : '👩' }}</span>
                  {{ opt.label }}
                </button>
              </div>
            </div>
            <div class="input-group">
              <label class="input-label">年龄</label>
              <input 
                v-model.number="form.age" 
                type="number" 
                min="10" 
                max="100"
                placeholder="请输入年龄"
                class="form-input"
              >
            </div>
            <div class="input-group">
              <label class="input-label">身高 (cm)</label>
              <input
                v-model.number="form.height"
                type="number"
                min="100"
                max="250"
                placeholder="请输入身高"
                class="form-input"
              >
            </div>
            <div class="input-group">
              <label class="input-label">体重 (kg)</label>
              <input
                v-model.number="form.weight"
                type="number"
                min="30"
                max="200"
                placeholder="请输入体重"
                class="form-input"
              >
            </div>
          </div>
        </div>
  
        <!-- 多选卡片区 -->
        <div class="form-section">
          <h2 class="section-title">🎯 健身目标</h2>
          <div class="card-grid">
            <div 
              v-for="goal in goalOptions"
              :key="goal"
              class="selection-card"
              :class="{ selected: form.goal.includes(goal) }"
              @click="toggleSelection('goal', goal)"
            >
              <span class="card-icon">🎯</span>
              <span class="card-text">{{ goal }}</span>
            </div>
          </div>
        </div>
  
        <div class="form-section">
          <h2 class="section-title">❤️ 运动兴趣</h2>
          <div class="card-grid">
            <div
              v-for="interest in interestOptions"
              :key="interest"
              class="selection-card"
              :class="{ selected: form.interest.includes(interest) }"
              @click="toggleSelection('interest', interest)"
            >
              <span class="card-icon">⚡</span>
              <span class="card-text">{{ interest }}</span>
            </div>
          </div>
        </div>
  
        <div class="form-section">
          <h2 class="section-title">💪 重点锻炼部位</h2>
          <div class="card-grid">
            <div
              v-for="part in partOptions"
              :key="part"
              class="selection-card"
              :class="{ selected: form.part.includes(part) }"
              @click="toggleSelection('part', part)"
            >
              <span class="card-icon">💪</span>
              <span class="card-text">{{ part }}</span>
            </div>
          </div>
        </div>
  
        <!-- 提交按钮 -->
        <div class="submit-section">
          <button 
            type="submit" 
            class="submit-btn"
            :disabled="isSubmitting"
          >
            <span v-if="!isSubmitting" class="btn-content">
              <span class="btn-icon">🚀</span>
              提交
            </span>
            <span v-else class="btn-content">
              <span class="loading-spinner"></span>
              提交中...
            </span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
  
<script>
import { ref, reactive } from 'vue';
import ImageUpload from '../components/ImageUpload.vue';
import axios from 'axios';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';

export default {
  name: 'Selector',
  components: {
    ImageUpload
  },
  setup() {
    const store = useStore();
    const router = useRouter();
    // 选项配置
    const genderOptions = [
      { label: '男性', value: 'male' },
      { label: '女性', value: 'female' }
    ]
    const form = reactive({
      gender: '',
      age: null,
      height: null,
      weight: null,
      goal: [],
      interest: [],
      part: [],
      avatar: ''
    })
    const handleAvatarUpload = (url) => {
      form.avatar = url
    }
    const isSubmitting = ref(false)
    const goalOptions = [
      '全身减脂减重',
      '局部变瘦更紧致',
      '增肌塑形提升线条',
      '提升运动能力/成绩',
      '保持健康',
      '康复/疼痛缓解'
    ]
    const interestOptions = [
      '跳舞', '跳绳', '跑步', '健身',
      '瑜伽', '游泳', '骑行', '拳击', '徒手训练'
    ]
    const partOptions = [
      '全身', '肩背', '腰腹', '臀部',
      '手臂', '腿部', '胸部'
    ]

    // 方法
    const submitForm = async () => {
      try {
        isSubmitting.value = true
        const { username, password } = store.state.user // 确保 store 中有 user 数据
        const formData = {
          username: username,
          password: password,
          gender: form.gender,
          age: form.age,
          height: form.height,
          weight: form.weight,
          goal: form.goal,
          interest: form.interest,
          part: form.part,
          // currentPlan: currentPlan,
          avatar: form.avatar
        }
        await axios.post('http://localhost:8000/api/auth/update', formData)
        alert('信息提交成功！')
        store.commit('setProfileModified', true);
        router.push({ name: 'Plan' });
      } catch (error) {
        alert('提交失败，请稍后重试')
        console.error('提交失败:', error)
      } finally {
        isSubmitting.value = false
      }
    }

    const toggleSelection = (field, value) => {
      const index = form[field].indexOf(value)
      if (index === -1) {
        form[field].push(value)
      } else {
        form[field].splice(index, 1)
      }
    }

    return {
      form,
      isSubmitting,
      genderOptions,
      goalOptions,
      interestOptions,
      partOptions,
      submitForm,
      toggleSelection,
      handleAvatarUpload
    }
  }
}
</script>
  
<style scoped>
.selector-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
  min-height: 100vh;
}

.form-wrapper {
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  padding: 2rem;
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.form-header {
  text-align: center;
  margin-bottom: 3rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid var(--border-color);
}

.form-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 1rem;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.form-subtitle {
  color: var(--text-secondary);
  font-size: 1.125rem;
  margin: 0;
}

.user-form {
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.section-title {
  color: var(--text-primary);
  margin: 0 0 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--primary-color);
  display: inline-block;
  font-size: 1.5rem;
  font-weight: 600;
}

.grid-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.input-label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 600;
  font-size: 0.875rem;
}

.form-input {
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

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.gender-select {
  display: flex;
  gap: 1rem;
}

.gender-btn {
  flex: 1;
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: var(--bg-primary);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-weight: 500;
}

.gender-btn:hover {
  background: var(--bg-secondary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.gender-btn.active {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.gender-icon {
  font-size: 1.25rem;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.selection-card {
  padding: 1.5rem;
  background: var(--bg-secondary);
  border: 2px solid transparent;
  border-radius: var(--radius-xl);
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  min-height: 120px;
  justify-content: center;
}

.selection-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.selection-card.selected {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border-color: var(--primary-color);
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.card-icon {
  font-size: 2rem;
}

.card-text {
  font-weight: 500;
  font-size: 0.875rem;
  line-height: 1.4;
}

.avatar-uploader {
  text-align: center;
  padding: 2rem;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-xl);
  background: var(--bg-secondary);
  transition: all 0.3s ease;
}

.avatar-uploader:hover {
  border-color: var(--primary-color);
  background: var(--bg-tertiary);
}

.upload-tip {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-top: 1rem;
}

.submit-section {
  text-align: center;
  padding-top: 2rem;
  border-top: 1px solid var(--border-color);
}

.submit-btn {
  width: 100%;
  max-width: 400px;
  padding: 1.25rem 2rem;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border: none;
  border-radius: var(--radius-xl);
  font-size: 1.125rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.btn-icon {
  font-size: 1.25rem;
}

.loading-spinner {
  width: 20px;
  height: 20px;
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
  .selector-container {
    padding: 1rem;
    margin: 1rem auto;
  }
  
  .form-wrapper {
    padding: 1.5rem;
  }
  
  .form-title {
    font-size: 2rem;
  }
  
  .grid-group {
    grid-template-columns: 1fr;
  }
  
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .gender-select {
    flex-direction: column;
  }
  
  .selection-card {
    min-height: 100px;
    padding: 1rem;
  }
  
  .card-icon {
    font-size: 1.5rem;
  }
  
  .card-text {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .form-wrapper {
    padding: 1rem;
  }
  
  .form-title {
    font-size: 1.75rem;
  }
  
  .card-grid {
    grid-template-columns: 1fr;
  }
  
  .selection-card {
    min-height: 80px;
    padding: 0.75rem;
  }
  
  .submit-btn {
    padding: 1rem 1.5rem;
    font-size: 1rem;
  }
}
</style>