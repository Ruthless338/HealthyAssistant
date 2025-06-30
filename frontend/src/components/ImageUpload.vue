<!-- ImageUpload.vue -->
<script lang="ts">
import { ref, defineEmits } from 'vue';
import axios from 'axios';

export default {
  name: 'ImageUpload',
  emits: ['uploaded', 'clear'],
  setup(props, { emit }) {
    const file = ref<File | null>(null);
    const previewUrl = ref<string>('');
    const isUploading = ref(false);
    const fileInput = ref<HTMLInputElement | null>(null);

    const onFileChange = async (event: Event) => {
      const input = event.target as HTMLInputElement;
      if (input.files?.length) {
        file.value = input.files[0];
        previewUrl.value = URL.createObjectURL(file.value);
        const formData = new FormData();
        formData.append('file', file.value);
        try {
          const response = await axios.post('http://localhost:8000/api/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
          console.log('上传成功:', response.data);
          emit('uploaded', response.data);
        } catch (error) {
          console.error('上传失败:', error);
          emit('clear');
        }
      }
    };

    const clear = () => {
      if (fileInput.value) {
        fileInput.value.value = ''; // 清空文件选择框
      }
      file.value = null;
      previewUrl.value = '';
      emit('clear');
    };

    return {
      onFileChange,
      previewUrl,
      file,
      isUploading,
      fileInput,
      clear
    };
  }
};
</script>

<template>
  <div class="image-upload">
    <input 
      type="file" 
      @change="onFileChange" 
      accept="image/*"
      ref="fileInput" 
    />
    <img v-if="previewUrl" :src="previewUrl" alt="Preview" class="preview" />
    <button 
      v-if="file" 
      @click="clear"
      :disabled="isUploading" 
    >
      {{ isUploading ? '上传中...' : '重新选择图片' }}
    </button>
  </div>
</template>

  
<style>
.preview {
  max-width: 200px;
  max-height: 200px;
  margin-top: 10px;
}
</style>