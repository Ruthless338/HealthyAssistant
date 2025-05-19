<!-- src/views/Selector.vue -->
<template>
    <div class="container">
      <form @submit.prevent="submitForm" class="user-form">
        <h1>🏋️‍♀️ 健身Selector</h1>
        <!-- 新增头像上传区域 -->
        <div class="form-section">
          <h2>📸 头像设置</h2>
          <div class="avatar-uploader">
            <ImageUpload @uploaded="handleAvatarUpload" />
            <p class="upload-tip">支持JPG/PNG格式，大小不超过5MB</p>
          </div>
        </div>
        <!-- 基本信息区 -->
        <div class="form-section">
          <h2>👤 基本信息</h2>
          <div class="grid-group">
            <div class="input-group">
              <label>性别</label>
              <div class="gender-select">
                <button 
                  v-for="opt in genderOptions" 
                  :key="opt.value"
                  :class="{ active: form.gender === opt.value }"
                  @click.prevent="form.gender = opt.value"
                >
                  {{ opt.label }}
                </button>
              </div>
            </div>
            <div class="input-group">
              <label>年龄</label>
              <input 
                v-model.number="form.age" 
                type="number" 
                min="10" 
                max="100"
                placeholder="请输入年龄"
              >
            </div>
            <div class="input-group">
              <label>身高 (cm)</label>
              <input
                v-model.number="form.height"
                type="number"
                min="100"
                max="250"
                placeholder="请输入身高"
              >
            </div>
            <div class="input-group">
              <label>体重 (kg)</label>
              <input
                v-model.number="form.weight"
                type="number"
                min="30"
                max="200"
                placeholder="请输入体重"
              >
            </div>
          </div>
        </div>
  
        <!-- 多选卡片区 -->
        <div class="form-section">
          <h2>🎯 健身目标</h2>
          <div class="card-grid">
            <div 
              v-for="goal in goalOptions"
              :key="goal"
              class="selection-card"
              :class="{ selected: form.goal.includes(goal) }"
              @click="toggleSelection('goal', goal)"
            >
              {{ goal }}
            </div>
          </div>
        </div>
  
        <div class="form-section">
          <h2>❤️ 运动兴趣</h2>
          <div class="card-grid">
            <div
              v-for="interest in interestOptions"
              :key="interest"
              class="selection-card"
              :class="{ selected: form.interest.includes(interest) }"
              @click="toggleSelection('interest', interest)"
            >
              {{ interest }}
            </div>
          </div>
        </div>
  
        <div class="form-section">
          <h2>💪 重点锻炼部位</h2>
          <div class="card-grid">
            <div
              v-for="part in partOptions"
              :key="part"
              class="selection-card"
              :class="{ selected: form.part.includes(part) }"
              @click="toggleSelection('part', part)"
            >
              {{ part }}
            </div>
          </div>
        </div>
  
        <!-- 提交按钮 -->
        <button 
          type="submit" 
          class="submit-btn"
          :disabled="isSubmitting"
        >
          <span v-if="!isSubmitting">🚀 完成注册</span>
          <span v-else>提交中...</span>
        </button>
      </form>
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
  .container {
    max-width: 1200px;
    margin: 2rem auto;
    padding: 0 1rem;
  }
  
  .user-form {
    background: white;
    border-radius: 16px;
    padding: 2rem;
    box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  }
  
  .form-section {
    margin-bottom: 3rem;
  }
  
  h2 {
    color: #2c3e50;
    margin-bottom: 1.5rem;
    padding-bottom: 0.5rem;
    border-bottom: 2px solid #4A90E2;
    display: inline-block;
  }
  
  .grid-group {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
  }
  
  .input-group {
    margin-bottom: 1rem;
  }
  
  label {
    display: block;
    margin-bottom: 0.5rem;
    color: #666;
    font-weight: 500;
  }
  
  input {
    width: 100%;
    padding: 0.8rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    transition: border-color 0.3s;
  }
  
  input:focus {
    outline: none;
    border-color: #4A90E2;
    box-shadow: 0 0 0 2px rgba(74,144,226,0.2);
  }
  
  .gender-select {
    display: flex;
    gap: 1rem;
  }
  
  .gender-select button {
    flex: 1;
    padding: 0.8rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    background: #f8f9fa;
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .gender-select button.active {
    background: #4A90E2;
    color: white;
    border-color: #4A90E2;
  }
  
  .card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 1rem;
  }
  
  .selection-card {
    padding: 1.2rem;
    background: #f8f9fa;
    border: 2px solid transparent;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .selection-card:hover {
    transform: translateY(-3px);
  }
  
  .selection-card.selected {
    background: #4A90E2;
    color: white;
    border-color: #357ABD;
    box-shadow: 0 4px 6px rgba(74,144,226,0.2);
  }
  
  .submit-btn {
    width: 100%;
    padding: 1.2rem;
    background: #4A90E2;
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 1.1rem;
    cursor: pointer;
    transition: opacity 0.2s;
  }
  
  .submit-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
  
  @media (max-width: 768px) {
    .container {
      padding: 1rem;
    }
    
    .card-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .avatar-uploader {
    text-align: center;
    margin-bottom: 2rem;
  }

  .upload-tip {
    color: #666;
    font-size: 0.9rem;
    margin-top: 0.5rem;
  }
  </style>