<template>
  <div class="video-player-container">
    <!-- 视频播放弹窗 -->
    <div v-if="showModal" class="video-modal-overlay" @click="closeModal">
      <div class="video-modal glass" @click.stop>
        <div class="video-modal-header">
          <h3 class="video-title">{{ exerciseName }} - 动作示范</h3>
          <button class="close-button" @click="closeModal">
            <span class="close-icon">✕</span>
          </button>
        </div>
        
        <div class="video-content">
          <!-- 视频生成中状态 -->
          <div v-if="videoStatus === 'PROCESSING'" class="video-processing">
            <div class="processing-spinner"></div>
            <p class="processing-text">AI正在生成动作示范视频...</p>
            <p class="processing-subtext">这可能需要几分钟时间，请耐心等待</p>
            <button class="refresh-button" @click="checkStatus">
              <span class="refresh-icon">🔄</span>
              刷新状态
            </button>
          </div>
          
          <!-- 视频播放器 -->
          <div v-else-if="videoStatus === 'SUCCESS' && videoUrl" class="video-player">
            <video 
              :src="videoUrl" 
              controls 
              autoplay
              class="exercise-video"
              @error="handleVideoError"
            >
              您的浏览器不支持视频播放
            </video>
          </div>
          
          <!-- 视频生成失败 -->
          <div v-else-if="videoStatus === 'FAILED'" class="video-error">
            <div class="error-icon">⚠️</div>
            <p class="error-text">视频生成失败</p>
            <button class="retry-button" @click="retryGeneration">
              <span class="retry-icon">🔄</span>
              重新生成
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  show: Boolean,
  exerciseName: String,
  exerciseDescription: String,
  userId: Number,
  dayIndex: Number,
  exerciseIndex: Number
})

const emit = defineEmits(['close'])

// 响应式数据
const showModal = ref(false)
const videoStatus = ref('')
const videoUrl = ref('')
const videoTaskId = ref('')

// 监听show属性变化
watch(() => props.show, (newVal) => {
  showModal.value = newVal
  if (newVal) {
    videoStatus.value = ''
    videoUrl.value = ''
    videoTaskId.value = ''
    checkExistingVideo()
  }
})

// 检查是否已有视频
const checkExistingVideo = async () => {
  try {
    const response = await axios.get(`http://localhost:8000/api/video/exercise`, {
      params: {
        userId: props.userId,
        dayIndex: props.dayIndex,
        exerciseIndex: props.exerciseIndex
      }
    })
    
    if (response.data.hasVideo) {
      videoStatus.value = response.data.status
      videoUrl.value = response.data.videoUrl
      videoTaskId.value = response.data.taskId
      console.log(response.data.videoUrl);
      
      if (response.data.status === 'PROCESSING') {
        pollVideoStatus()
      }
    } else {
      // 没有视频，开始生成
      await generateVideo()
    }
  } catch (err) {
    console.error('检查视频状态失败:', err)
    videoStatus.value = 'FAILED'
  }
}

// 生成视频
const generateVideo = async () => {
  try {
    videoStatus.value = 'PROCESSING'
    
    const response = await axios.post('http://localhost:8000/api/video/generate', {
      userId: props.userId,
      exerciseName: props.exerciseName,
      description: props.exerciseDescription,
      dayIndex: props.dayIndex,
      exerciseIndex: props.exerciseIndex
    })
    
    videoTaskId.value = response.data.taskId
    pollVideoStatus()
  } catch (err) {
    console.error('生成视频失败:', err)
    videoStatus.value = 'FAILED'
  }
}

// 轮询检查视频状态
const pollVideoStatus = () => {
  if (!videoTaskId.value) return
  
  const checkStatus = async () => {
    try {
      const response = await axios.get(`http://localhost:8000/api/video/status/${videoTaskId.value}`)
      
      if (response.data.status === 'SUCCESS') {
        videoStatus.value = 'SUCCESS'
        videoUrl.value = response.data.videoUrl
        return
      } else if (response.data.status === 'FAILED') {
        videoStatus.value = 'FAILED'
        return
      }
      
      // 继续轮询
      setTimeout(checkStatus, 5000) // 每5秒检查一次
    } catch (err) {
      console.error('检查视频状态失败:', err)
      videoStatus.value = 'FAILED'
    }
  }
  
  checkStatus()
}

// 手动检查状态
const checkStatus = () => {
  if (videoTaskId.value) {
    pollVideoStatus()
  }
}

// 重新生成视频
const retryGeneration = async () => {
  await generateVideo()
}

// 关闭弹窗
const closeModal = () => {
  showModal.value = false
  videoStatus.value = ''
  videoUrl.value = ''
  videoTaskId.value = ''
  emit('close')
}

// 处理视频错误
const handleVideoError = () => {
  videoStatus.value = 'FAILED'
}
</script>

<style scoped>
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

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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

/* 响应式设计 */
@media (max-width: 768px) {
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