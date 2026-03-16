<template>
  <div class="upload">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>上传教学资料</span>
        </div>
      </template>

      <el-form :model="uploadForm" :rules="rules" ref="uploadFormRef" label-width="100px">
        <el-form-item label="资料标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入资料标题" />
        </el-form-item>
        <el-form-item label="资料描述" prop="description">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入资料描述"
          />
        </el-form-item>
        <el-form-item label="资料分类" prop="categoryId">
          <el-cascader
            v-model="uploadForm.categoryId"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'categoryName', children: 'children', emitPath: false }"
            placeholder="请选择分类"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="上传文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持上传最大5GB的文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <div v-if="uploadProgress > 0" class="progress-container">
        <div class="progress-content">
          <div class="progress-header">
            <el-icon class="upload-icon"><Upload /></el-icon>
            <span class="progress-title">正在上传文件</span>
          </div>
          
          <el-progress 
            :percentage="uploadProgress" 
            :stroke-width="20"
            :show-text="false"
            :status="uploadProgress === 100 ? 'success' : ''"
          />
          
          <div class="progress-info">
            <div class="progress-percent">{{ uploadProgress }}%</div>
            <div class="progress-details">
              <div class="detail-item">
                <span class="detail-label">已上传:</span>
                <span class="detail-value">{{ uploadedSize }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">总大小:</span>
                <span class="detail-value">{{ totalSize }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">上传速度:</span>
                <span class="detail-value">{{ uploadSpeed }}</span>
              </div>
            </div>
          </div>
          
          <div class="progress-status">{{ uploadStatus }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { uploadMaterial } from '@/api/material'
import { getCategoryTree } from '@/api/category'
import { ElMessage } from 'element-plus'

const router = useRouter()

const uploadFormRef = ref(null)
const uploadRef = ref(null)
const loading = ref(false)
const categoryOptions = ref([])
const uploadProgress = ref(0)
const uploadStatus = ref('')
const uploadedSize = ref('0 B')
const totalSize = ref('0 B')
const uploadSpeed = ref('0 B/s')
const startTime = ref(0)
const lastLoaded = ref(0)

const uploadForm = reactive({
  title: '',
  description: '',
  categoryId: null,
  file: null
})

const rules = {
  title: [
    { required: true, message: '请输入资料标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入资料描述', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择资料分类', trigger: 'change' }
  ],
  file: [
    { required: true, message: '请上传文件', trigger: 'change' }
  ]
}

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categoryOptions.value = res.data
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const handleFileChange = (file) => {
  if (file.size > 5120 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过5GB')
    uploadRef.value.clearFiles()
    return
  }
  uploadForm.file = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const formatProgress = (percentage) => {
  return percentage === 100 ? '完成' : `${percentage}%`
}

const handleSubmit = async () => {
  await uploadFormRef.value.validate()
  
  if (!uploadForm.file) {
    ElMessage.error('请上传文件')
    return
  }

  loading.value = true
  uploadProgress.value = 0
  uploadStatus.value = '准备上传...'
  uploadedSize.value = '0 B'
  totalSize.value = formatFileSize(uploadForm.file.size)
  uploadSpeed.value = '0 B/s'
  startTime.value = Date.now()
  lastLoaded.value = 0
  
  try {
    const formData = new FormData()
    formData.append('title', uploadForm.title)
    formData.append('description', uploadForm.description)
    formData.append('categoryId', uploadForm.categoryId)
    formData.append('file', uploadForm.file)

    await uploadMaterial(formData, (progressEvent) => {
      const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      uploadProgress.value = percentCompleted
      uploadedSize.value = formatFileSize(progressEvent.loaded)
      
      const currentTime = Date.now()
      const timeDiff = (currentTime - startTime.value) / 1000
      const loadedDiff = progressEvent.loaded - lastLoaded.value
      
      if (timeDiff > 0) {
        const speed = loadedDiff / timeDiff
        uploadSpeed.value = formatFileSize(speed) + '/s'
      }
      
      lastLoaded.value = progressEvent.loaded
      startTime.value = currentTime
      
      uploadStatus.value = percentCompleted === 100 ? '上传完成！' : '上传中...'
    })
    
    uploadProgress.value = 100
    uploadStatus.value = '上传完成！'
    ElMessage.success('上传成功')
    
    setTimeout(() => {
      router.push('/app/materials')
    }, 1500)
  } catch (error) {
    uploadProgress.value = 0
    uploadStatus.value = ''
    ElMessage.error(error.message || '上传失败')
  } finally {
    loading.value = false
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const handleReset = () => {
  uploadFormRef.value.resetFields()
  uploadRef.value.clearFiles()
  uploadForm.file = null
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.upload {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-weight: bold;
  color: #303133;
}

.el-icon--upload {
  font-size: 67px;
  color: #c0c4cc;
  margin: 40px 0 16px;
}

.progress-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.progress-content {
  background: #fff;
  border-radius: 16px;
  padding: 40px 60px;
  width: 500px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.progress-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
}

.upload-icon {
  font-size: 32px;
  color: #667eea;
  margin-right: 12px;
}

.progress-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.progress-info {
  margin-top: 30px;
  text-align: center;
}

.progress-percent {
  font-size: 48px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 20px;
}

.progress-details {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.detail-item {
  text-align: center;
}

.detail-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.detail-value {
  display: block;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.progress-status {
  margin-top: 20px;
  text-align: center;
  font-size: 16px;
  color: #606266;
}

:deep(.el-progress-bar__outer) {
  background-color: #f0f2f5;
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>