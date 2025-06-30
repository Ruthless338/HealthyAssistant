<!-- src/views/Plan.vue -->
<template>
  <div class="plan-container">
    <!-- 用户信息卡片 -->
    <div class="user-card glass">
      <div class="avatar-section">
        <div class="avatar-wrapper">
        <img :src="'http://localhost:8000/uploads/'+user.avatar || defaultAvatar" class="user-avatar" />
          <div class="avatar-ring"></div>
        </div>
        <div class="user-meta">
          <h2 class="username gradient-text">{{ user.username }}的智能运动计划</h2>
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
        <div class="stat-item glass">
          <div class="stat-icon-wrapper">
          <span class="stat-icon">🏋️</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ user.height }}cm / {{ user.weight }}kg</div>
            <div class="stat-title">身体数据</div>
          </div>
        </div>
        <div class="stat-item glass">
          <div class="stat-icon-wrapper">
          <span class="stat-icon">📊</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ bmi }}</div>
            <div class="stat-title">BMI指数 ({{ bmiStatus }})</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loader glass">
        <div class="spinner"></div>
        <p class="loading-text">AI教练正在定制您的计划...(可能需要几分钟，请耐心等待；也可先浏览其他功能)</p>
        <div class="loading-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>

    <!-- 周计划展示 -->
    <div v-if="exercisePlan" class="week-plan">
      <div 
        v-for="(day, dayIndex) in exercisePlan.weekPlan"
        :key="dayIndex"
        class="day-card glass"
      >
        <div class="day-header">
          <div class="day-info">
          <h3 class="day-title">
            <span class="day-number">第 {{ dayIndex + 1 }} 天</span>
              <span class="day-badge">Day {{ dayIndex + 1 }}</span>
            </h3>
            <div class="duration-info">
              <span class="duration-icon">⏱️</span>
            <span class="duration">预计时长 {{ calculateDuration(day) }}</span>
            </div>
          </div>
          <div class="day-progress">
            <div class="progress-ring">
              <svg width="60" height="60">
                <circle cx="30" cy="30" r="25" fill="none" stroke="rgba(255,255,255,0.2)" stroke-width="4"/>
                <circle cx="30" cy="30" r="25" fill="none" stroke="var(--primary-color)" stroke-width="4" 
                        stroke-dasharray="157" stroke-dashoffset="157" stroke-linecap="round"/>
              </svg>
              <span class="progress-text">0%</span>
            </div>
          </div>
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
                    :src="'http://localhost:8000'+exercise.image || defaultExerciseImage"
                    :alt="exercise.name"
                    class="exercise-image"
                    @error="handleImageError"
                  />
                  <div class="image-overlay">
                    <div class="overlay-content">
                      <button class="demo-button" @click="showVideoDemo(exercise, dayIndex, exIndex)">
                        <span class="play-icon">▶️</span>
                      <span>动作示范</span>
                    </button>
                    </div>
                  </div>
                </div>
                <div class="exercise-badge">
                  <span class="badge difficulty" :class="difficultyClass(exercise.difficulty)">
                    <span class="badge-icon">⚡</span>
                    难度：{{ difficultyText(exercise.difficulty) }}
                  </span>
                </div>
              </div>

              <!-- 详细信息层 -->
              <div class="content-wrapper">
                <h4 class="exercise-name">{{ exercise.name }}</h4>
                
                <div class="specs">
                  <div class="spec-item">
                    <div class="spec-icon">🔄</div>
                    <div class="spec-content">
                      <span class="value">{{ exercise.sets }} 组 × {{ exercise.reps }} 次</span>
                      <span class="label">组间休息 {{ exercise.interval }}</span>
                    </div>
                  </div>
                  
                  <div class="spec-item">
                    <div class="spec-icon">🎯</div>
                    <div class="spec-content">
                      <span class="value">{{ exercise.targetArea?.join(' / ') || '全身' }}</span>
                      <span class="label">主要训练部位</span>
                    </div>
                  </div>
                </div>

                <div class="exercise-desc">
                  <div class="desc-header">
                    <span class="desc-icon">ℹ️</span>
                    <span>动作要点</span>
                  </div>
                  <p class="desc-content">{{ exercise.description || defaultTips }}</p>
                </div>

                <div class="action-bar">
                  <button class="action-btn reminder" @click="setReminder(exercise)">
                    <span class="btn-icon">⏰</span>
                    训练提醒
                  </button>
                  <button class="action-btn favorite" @click="toggleFavorite(exercise)">
                    <span :class="['heart-icon', { filled: exercise.favorited }]">
                      {{ exercise.favorited ? '❤️' : '🤍' }}
                    </span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 视频播放器组件 -->
    <VideoPlayer
      :show="showVideoModal"
      :exercise-name="currentVideoExercise?.name"
      :exercise-description="currentVideoExercise?.description"
      :user-id="user.id"
      :day-index="currentDayIndex"
      :exercise-index="currentExerciseIndex"
      @close="closeVideoModal"
    />

    <!-- 错误处理 -->
    <div v-if="error" class="error-notification glass">
      <div class="error-icon">⚠️</div>
      <div class="error-content">
      <div class="error-message">{{ error }}</div>
        <button class="retry-button" @click="fetchExercisePlan">
          <span class="retry-icon">🔄</span>
          重试
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import axios from 'axios'
import VideoPlayer from '../components/VideoPlayer.vue'

