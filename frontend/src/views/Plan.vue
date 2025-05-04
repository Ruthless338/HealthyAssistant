<!-- src/views/Plan.vue -->
<template>
  <div class="plan-container">
    <!-- 用户信息卡片 -->
    <div class="user-card glassmorphism">
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
        <stat-item 
          title="身体数据"
          :value="`${user.height}cm / ${user.weight}kg`"
          icon="🏋️"
        />
        <stat-item 
          title="BMI指数"
          :value="bmi"
          :sub-value="bmiStatus"
          icon="📊"
        />
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loader">
        <div class="pulse"></div>
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
            @mouseenter="setActiveExercise(dayIndex, exIndex)"
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
                      <i class="icon-play-circle"></i>
                      <span>动作示范</span>
                    </button>
                  </div>
                </div>
                <div class="exercise-badge">
                  <span class="badge difficulty" :class="difficultyClass(exercise.difficulty)">
                    难度：{{ difficultyText(exercise.difficulty) }}
                  </span>
                  <span class="badge type">
                    {{ exercise.type || '综合训练' }}
                  </span>
                </div>
              </div>

              <!-- 详细信息层 -->
              <div class="content-wrapper">
                <h4 class="exercise-name">{{ exercise.name }}</h4>
                
                <div class="specs">
                  <div class="spec-item">
                    <i class="icon-repeats"></i>
                    <div class="spec-content">
                      <span class="value">{{ exercise.sets }} 组 × {{ exercise.reps }} 次</span>
                      <span class="label">组间休息 {{ exercise.interval }}秒</span>
                    </div>
                  </div>
                  
                  <div class="spec-item">
                    <i class="icon-target"></i>
                    <div class="spec-content">
                      <span class="value">{{ exercise.targetArea.join(' / ') }}</span>
                      <span class="label">主要训练部位</span>
                    </div>
                  </div>
                </div>

                <div class="exercise-desc">
                  <div class="desc-header">
                    <i class="icon-info"></i>
                    <span>动作要点</span>
                  </div>
                  <p class="desc-content">{{ exercise.description || defaultTips }}</p>
                </div>

                <div class="action-bar">
                  <button class="action-btn reminder" @click="setReminder(exercise)">
                    <i class="icon-alarm"></i>训练提醒
                  </button>
                  <button class="action-btn favorite" @click="toggleFavorite(exercise)">
                    <i :class="['icon-heart', { filled: exercise.favorited }]"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误处理 -->
    <error-notification
      v-if="error"
      :error="error"
      @retry="fetchExercisePlan"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import axios from 'axios'
import { useLoading } from 'vue-loading-overlay'

// 组件配置
const store = useStore()
const user = computed(() => store.state.user)
const $loading = useLoading()

// 响应式数据
const exercisePlan = ref(null)
const error = ref(null)
const hoverExercise = ref(-1)

// 计算属性
const userTags = computed(() => [
  ...user.value.interest,
  ...user.value.goal,
  ...user.value.part
])
const bmi = computed(() => 
  (user.value.weight / ((user.value.height/100) ** 2)).toFixed(1)
)

// 生命周期钩子
onMounted(async () => {
  const loader = $loading.show()
  try {
    await fetchExercisePlan()
  } catch (err) {
    error.value = '计划生成失败，请稍后重试'
  } finally {
    loader.hide()
  }
})

// 获取运动计划
const fetchExercisePlan = async () => {
  try {
    const response = await axios.post('/api/plan/generate', {
      id: user.value.id,
    })
    //response.data为两层列表
    console.log(response.data)
    exercisePlan.value = processPlan(response.data)
    console.log(exercisePlan.value)
  } catch (err) {
    handleError(err)
  }
}

// 数据处理方法
const processPlan = (plan) => {
  return {
    weekPlan: plan.weekPlan.map((day, dayIndex) => ({
      // 添加日期标识
      dayNumber: dayIndex + 1,
      exercises: day.exercises.map(ex => ({
        // 字段重映射
        name: ex.name,
        description: ex.description,
        sets: ex.group,       // 后端group对应前端sets
        reps: ex.reps || 12,  // 后端没有reps时设置默认值
        interval: ex.interval + '秒', // 添加单位
        image: ex.image || defaultExerciseImage,
        
        // 新增前端需要的字段（需后端补充或前端计算）
        difficulty: ex.difficulty || 3,     // 难度星级（1-5）
        tips: ex.tips || '保持核心收紧，注意呼吸节奏' // 默认提示
      }))
    }))
  }
}

// 在data部分添加默认图片
const defaultExerciseImage = 'https://pic1.imgdb.cn/item/68161c4e58cb8da5c8d9ea4f.png'

// 错误处理
const handleError = (err) => {
  error.value = err.response?.data?.message || '网络连接异常'
}
</script>

<style scoped>
/* 新增的样式设计 */
.plan-container {
  --primary-color: #2c3e50;
  --accent-color: #42b983;
  --glass-bg: rgba(255, 255, 255, 0.95);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.card-inner {
  transform-style: preserve-3d;
  transition: transform 0.6s;
}

.media-wrapper {
  position: relative;
  overflow: hidden;
  border-radius: 1rem 1rem 0 0;
}

.hover-overlay {
  background: linear-gradient(0deg, rgba(0,0,0,0.7) 20%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s;
}


.user-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 1.5rem;
  padding: 2rem;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  margin-bottom: 3rem;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 2rem;
  margin-bottom: 2rem;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.user-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.tag {
  background: rgba(255,255,255,0.9);
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  font-size: 0.9em;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.user-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1.5rem;
}

.day-card {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.exercise-card {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 1.5rem;
  padding: 1.5rem;
  border-radius: 0.8rem;
  transition: transform 0.2s;
}

.exercise-card:hover {
  transform: translateY(-2px);
}

.exercise-media {
  position: relative;
  border-radius: 0.8rem;
  overflow: hidden;
}

.exercise-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: transform 0.3s;
}

.exercise-image.hovered {
  transform: scale(1.05);
}

.exercise-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 60%, rgba(0,0,0,0.6));
  display: flex;
  align-items: flex-end;
  padding: 1rem;
  opacity: 0;
  transition: opacity 0.3s;
}

.exercise-card:hover .exercise-overlay {
  opacity: 1;
}


</style>