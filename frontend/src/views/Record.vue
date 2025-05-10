<template>
  <div class="record-container">
    <h1>运动记录</h1>
    <div class="add-record-container">
      <div class="add-record">
        <h2>添加新记录</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>运动类型</label>
            <input 
              type="text" 
              v-model="newRecord.exerciseType" 
              required 
              placeholder="例如：跑步、游泳"
            />
          </div>

          <div class="form-group">
            <label>组数</label>
            <input 
              type="number" 
              v-model.number="newRecord.sets" 
              required 
              min="1"
            />
          </div>

          <div class="form-group">
            <label>每组时长（秒）</label>
            <input 
              type="number" 
              v-model.number="newRecord.durationPerSet" 
              required 
              min="1"
            />
          </div>

          <div class="form-group">
            <label>间隔时间（秒）</label>
            <input 
              type="number" 
              v-model.number="newRecord.restBetweenSets" 
              required 
              min="0"
            />
          </div>

          <div class="form-group">
            <label>自我感受</label>
            <textarea 
              v-model="newRecord.notes" 
              required 
              placeholder="记录本次运动的感受"
            ></textarea>
          </div>

          <div class="form-group">
            <label>上传运动照片</label>
            <ImageUpload 
              @uploaded="handleImageUpload"
              @clear="clearImage"
              ref="imageUploadRef"
            />
          </div>

          <button type="submit" class="submit-btn">保存记录</button>
        </form>
      </div>
    </div>

    <div class="records-list">
      <div 
        v-for="record in records" 
        :key="record.recordId" 
        class="record-card"
      >
        <div class="card-header">
          <h3>{{ record.exerciseType }}</h3>
          <span class="record-date">{{ formatDate(record.recordDate) }}</span>
        </div>
        
        <div class="card-content">
          <div class="stats">
            <div class="stat-item">
              <span class="label">组数：</span>
              <span class="value">{{ record.sets }} 组</span>
            </div>
            <div class="stat-item">
              <span class="label">单组时长：</span>
              <span class="value">{{ record.durationPerSet }} 秒</span>
            </div>
            <div class="stat-item">
              <span class="label">间隔时间：</span>
              <span class="value">{{ record.restBetweenSets }} 秒</span>
            </div>
          </div>
          
          <div class="notes" v-if="record.notes">
            <p>{{ record.notes }}</p>
          </div>

          <img 
            v-if="record.imageUrl" 
            :src="getImageUrl(record.imageUrl)" 
            class="record-image"
            alt="运动照片"
          />
        </div>

        <div class="card-actions">
          <button 
            class="delete-btn" 
            @click="deleteRecord(record.recordId!)"
          >
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useStore } from 'vuex';
import axios from 'axios';
import ImageUpload from '../components/ImageUpload.vue';

const store = useStore();
const isSubmitting = ref(false);
const imageUploadRef = ref(null);

interface SportRecord {
  recordId?: number;
  exerciseType: string;
  sets: number;
  durationPerSet: number;
  restBetweenSets: number;
  recordDate: string;
  notes: string;
  imageUrl?: string;
}


const records = ref<SportRecord[]>([]);
const newRecord = ref<SportRecord>({
  exerciseType: '',
  sets: 1,
  durationPerSet: 30,
  restBetweenSets: 60,
  recordDate: '',
  notes: '',
  imageUrl: ''
});

// 图片上传处理
const handleImageUpload = (fileName: string) => {
  newRecord.value.imageUrl = fileName;
};

const clearImage = () => {
  newRecord.value.imageUrl = '';
};

// 获取图片完整URL
const getImageUrl = (fileName: string) => {
  return `http://localhost:8000/uploads/${fileName}`;
};

// 日期格式化
const formatDate = (dateString: string) => {
  const options: Intl.DateTimeFormatOptions = { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit' 
  };
  return new Date(dateString).toLocaleDateString('zh-CN', options);
};

// 加载记录
const loadRecords = async () => {
  try {
    const username = store.state.user.username;
    // console.log("当前用户名:", username);
    const response = await axios.get(
      `http://localhost:8000/api/record/readRecord/${username}`
    );
    console.log("获取运动记录:", response.data);
    records.value = response.data;
  } catch (error) {
    console.error('加载运动记录失败:', error);
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    isSubmitting.value = true;
    
    // 确保图片已上传
    if (newRecord.value.imageUrl === '') {
      console.warn('请先上传图片');
      return;
    }

    const recordData = {
      ...newRecord.value,
      recordDate: new Date().toISOString()
    };

    const response = await axios.post(
      `http://localhost:8000/api/record/addRecord`,
      {
        userRecord: recordData,
        username: store.state.user.username
      }
    );

    console.log("添加成功:", response.data);
    
    // 重置表单和图片上传组件
    newRecord.value = { 
      exerciseType: '',
      sets: 1,
      durationPerSet: 30,
      restBetweenSets: 60,
      recordDate: '',
      notes: '',
      imageUrl: ''
     };
    clearImage(); // 调用ImageUpload的clear方法
    await loadRecords();
  } catch (error) {
    console.error('保存记录失败:', error);
  } finally {
    isSubmitting.value = false;
  }
};

// 删除记录
const deleteRecord = async (recordId: number) => {
  try {
    await axios.delete(`http://localhost:8000/api/record/deleteRecord/${recordId}`);
    await loadRecords();
  } catch (error) {
    console.error('删除记录失败:', error);
  }
};

onMounted(() => {
  loadRecords();
});
</script>

<style scoped>
.record-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.add-record-container {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.form-group {
  margin-bottom: 1.2rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

input, textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
}

.submit-btn {
  background: #4CAF50;
  color: white;
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.3s;
}

.submit-btn:hover {
  background: #45a049;
}

.records-list {
  display: grid;
  gap: 1.5rem;
}

.record-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.record-date {
  color: #666;
  font-size: 0.9rem;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
}

.stat-item {
  background: #f8f9fa;
  padding: 0.8rem;
  border-radius: 8px;
}

.label {
  color: #666;
  font-size: 0.9rem;
}

.value {
  font-weight: 500;
  color: #333;
}

.record-image {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 8px;
  margin-top: 1rem;
}

.delete-btn {
  background: #ff4444;
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.delete-btn:hover {
  background: #cc0000;
}
</style>