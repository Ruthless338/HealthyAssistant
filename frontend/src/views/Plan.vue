<!-- src/views/Plan.vue -->
<template>
  <div class="plan-container">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="avatar-section">
        <img :src="user.avatar || defaultAvatar" class="user-avatar" />
        <div class="user-meta">
          <h2 class="username">{{ user.username }}的智能运动计划</h2>
          <div class="tag-cloud">
            <span 
              v-for="(tag, index) in userTags"
              :key="index"
              class="tag"
              :style="tagStyle(index)"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
      <div class="health-stats">
        <div class="stat-item">
          <span class="stat-icon">🏋️</span>
          <div class="stat-content">
            <div class="stat-value">{{ user.height }}cm / {{ user.weight }}kg</div>
            <div class="stat-title">身体数据</div>
          </div>
        </div>
        <div class="stat-item">
          <span class="stat-icon">📊</span>
          <div class="stat-content">
            <div class="stat-value">{{ bmi }}</div>
            <div class="stat-title">BMI指数 ({{ bmiStatus }})</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loader">
        <div class="spinner"></div>
        <p class="loading-text">AI教练正在定制您的计划...</p>
      </div>
    </div>

    <!-- 周计划展示 -->
    <div v-if="exercisePlan" class="week-plan">
      <div 
        v-for="(day, dayIndex) in exercisePlan.weekPlan"
        :key="dayIndex"
        class="day-card"
      >
        <div class="day-header">
          <h3 class="day-title">
            <span class="day-number">第 {{ dayIndex + 1 }} 天</span>
            <span class="duration">预计时长 {{ calculateDuration(day) }}</span>
          </h3>
        </div>
        
        <div class="exercise-grid">
          <div
            v-for="(exercise, exIndex) in day.exercises"
            :key="exIndex"
            class="exercise-card"
            @mouseenter="activeExercise = `${dayIndex}-${exIndex}`"
          >
            <div class="card-inner" :class="{ active: activeExercise === `${dayIndex}-${exIndex}` }">
              <!-- 图片展示层 -->
              <div class="media-wrapper">
                <div class="image-frame">
                  <img
                    :src="exercise.image"
                    :alt="exercise.name"
                    class="exercise-image"
                    @error="handleImageError"
                  />
                  <div class="hover-overlay">
                    <button class="demo-button" @click="showVideoDemo(exercise)">
                      <i class="icon-play"></i>
                      <span>动作示范</span>
                    </button>
                  </div>
                </div>
                <div class="exercise-badge">
                  <span class="badge difficulty" :class="difficultyClass(exercise.difficulty)">
                    难度：{{ difficultyText(exercise.difficulty) }}
                  </span>
                </div>
              </div>

              <!-- 详细信息层 -->
              <div class="content-wrapper">
                <h4 class="exercise-name">{{ exercise.name }}</h4>
                
                <div class="specs">
                  <div class="spec-item">
                    <i class="icon-repeats">🔄</i>
                    <div class="spec-content">
                      <span class="value">{{ exercise.sets }} 组 × {{ exercise.reps }} 次</span>
                      <span class="label">组间休息 {{ exercise.interval }}</span>
                    </div>
                  </div>
                  
                  <div class="spec-item">
                    <i class="icon-target">🎯</i>
                    <div class="spec-content">
                      <span class="value">{{ exercise.targetArea?.join(' / ') || '全身' }}</span>
                      <span class="label">主要训练部位</span>
                    </div>
                  </div>
                </div>

                <div class="exercise-desc">
                  <div class="desc-header">
                    <i class="icon-info">ℹ️</i>
                    <span>动作要点</span>
                  </div>
                  <p class="desc-content">{{ exercise.description || defaultTips }}</p>
                </div>

                <div class="action-bar">
                  <button class="action-btn reminder" @click="setReminder(exercise)">
                    <i class="icon-alarm">⏰</i>训练提醒
                  </button>
                  <button class="action-btn favorite" @click="toggleFavorite(exercise)">
                    <i :class="['icon-heart', { filled: exercise.favorited }]">❤️</i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误处理 -->
    <div v-if="error" class="error-notification">
      <div class="error-message">{{ error }}</div>
      <button class="retry-button" @click="fetchExercisePlan">重试</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import axios from 'axios'

const store = useStore()
const user = computed(() => store.state.user)

// 响应式数据
const exercisePlan = ref(null)
const error = ref(null)
const loading = ref(false)
const activeExercise = ref(null)
const defaultTips = ref('保持核心收紧，注意呼吸节奏')
const defaultExerciseImage = 'https://pic1.imgdb.cn/item/68161c4e58cb8da5c8d9ea4f.png'

// 计算属性
const userTags = computed(() => [
  ...(user.value.interest || []),
  ...(user.value.goal || []),
  ...(user.value.part || [])
])

const bmi = computed(() => {
  const height = user.value.height / 100
  return (user.value.weight / (height * height)).toFixed(1)
})

