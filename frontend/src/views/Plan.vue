<!-- Home.vue -->
<template>
    <div class="container">
      <!-- 用户数据概览 -->
      <div class="user-profile">
        <div class="avatar">
          <img :src="user.gender === 'male' ? maleAvatar : femaleAvatar" alt="avatar">
        </div>
        <div class="user-info">
          <h2>{{ user.name }}, {{ user.age }}</h2>
          <div class="health-data">
            <div class="data-item">
              <span class="label">BMI</span>
              <span class="value">{{ calculateBMI() }}</span>
            </div>
            <div class="data-item">
              <span class="label">运动等级</span>
              <span class="value">{{ user.fitnessLevel }}</span>
            </div>
          </div>
        </div>
      </div>
  
      <!-- AI建议主面板 -->
      <div class="ai-recommendation">
        <div class="recommendation-header">
          <div class="ai-icon">
            <svg><!-- AI图标 --></svg>
          </div>
          <h3>您的个性化运动建议</h3>
          <button @click="refreshPlan" class="refresh-btn">
            <svg><!-- 刷新图标 --></svg>
            重新生成计划
          </button>
        </div>
  
        <!-- 计划展示 -->
        <div class="plan-container">
          <div class="plan-card intensity">
            <h4>🏋️ 推荐强度</h4>
            <div class="progress-bar">
              <div :style="intensityStyle" class="progress"></div>
            </div>
            <p>{{ plan.intensityDescription }}</p>
          </div>
  
          <div class="plan-card weekly-plan">
            <h4>📅 本周计划</h4>
            <div class="schedule-grid">
              <div v-for="(day, index) in plan.weeklySchedule" 
                   :key="index"
                   class="day-card"
                   :class="{ active: day.active }">
                {{ day.name }}
                <span class="sport-type">{{ day.sport }}</span>
              </div>
            </div>
          </div>
  
          <div class="plan-card details">
            <h4>📝 计划详情</h4>
            <div class="plan-details">
              <div class="detail-item" v-for="(detail, index) in plan.details" :key="index">
                <div class="icon-box">
                  <svg :class="detail.type"><!-- 类型图标 --></svg>
                </div>
                <div class="content">
                  <h5>{{ detail.title }}</h5>
                  <p>{{ detail.description }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  // 示例数据
  const user = ref({
    gender: 'male',
    age: 28,
    height: 175,
    weight: 70,
    fitnessLevel: '中级',
    preferences: ['力量训练', '游泳']
  });
  
  const plan = ref({
    intensity: 65,
    weeklySchedule: [],
    details: []
  });
  
  const calculateBMI = () => {
    return (user.value.weight / ((user.value.height/100) ** 2)).toFixed(1);
  };
  
  const intensityStyle = computed(() => ({
    width: `${plan.value.intensity}%`,
    background: `linear-gradient(90deg, #4A90E2 ${plan.value.intensity}%, #E0E0E0 0%)`
  }));
  </script>
  
  <style scoped>
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }
  
  .user-profile {
    display: flex;
    align-items: center;
    gap: 2rem;
    padding: 1.5rem;
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-radius: 16px;
    box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  }
  
  .avatar img {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    border: 3px solid #4A90E2;
  }
  
  .health-data {
    display: flex;
    gap: 2rem;
    margin-top: 1rem;
  }
  
  .data-item {
    text-align: center;
    background: white;
    padding: 0.8rem 1.5rem;
    border-radius: 12px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  }
  
  .ai-recommendation {
    margin-top: 2rem;
    background: white;
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  }
  
  .recommendation-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 2rem;
  }
  
  .refresh-btn {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.8rem 1.5rem;
    background: #4A90E2;
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: transform 0.2s;
  }
  
  .plan-container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1.5rem;
  }
  
  .plan-card {
    background: #f8f9fa;
    padding: 1.5rem;
    border-radius: 16px;
    transition: transform 0.2s;
  }
  
  .plan-card:hover {
    transform: translateY(-5px);
  }
  
  .schedule-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 0.8rem;
  }
  
  .day-card {
    padding: 1rem;
    text-align: center;
    background: white;
    border-radius: 8px;
    transition: all 0.2s;
  }
  
  .day-card.active {
    background: #4A90E2;
    color: white;
  }
  
  .progress-bar {
    height: 12px;
    background: #eee;
    border-radius: 6px;
    overflow: hidden;
    margin: 1rem 0;
  }
  
  .progress {
    height: 100%;
    border-radius: 6px;
    transition: width 0.5s ease;
  }
  
  .detail-item {
    display: flex;
    gap: 1rem;
    align-items: center;
    padding: 1rem;
    background: white;
    margin: 1rem 0;
    border-radius: 12px;
  }
  
  .icon-box {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: grid;
    place-items: center;
  }
  
  .icon-box.warmup { background: #FFD70033; }
  .icon-box.cardio { background: #FF6B6B33; }
  .icon-box.strength { background: #4A90E233; }
  
  @media (max-width: 768px) {
    .user-profile { flex-direction: column; }
    .schedule-grid { grid-template-columns: repeat(2, 1fr); }
  }
  </style>