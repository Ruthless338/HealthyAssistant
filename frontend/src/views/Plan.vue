<!-- src/views/Plan.vue -->
<template>
  <div class="plan-container">
    <h1>个性化运动计划</h1>
    
    <div v-if="userInfo" class="user-info">
      <h2>用户信息</h2>
      <div class="info-card">
        <p>姓名: {{ userInfo.name }}</p>
        <p>年龄: {{ userInfo.age }}</p>
        <p>体重: {{ userInfo.weight }} kg</p>
        <p>身高: {{ userInfo.height }} cm</p>
        <p>兴趣: {{ userInfo.interests.join(', ') }}</p>
        <p>目标: {{ userInfo.goals.join(', ') }}</p>
      </div>
    </div>
    
    <div v-if="exercisePlan" class="exercise-plan">
      <h2>运动计划</h2>
      
      <div v-for="(day, index) in exercisePlan.weekPlan" :key="index" class="day-plan">
        <h3>星期{{ index + 1 }}</h3>
        <div class="exercise-items">
          <div v-for="(exercise, exIndex) in day.exercises" :key="exIndex" class="exercise-item">
            <img :src="exercise.image" :alt="exercise.name" class="exercise-image" />
            <div class="exercise-details">
              <h4>{{ exercise.name }}</h4>
              <p>组数: {{ exercise.sets }}</p>
              <p>次数: {{ exercise.reps }}</p>
              <p>{{ exercise.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="loading">
      <p>正在生成您的运动计划...</p>
    </div>
  </div>
</template>

<script>
import { getUserInfo } from '@/services/api';
import { getExercisePlanFromKimi } from '@/services/kimi';
import { ref, onMounted } from 'vue';

export default {
  name: 'Plan',
  setup() {
    const userInfo = ref(null);
    const exercisePlan = ref(null);

    const fetchUserInfo = async () => {
      try {
        const user = await getUserInfo();
        userInfo.value = user;
        return user;
      } catch (error) {
        console.error('获取用户信息失败:', error);
      }
    };

    const fetchExercisePlan = async (userData) => {
      try {
        const plan = await getExercisePlanFromKimi(userData);
        exercisePlan.value = plan;
      } catch (error) {
        console.error('获取运动计划失败:', error);
      }
    };

    onMounted(async () => {
      const user = await fetchUserInfo();
      if (user) {
        fetchExercisePlan(user);
      }
    });

    return {
      userInfo,
      exercisePlan,
    };
  },
};
</script>

<style scoped>
.plan-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.user-info {
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-card {
  background-color: white;
  padding: 15px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.exercise-plan {
  margin-top: 30px;
}

.day-plan {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
}

.exercise-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.exercise-item {
  background-color: white;
  border-radius: 6px;
  padding: 15px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.exercise-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.exercise-details h4 {
  margin-top: 0;
  margin-bottom: 10px;
}

.loading {
  text-align: center;
  padding: 30px;
  color: #666;
}
</style>