const store = useStore()
const user = computed(() => store.state.user)

// 响应式数据
const exercisePlan = ref(null)
const error = ref(null)
const loading = ref(false)
const activeExercise = ref(null)
const defaultTips = ref('保持核心收紧，注意呼吸节奏')
const defaultExerciseImage = 'https://pic1.imgdb.cn/item/68161c4e58cb8da5c8d9ea4f.png'

// 视频相关状态
const showVideoModal = ref(false)
const currentVideoExercise = ref(null)
const currentDayIndex = ref(-1)
const currentExerciseIndex = ref(-1)

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
    // 如果有缓存的计划且用户信息未修改，直接使用缓存
    if (user.value.currentPlan && !store.state.profileModified) {
      exercisePlan.value = processPlan(user.value.currentPlan);
      return;
    }

    // 只有在没有计划或用户信息已更新时，才重新生成
    const response = await axios.post(`http://localhost:8000/api/plan/generate?id=${user.value.id}`);
    exercisePlan.value = processPlan(response.data);
    store.commit('updateUserPlan', response.data);
    // 重置 profileModified 标志
    store.commit('setProfileModified', false);
  } catch (err) {
    error.value = err.response?.data?.message || '网络连接异常'
  }
}

const processPlan = (plan) => ({
  weekPlan: plan.WeekPlan.map((day, dayIndex) => ({
    dayNumber: dayIndex + 1,
    exercises: day.exercises.map(ex => ({
      ...ex,
      sets: ex.groups || 3,
      reps: ex.reps || 12,
      interval: ex.interval ? `${ex.interval}秒` : '60秒',
      difficulty: ex.difficulty || 3,
      targetArea: ex.parts || ['全身'],
      image: ex.image || defaultExerciseImage,
      favorited: false
    }))
  })),
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

// 视频相关方法
const showVideoDemo = (exercise, dayIndex, exerciseIndex) => {
  currentVideoExercise.value = exercise
  currentDayIndex.value = dayIndex
  currentExerciseIndex.value = exerciseIndex
  showVideoModal.value = true
}

const closeVideoModal = () => {
  showVideoModal.value = false
  currentVideoExercise.value = null
  currentDayIndex.value = -1
  currentExerciseIndex.value = -1
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
  margin: 0 auto;
  padding: 0 1rem;
}

.user-card {
  border-radius: var(--radius-2xl);
  padding: 2.5rem;
  margin-bottom: 3rem;
  position: relative;
  overflow: hidden;
}

.user-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(59, 130, 246, 0.1));
  z-index: -1;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 2rem;
  margin-bottom: 2rem;
}

.avatar-wrapper {
  position: relative;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--shadow-lg);
  transition: all 0.3s ease;
}

