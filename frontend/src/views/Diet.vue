<!-- src/views/Diet.vue -->

<template>
    <div class="diet-management">
      <h1>个性化饮食管理</h1>
      
      <!-- 用户基本信息展示 -->
      <div class="user-info">
        <img :src="user.avatar" alt="用户头像" class="avatar">
        <div class="user-details">
          <h2>{{ user.username }}</h2>
          <p>年龄: {{ user.age }} | 性别: {{ user.gender }}</p>
          <p>身高: {{ user.height }}cm | 体重: {{ user.weight }}kg</p>
          <p>目标: {{ user.goal.join(', ') }}</p>
          <p>运动兴趣: {{ user.interest.join(', ') }}</p>
        </div>
      </div>
      
      <!-- 饮食计划展示 -->
      <div class="diet-plan">
        <h2>本周饮食计划</h2>
        <div class="week-plan">
          <div v-for="(day, index) in dietPlan.days" :key="index" class="day-plan">
            <h3>{{ day.date }}</h3>
            <div class="meal-time" v-for="(meal, mealIndex) in day.meals" :key="mealIndex">
              <h4>{{ meal.time }}</h4>
              <div class="food-items">
                <div v-for="(food, foodIndex) in meal.foods" :key="foodIndex" class="food-item">
                  <img :src="food.imageUrl" :alt="food.name" class="food-image">
                  <div class="food-details">
                    <h5>{{ food.name }}</h5>
                    <p>份量: {{ food.quantity }}</p>
                    <p>卡路里: {{ food.calories }} kcal</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 营养成分分析 -->
      <div class="nutrition-analysis">
        <h2>营养成分分析</h2>
        <div class="nutrition-chart">
          <!-- 这里可以使用图表库如Chart.js或ECharts展示营养成分 -->
          <div class="chart-placeholder">营养成分图表</div>
        </div>
        <div class="nutrition-details">
          <div class="nutrition-item">
            <h4>蛋白质</h4>
            <p>{{ nutritionData.protein }}g</p>
          </div>
          <div class="nutrition-item">
            <h4>碳水化合物</h4>
            <p>{{ nutritionData.carbohydrates }}g</p>
          </div>
          <div class="nutrition-item">
            <h4>脂肪</h4>
            <p>{{ nutritionData.fat }}g</p>
          </div>
          <div class="nutrition-item">
            <h4>总热量</h4>
            <p>{{ nutritionData.calories }}kcal</p>
          </div>
        </div>
      </div>
      
      <!-- 饮食记录与反馈 -->
      <div class="diet-records">
        <h2>我的饮食记录</h2>
        <div class="record-form">
          <h3>记录今日饮食</h3>
          <div class="meal-time">
            <h4>早餐</h4>
            <div class="food-input">
              <input type="text" placeholder="食物名称" v-model="todayRecord.breakfast.food">
              <input type="text" placeholder="份量" v-model="todayRecord.breakfast.quantity">
            </div>
          </div>
          <div class="meal-time">
            <h4>午餐</h4>
            <div class="food-input">
              <input type="text" placeholder="食物名称" v-model="todayRecord.lunch.food">
              <input type="text" placeholder="份量" v-model="todayRecord.lunch.quantity">
            </div>
          </div>
          <div class="meal-time">
            <h4>晚餐</h4>
            <div class="food-input">
              <input type="text" placeholder="食物名称" v-model="todayRecord.dinner.food">
              <input type="text" placeholder="份量" v-model="todayRecord.dinner.quantity">
            </div>
          </div>
          <div class="meal-time">
            <h4>加餐</h4>
            <div class="food-input">
              <input type="text" placeholder="食物名称" v-model="todayRecord.snacks.food">
              <input type="text" placeholder="份量" v-model="todayRecord.snacks.quantity">
            </div>
          </div>
          <button @click="saveRecord">保存记录</button>
        </div>
        
        <div class="feedback">
          <h3>反馈与评价</h3>
          <textarea placeholder="请输入对饮食建议的反馈和评价..." v-model="feedbackText"></textarea>
          <button @click="submitFeedback">提交反馈</button>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    name: 'Diet',
    data() {
      return {
        user: {
          id: null,
          username: '',
          age: 0,
          height: 0,
          weight: 0,
          goal: [],
          avatar: '',
          gender: '',
          userRecords: []
        },
        dietPlan: {
          dailyMeals: []
        },
        nutritionData: {
          dailyCalories: 0,
          dailyProtein: 0,
          dailyCarbohydrates: 0,
          dailyFat: 0
        },
        todayRecord: {
          date: new Date().toISOString().split('T')[0],
          breakfast: '',
          lunch: '',
          dinner: '',
          snacks: ''
        },
        feedbackText: ''
      };
    },
    methods: {
      async fetchUserData() {
        try {
          const username = this.$route.params.username;
          const response = await axios.get(`/api/auth/get${username}`);
          this.user = response.data;
          this.generateDietPlan();
        } catch (error) {
          console.error('获取用户数据失败:', error);
        }
      },
      async generateDietPlan() {
        try {
          const aiResponse = await axios.post('/api/ai/generate-diet-plan', {
            user: this.user
          });
          this.dietPlan = aiResponse.data;
        } catch (error) {
          console.error('生成饮食计划失败:', error);
        }
      },
      saveRecord() {
        axios.post('/api/diet/record', this.todayRecord)
          .then(response => {
            console.log('饮食记录保存成功:', response.data);
            this.todayRecord = {
              date: new Date().toISOString().split('T')[0],
              breakfast: '',
              lunch: '',
              dinner: '',
              snacks: ''
            };
          })
          .catch(error => {
            console.error('保存饮食记录失败:', error);
          });
      },
      submitFeedback() {
        axios.post('/api/diet/feedback', {
          username: this.user.username,
          feedback: this.feedbackText
        })
          .then(response => {
            console.log('反馈提交成功:', response.data);
            this.feedbackText = '';
          })
          .catch(error => {
            console.error('提交反馈失败:', error);
          });
      }
    },
    mounted() {
      this.fetchUserData();
    }
  };
  </script>
  
  <style scoped>
  .diet-management {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 30px;
    padding: 20px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    background-color: #f5f5f5;
  }
  
  .avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
    margin-right: 20px;
  }
  
  .user-details h2 {
    margin-top: 0;
  }
  
  .diet-plan, .nutrition-analysis, .diet-records {
    margin-bottom: 30px;
    padding: 20px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }
  
  .week-plan {
    display: flex;
    overflow-x: auto;
    gap: 20px;
    padding-bottom: 10px;
  }
  
  .day-plan {
    min-width: 200px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    padding: 15px;
    background-color: #fafafa;
  }
  
  .meal-time {
    margin-bottom: 15px;
  }
  
  .food-items {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 10px;
  }
  
  .food-item {
    display: flex;
    align-items: center;
    background-color: #fff;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    padding: 8px;
  }
  
  .food-image {
    width: 50px;
    height: 50px;
    border-radius: 4px;
    object-fit: cover;
    margin-right: 10px;
  }
  
  .nutrition-chart {
    height: 300px;
    background-color: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;
  }
  
  .nutrition-details {
    display: flex;
    justify-content: space-around;
    text-align: center;
  }
  
  .nutrition-item {
    background-color: #f0f7ff;
    padding: 15px;
    border-radius: 8px;
    min-width: 150px;
  }
  
  .record-form, .feedback {
    margin-bottom: 20px;
  }
  
  .food-input {
    display: flex;
    gap: 10px;
    margin-bottom: 10px;
  }
  
  .food-input input {
    flex: 1;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
  }
  
  button {
    background-color: #4CAF50;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
  }
  
  button:hover {
    background-color: #45a049;
  }
  
  textarea {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    resize: vertical;
    min-height: 100px;
  }
  </style>