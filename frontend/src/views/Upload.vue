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
                支持上传最大100MB的文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
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
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过100MB')
    uploadRef.value.clearFiles()
    return
  }
  uploadForm.file = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleSubmit = async () => {
  await uploadFormRef.value.validate()
  
  if (!uploadForm.file) {
    ElMessage.error('请上传文件')
    return
  }

  loading.value = true
  try {
    const formData = new FormData()
    formData.append('title', uploadForm.title)
    formData.append('description', uploadForm.description)
    formData.append('categoryId', uploadForm.categoryId)
    formData.append('file', uploadForm.file)

    await uploadMaterial(formData)
    ElMessage.success('上传成功')
    router.push('/app/materials')
  } catch (error) {
    ElMessage.error(error.message || '上传失败')
  } finally {
    loading.value = false
  }
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

.el-upload__text {
  color: #606266;
  font-size: 14px;
}

.el-upload__text em {
  color: #409EFF;
  font-style: normal;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 7px;
}
</style>