.avatar-ring {
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border: 2px solid transparent;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color)) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: destination-out;
  mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.username {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.tag {
  padding: 0.5rem 1rem;
  border-radius: var(--radius-xl);
  font-size: 0.875rem;
  font-weight: 500;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.tag:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.health-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  border-radius: var(--radius-xl);
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.stat-icon-wrapper {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
}

.stat-icon {
  font-size: 1.5rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-weight: 700;
  font-size: 1.25rem;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.stat-title {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loader {
  text-align: center;
  padding: 3rem;
  border-radius: var(--radius-2xl);
  min-width: 300px;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1.5rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  margin: 0 0 1rem;
  color: var(--text-primary);
  font-size: 1.125rem;
  font-weight: 500;
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: var(--primary-color);
  border-radius: 50%;
  animation: dots 1.4s infinite ease-in-out;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes dots {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.day-card {
  border-radius: var(--radius-2xl);
  padding: 2rem;
  margin-bottom: 2.5rem;
  position: relative;
  overflow: hidden;
}

.day-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.day-info {
  flex: 1;
}

.day-title {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 0 0 0.5rem;
}

.day-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.day-badge {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-lg);
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.duration-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.duration-icon {
  font-size: 1rem;
}

.day-progress {
  position: relative;
}

.progress-ring {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-text {
  position: absolute;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--primary-color);
}

.exercise-grid {
  display: grid;
  gap: 2rem;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
}

.exercise-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
}

.exercise-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}

.card-inner {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  transition: all 0.3s ease;
}

.card-inner.active {
  box-shadow: var(--shadow-xl);
  transform: scale(1.02);
}

.media-wrapper {
  position: relative;
}

.image-frame {
  position: relative;
  overflow: hidden;
}

.exercise-image {
  width: 100%;
  height: 220px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.exercise-card:hover .exercise-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.exercise-card:hover .image-overlay {
  opacity: 1;
}

.overlay-content {
  text-align: center;
}

.demo-button {
  background: rgba(255, 255, 255, 0.95);
  border: none;
  border-radius: var(--radius-xl);
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  color: var(--text-primary);
}

.demo-button:hover {
  background: white;
  transform: scale(1.05);
  box-shadow: var(--shadow-lg);
}

.play-icon {
  font-size: 1.25rem;
}

.exercise-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
}

.badge {
  padding: 0.5rem 1rem;
  border-radius: var(--radius-lg);
  font-size: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.badge-icon {
  font-size: 0.875rem;
}

.content-wrapper {
  padding: 1.5rem;
}

.exercise-name {
  margin: 0 0 1.5rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
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
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  transition: all 0.3s ease;
}

.spec-item:hover {
  background: var(--bg-tertiary);
  transform: translateX(4px);
}

.spec-icon {
  font-size: 1.25rem;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
}

.spec-content {
  flex: 1;
}

.value {
  font-weight: 600;
  color: var(--text-primary);
  display: block;
  margin-bottom: 0.25rem;
}

.label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.exercise-desc {
  padding: 1.25rem;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.05), rgba(59, 130, 246, 0.05));
  border-radius: var(--radius-lg);
  border-left: 4px solid var(--primary-color);
}

.desc-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  font-weight: 600;
  color: var(--text-primary);
}

.desc-icon {
  font-size: 1rem;
}

.desc-content {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.6;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.action-btn {
  padding: 0.75rem 1.25rem;
  border: none;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.reminder {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
}

.reminder:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.favorite {
  background: transparent;
  border: 2px solid var(--border-color);
  color: var(--text-secondary);
  padding: 0.75rem;
}

.favorite:hover {
  border-color: var(--error-color);
  color: var(--error-color);
  transform: scale(1.1);
}

.heart-icon {
  font-size: 1.25rem;
  transition: all 0.3s ease;
}

.heart-icon.filled {
  color: var(--error-color);
}

/* 视频弹窗样式 */
.video-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
}

.video-modal {
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  position: relative;
}

.video-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--border-color);
}

.video-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
}

.close-button {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--radius-lg);
  transition: all 0.3s ease;
}

.close-button:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.video-content {
  padding: 2rem;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-processing {
  text-align: center;
  padding: 2rem;
}

.processing-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1.5rem;
}

.processing-text {
  font-size: 1.125rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.processing-subtext {
  color: var(--text-secondary);
  margin-bottom: 2rem;
}

.refresh-button {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  border: none;
  border-radius: var(--radius-lg);
  padding: 0.75rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  margin: 0 auto;
}

.refresh-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.video-player {
  width: 100%;
  display: flex;
  justify-content: center;
}

.exercise-video {
  width: 100%;
  max-width: 100%;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

.video-error {
  text-align: center;
  padding: 2rem;
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.error-text {
  font-size: 1.125rem;
  color: var(--error-color);
  margin-bottom: 2rem;
}

.retry-button {
  background: var(--error-color);
  color: white;
  border: none;
  border-radius: var(--radius-lg);
  padding: 0.75rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  margin: 0 auto;
}

.retry-button:hover {
  background: #dc2626;
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.error-notification {
  padding: 2rem;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  border-left: 4px solid var(--error-color);
}

.error-icon {
  font-size: 2rem;
}

.error-content {
  flex: 1;
}

.error-message {
  color: var(--error-color);
  font-weight: 500;
  margin-bottom: 1rem;
}

.retry-icon {
  font-size: 1rem;
}

/* 难度等级样式 */
.difficulty-1 { 
  background: rgba(16, 185, 129, 0.1); 
  color: var(--success-color); 
}
.difficulty-2 { 
  background: rgba(245, 158, 11, 0.1); 
  color: var(--warning-color); 
}
.difficulty-3 { 
  background: rgba(239, 68, 68, 0.1); 
  color: var(--error-color); 
}

/* 响应式设计 */
@media (max-width: 768px) {
  .plan-container {
    padding: 0 0.5rem;
  }
  
  .user-card {
    padding: 1.5rem;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .health-stats {
    grid-template-columns: 1fr;
  }
  
  .exercise-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .day-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .video-modal {
    margin: 1rem;
    max-height: 80vh;
  }
  
  .video-modal-header {
    padding: 1rem 1.5rem;
  }
  
  .video-content {
    padding: 1rem;
  }
}
</style>