<template>
  <div class="media-upload">
    <div class="media-list" v-if="mediaList.length > 0">
      <div v-for="(url, index) in mediaList" :key="index" class="media-item">
        <template v-if="isImage(url)">
          <img :src="url" alt="media" @click="preview(url)" />
        </template>
        <template v-else-if="isVideo(url)">
          <video :src="url" controls></video>
        </template>
        <div class="media-actions">
          <button class="delete-btn" @click="removeMedia(index)">×</button>
        </div>
      </div>
    </div>
    
    <div class="upload-trigger" @click="triggerSelect">
      <div v-if="uploading" class="uploading-state">
        <div class="spinner"></div>
        <span>{{ progress }}%</span>
      </div>
      <div v-else class="normal-state">
        <span class="icon">+</span>
        <span>上传图片/视频</span>
      </div>
      <input 
        type="file" 
        ref="fileInput" 
        multiple 
        accept="image/jpeg,image/png,image/jpg,video/mp4" 
        style="display: none" 
        @change="handleFileSelect"
      />
    </div>
    
    <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

    <!-- Preview Modal -->
    <div v-if="previewUrl" class="preview-modal" @click="closePreview">
      <img :src="previewUrl" v-if="isImage(previewUrl)" />
      <video :src="previewUrl" v-else controls autoplay></video>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import axios from '../utils/axios' // Assuming axios is configured

const props = defineProps({
  modelValue: {
    type: String, // JSON string or comma separated
    default: ''
  },
  limit: {
    type: Number,
    default: 9
  }
})

const emit = defineEmits(['update:modelValue'])

const fileInput = ref(null)
const uploading = ref(false)
const progress = ref(0)
const errorMsg = ref('')
const previewUrl = ref(null)

// Parse modelValue to array
const mediaList = computed(() => {
  if (!props.modelValue) return []
  try {
    // Try JSON parse first
    if (props.modelValue.startsWith('[')) {
        return JSON.parse(props.modelValue)
    }
    // Fallback to comma separated
    return props.modelValue.split(',').filter(s => s)
  } catch (e) {
    return []
  }
})

const triggerSelect = () => {
  if (uploading.value) return
  fileInput.value.click()
}

const handleFileSelect = async (e) => {
  const files = Array.from(e.target.files)
  if (!files.length) return
  
  // Validation
  for (const file of files) {
    if (file.size > 50 * 1024 * 1024) {
      errorMsg.value = `文件 ${file.name} 超过50MB限制`
      return
    }
    if (!['image/jpeg', 'image/png', 'image/jpg', 'video/mp4'].includes(file.type)) {
      errorMsg.value = `文件 ${file.name} 格式不支持`
      return
    }
  }
  
  uploading.value = true
  errorMsg.value = ''
  progress.value = 0
  
  const uploadedUrls = []
  
  try {
    let completed = 0
    const total = files.length
    
    for (const file of files) {
      const formData = new FormData()
      formData.append('file', file)
      
      const res = await axios.post('/upload/file', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (progressEvent) => {
           // Simple progress logic for batch
           const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
           // This is per file, but we show overall roughly
           progress.value = Math.floor(((completed * 100) + percentCompleted) / total)
        }
      })
      
      if (res.code === 200 || res.url) {
        uploadedUrls.push(res.url || res.data.url) // Adapt to response format
      } else {
        throw new Error('Upload failed')
      }
      completed++
    }
    
    // Update modelValue
    const newList = [...mediaList.value, ...uploadedUrls]
    emit('update:modelValue', JSON.stringify(newList))
    
  } catch (e) {
    console.error(e)
    errorMsg.value = '上传失败，请重试'
  } finally {
    uploading.value = false
    fileInput.value.value = '' // Reset input
  }
}

const removeMedia = (index) => {
  const newList = [...mediaList.value]
  newList.splice(index, 1)
  emit('update:modelValue', JSON.stringify(newList))
}

const isImage = (url) => {
  return /\.(jpg|jpeg|png|gif|webp)$/i.test(url)
}

const isVideo = (url) => {
  return /\.(mp4|webm|mov)$/i.test(url)
}

const preview = (url) => {
  previewUrl.value = url
}

const closePreview = () => {
  previewUrl.value = null
}
</script>

<style scoped>
.media-upload {
  width: 100%;
}
.media-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}
.media-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #eee;
}
.media-item img, .media-item video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}
.media-actions {
  position: absolute;
  top: 0;
  right: 0;
  background: rgba(0,0,0,0.5);
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.delete-btn {
  color: white;
  border: none;
  background: none;
  font-size: 16px;
  line-height: 1;
  padding: 0;
  cursor: pointer;
}
.upload-trigger {
  width: 100px;
  height: 100px;
  border: 1px dashed #ccc;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #fafafa;
  transition: all 0.3s;
}
.upload-trigger:hover {
  border-color: #1890ff;
  color: #1890ff;
}
.normal-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 12px;
  color: #666;
}
.icon {
  font-size: 24px;
  margin-bottom: 4px;
}
.uploading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #1890ff;
}
.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 5px;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.error-msg {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 5px;
}
.preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.8);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview-modal img, .preview-modal video {
  max-width: 90%;
  max-height: 90%;
}
</style>