const bmiStatus = computed(() => {
  const value = parseFloat(bmi.value)
  if (value < 18.5) return '偏轻'
  if (value < 24) return '正常'
  if (value < 28) return '过重'
  return '肥胖'
})

// 生命周期钩子
onMounted(async () => {
  loading.value = true
  try {
    await fetchExercisePlan()
  } catch (err) {
    error.value = '计划生成失败，请稍后重试'
  } finally {
    loading.value = false
  }
})

// 方法定义
const fetchExercisePlan = async () => {
  try {
    const response = await axios.post('http://localhost:8000/api/plan/generate', {
       id: user.value.id 
    });
    exercisePlan.value = processPlan(response.data)
  } catch (err) {
    error.value = err.response?.data?.message || '网络连接异常'
  }
}

const processPlan = (plan) => ({
  weekPlan: plan.WeekPlan.map((day, dayIndex) => ({
    dayNumber: dayIndex + 1,
    exercises: day.exercises.map(ex => ({
      ...ex,
      sets: ex.group || 3,
      reps: ex.reps || 12,
      interval: ex.interval ? `${ex.interval}秒` : '60秒',
      difficulty: ex.difficulty || 3,
      targetArea: ex.parts || ['全身'],
      // image: ex.image || defaultExerciseImage,
      iamge: defaultExerciseImage,
      favorited: false
    }))
  }))
})

const calculateDuration = (day) => {
  const total = day.exercises.reduce((sum, ex) => sum + (ex.sets * (ex.reps * 3 + parseInt(ex.interval))) || 0, 0)
  return `${Math.ceil(total / 60)}分钟`
}

const difficultyClass = (level) => `difficulty-${Math.min(5, Math.max(1, level))}`
const difficultyText = (level) => ['入门', '初级', '中级', '进阶', '专业'][Math.min(4, Math.max(0, level - 1))]

const tagStyle = (index) => {
  const colors = ['#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', '#ffeead']
  return {
    backgroundColor: colors[index % colors.length] + '20',
    color: colors[index % colors.length]
  }
}

const handleImageError = (event) => {
  event.target.src = defaultExerciseImage
}

const showVideoDemo = (exercise) => {
  console.log('展示动作示范：', exercise.name)
}

const setReminder = (exercise) => {
  console.log('设置提醒：', exercise.name)
}

const toggleFavorite = (exercise) => {
  exercise.favorited = !exercise.favorited
}
</script>

<style scoped>
.plan-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.user-card {
  background: white;
  border-radius: 1.5rem;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.username {
  margin: 0;
  font-size: 1.5rem;
  color: #2c3e50;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 1rem;
}

.tag {
  padding: 0.5rem 1rem;
  border-radius: 1rem;
  font-size: 0.9em;
  font-weight: 500;
}

.health-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1.5rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 1rem;
}

.stat-icon {
  font-size: 1.5rem;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-weight: 600;
  color: #2c3e50;
}

.stat-title {
  font-size: 0.9em;
  color: #666;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255,255,255,0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #42b983;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 1rem;
  color: #666;
  text-align: center;
}

.day-card {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.day-header {
  margin-bottom: 1.5rem;
}

.day-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0;
  color: #2c3e50;
}

.duration {
  font-size: 0.9em;
  color: #666;
}

.exercise-grid {
  display: grid;
  gap: 1.5rem;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.exercise-card {
  background: white;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.exercise-card:hover {
  transform: translateY(-5px);
}

.media-wrapper {
  position: relative;
}

.exercise-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.exercise-card:hover .hover-overlay {
  opacity: 1;
}

.demo-button {
  padding: 0.8rem 1.5rem;
  background: rgba(255,255,255,0.9);
  border: none;
  border-radius: 2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: background 0.2s;
}

.demo-button:hover {
  background: white;
}

.content-wrapper {
  padding: 1.5rem;
}

.exercise-name {
  margin: 0 0 1rem;
  color: #2c3e50;
}

.specs {
  display: grid;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.spec-item {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.spec-content {
  flex: 1;
}

.value {
  font-weight: 500;
  color: #2c3e50;
}

.label {
  font-size: 0.9em;
  color: #666;
}

.exercise-desc {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 0.8rem;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  margin-top: 1.5rem;
}

.action-btn {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.reminder {
  background: #e3f2fd;
  color: #2196f3;
}

.favorite {
  background: transparent;
}

.error-notification {
  padding: 1.5rem;
  background: #ffeeee;
  border-radius: 0.8rem;
  color: #ff4444;
  text-align: center;
}

.retry-button {
  margin-top: 1rem;
  padding: 0.8rem 1.5rem;
  background: #42b983;
  color: white;
  border: none;
  border-radius: 2rem;
  cursor: pointer;
}

.difficulty-1 { color: #4caf50; background: #e8f5e9; }
.difficulty-2 { color: #ff9800; background: #fff3e0; }
.difficulty-3 { color: #f44336; background: #ffebee; }
</style>