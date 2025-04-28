<template>
    <div class="record-container">
      <h1>运动记录</h1>
      <div class="add-record-container">
        <div class="add-record">
          <h2>添加新记录</h2>
          <form @submit.prevent="addRecord">
            <div class="form-group">
              <label for="title">标题</label>
              <input type="text" id="title" v-model="record.title" required />
            </div>
            <div class="form-group">
              <label for="description">描述</label>
              <textarea id="description" v-model="record.description" required></textarea>
            </div>
            <div class="form-group">
              <label for="duration">时长 (分钟)</label>
              <input type="number" id="duration" v-model.number="record.duration" required />
            </div>
            <div class="form-group">
              <label for="calories">消耗卡路里</label>
              <input type="number" id="calories" v-model.number="record.calories" required />
            </div>
            <div class="form-group">
              <label for="username">用户名</label>
              <input type="text" id="username" v-model="record.username" required />
            </div>
            <button type="submit">添加记录</button>
          </form>
        </div>
        <div class="add-record" v-if="selectedRecord">
          <h2>更新记录</h2>
          <form @submit.prevent="updateRecord">
            <!-- 体重，时间， -->
            <div class="form-group">
              <label for="updateDescription">描述</label>
              <textarea id="updateDescription" v-model="selectedRecord.description" required></textarea>
            </div>
            <div class="form-group">
              <label for="updateDuration">时长 (分钟)</label>
              <input type="number" id="updateDuration" v-model.number="selectedRecord.duration" required />
            </div>

            <button type="submit">更新记录</button>
          </form>
        </div>
      </div>
      <div class="records-list">
        <div
          v-for="(item, index) in records"
          :key="index"
          class="record-item"
        >
          <div class="record-header">
            <h3>{{ item.title }}</h3>
            <span>{{ item.date }}</span>
          </div>
          <div class="record-content">
            <p>{{ item.description }}</p>
            <div class="record-stats">
              <span>时长: {{ item.duration }} 分钟</span>
              <span>消耗: {{ item.calories }} 卡路里</span>
            </div>
          </div>
          <div class="record-actions">
            <button @click="deleteRecord(item.id)">删除</button>
            <button @click="selectRecordForUpdate(item)">更新</button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import axios from 'axios';
  import { ref, onMounted } from 'vue';
  
  interface UserRecord {
    id: string;
    username: string;
    title: string;
    description: string;
    duration: number;
    calories: number;
    date: string;
  }
  
  const records = ref<UserRecord[]>([]);
  const record = ref<UserRecord>({
    id: '',
    username: '',
    title: '',
    description: '',
    duration: 0,
    calories: 0,
    date: '',
  });
  const selectedRecord = ref<UserRecord | null>(null);
  
  onMounted(async () => {
    loadRecords();
  });
  
  const loadRecords = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/record/readRecord');
      records.value = response.data;
    } catch (error) {
      console.error('Failed to load records:', error);
    }
  };
  
  const addRecord = async () => {
    try {
      record.value.date = new Date().toISOString().split('T')[0];
      await axios.post('http://localhost:8080/api/record/addRecord', record.value);
      await loadRecords();
      record.value = {
        id: '',
        username: '',
        title: '',
        description: '',
        duration: 0,
        calories: 0,
        date: '',
      };
    } catch (error) {
      console.error('Failed to add record:', error);
    }
  };
  
  const deleteRecord = async (recordId: string) => {
    try {
      await axios.delete(`http://localhost:8080/api/record/deleteRecord/${recordId}`);
      await loadRecords();
    } catch (error) {
      console.error('Failed to delete record:', error);
    }
  };
  
  const selectRecordForUpdate = (recordToUpdate: UserRecord) => {
    selectedRecord.value = { ...recordToUpdate };
  };
  
  const updateRecord = async () => {
    if (selectedRecord.value) {
      try {
        await axios.put('http://localhost:8080/api/record/updateRecord', selectedRecord.value);
        await loadRecords();
        selectedRecord.value = null;
      } catch (error) {
        console.error('Failed to update record:', error);
      }
    }
  };
  </script>
  
  <style scoped>
  .record-container {
    max-width: 1000px;
    margin: 0 auto;
    padding: 20px;
  }
  
  .records-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    margin-top: 30px;
  }
  
  .add-record-container {
    margin-bottom: 30px;
  }
  
  .add-record {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .form-group {
    margin-bottom: 15px;
  }
  
  label {
    display: block;
    margin-bottom: 5px;
  }
  
  input,
  textarea {
    width: 100%;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
  }
  
  button {
    background-color: #4CAF50;
    color: white;
    padding: 10px 15px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    margin-right: 10px;
  }
  
  button:hover {
    background-color: #45a049;
  }
  
  .record-item {
    background-color: #f9f9f9;
    border-radius: 8px;
    padding: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 15px;
  }
  
  .record-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
  }
  
  .record-stats {
    display: flex;
    justify-content: space-between;
    margin-top: 10px;
  }
  
  .record-actions {
    margin-top: 15px;
    display: flex;
    justify-content: flex-end;
  }
  </style>