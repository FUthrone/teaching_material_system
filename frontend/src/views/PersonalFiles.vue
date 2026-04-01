<template>
  <div class="personal-files">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人文件</span>
          <el-button type="primary" @click="showUploadDialog">上传文件</el-button>
        </div>
      </template>

      <el-table :data="files" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="fileName" label="文件名" width="200" />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载次数" width="100" />
        <el-table-column prop="createTime" label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
            <el-button type="success" link @click="handleShare(row)">分享</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadFiles"
        @current-change="loadFiles"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog
      v-model="uploadDialogVisible"
      title="上传个人文件"
      width="600px"
    >
      <el-form :model="uploadForm" :rules="rules" ref="uploadFormRef" label-width="100px">
        <el-form-item label="文件标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入文件标题" />
        </el-form-item>
        <el-form-item label="文件描述" prop="description">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入文件描述"
          />
        </el-form-item>
        <el-form-item label="文件分类" prop="categoryId">
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
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpload" :loading="uploadLoading">
            上传
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="shareDialogVisible"
      title="创建分享链接"
      width="500px"
    >
      <el-form :model="shareForm" label-width="100px">
        <el-form-item label="文件名称">
          <el-input v-model="currentShareFile.title" disabled />
        </el-form-item>
        
        <el-form-item label="访问密码">
          <el-switch v-model="shareForm.needPassword" />
          <el-input
            v-if="shareForm.needPassword"
            v-model="shareForm.password"
            placeholder="请输入访问密码"
            style="width: 200px; margin-left: 10px"
          />
        </el-form-item>
        
        <el-form-item label="有效期">
          <el-select v-model="shareForm.expireDays" placeholder="请选择有效期">
            <el-option label="1天" :value="1" />
            <el-option label="7天" :value="7" />
            <el-option label="30天" :value="30" />
            <el-option label="永久有效" :value="-1" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="下载限制">
          <el-input-number
            v-model="shareForm.maxDownloads"
            :min="-1"
            :max="10000"
          />
          <span style="margin-left: 10px; color: #909399; font-size: 12px">
            -1表示无限制
          </span>
        </el-form-item>
      </el-form>

      <div v-if="shareLink" style="margin-top: 20px">
        <el-divider />
        <el-form-item label="分享链接">
          <el-input v-model="shareLink" readonly>
            <template #append>
              <el-button @click="copyShareLink">复制链接</el-button>
            </template>
          </el-input>
        </el-form-item>
        <div v-if="shareForm.needPassword" style="margin-left: 100px; color: #909399; font-size: 12px">
          访问密码: {{ shareForm.password }}
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shareDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createShareLink" :loading="shareLoading">
            创建分享
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPersonalFiles, uploadMaterial, deleteMaterial, downloadMaterial } from '@/api/material'
import { getCategoryTree } from '@/api/category'
import { createShare } from '@/api/share'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const files = ref([])
const categoryOptions = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const uploadDialogVisible = ref(false)
const uploadLoading = ref(false)
const uploadFormRef = ref(null)
const uploadRef = ref(null)
const uploadForm = reactive({
  title: '',
  description: '',
  categoryId: null,
  file: null
})

const rules = {
  title: [
    { required: true, message: '请输入文件标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入文件描述', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择文件分类', trigger: 'change' }
  ],
  file: [
    { required: true, message: '请上传文件', trigger: 'change' }
  ]
}

const shareDialogVisible = ref(false)
const shareLoading = ref(false)
const shareLink = ref('')
const currentShareFile = ref({})
const shareForm = reactive({
  needPassword: false,
  password: '',
  expireDays: 7,
  maxDownloads: -1
})

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categoryOptions.value = res.data
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadFiles = async () => {
  loading.value = true
  try {
    const res = await getPersonalFiles({
      page: pagination.page,
      size: pagination.size
    })
    files.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

const showUploadDialog = () => {
  uploadForm.title = ''
  uploadForm.description = ''
  uploadForm.categoryId = null
  uploadForm.file = null
  uploadDialogVisible.value = true
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

const handleUpload = async () => {
  await uploadFormRef.value.validate()
  
  if (!uploadForm.file) {
    ElMessage.error('请上传文件')
    return
  }

  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('title', uploadForm.title)
    formData.append('description', uploadForm.description)
    formData.append('categoryId', uploadForm.categoryId)
    formData.append('file', uploadForm.file)
    formData.append('isPrivate', 1)

    await uploadMaterial(formData)
    
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    loadFiles()
  } catch (error) {
    ElMessage.error(error.message || '上传失败')
  } finally {
    uploadLoading.value = false
  }
}

const handleDownload = async (row) => {
  try {
    await downloadMaterial(row.id, row.fileName)
    ElMessage.success('下载成功')
    loadFiles()
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const handleShare = (row) => {
  currentShareFile.value = row
  shareForm.needPassword = false
  shareForm.password = ''
  shareForm.expireDays = 7
  shareForm.maxDownloads = -1
  shareLink.value = ''
  shareDialogVisible.value = true
}

const createShareLink = async () => {
  shareLoading.value = true
  try {
    const password = shareForm.needPassword ? shareForm.password : null
    const res = await createShare(
      currentShareFile.value.id,
      password,
      shareForm.expireDays,
      shareForm.maxDownloads
    )
    
    const baseUrl = window.location.origin
    shareLink.value = `${baseUrl}/share/${res.data.shareCode}`
    
    ElMessage.success('分享链接创建成功')
  } catch (error) {
    ElMessage.error(error.message || '创建分享链接失败')
  } finally {
    shareLoading.value = false
  }
}

const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该文件吗？', '提示', {
      type: 'warning'
    })
    await deleteMaterial(row.id)
    ElMessage.success('删除成功')
    loadFiles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString()
}

onMounted(() => {
  loadCategories()
  loadFiles()
})
</script>

<style scoped>
.personal-files {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-icon--upload {
  font-size: 67px;
  color: #c0c4cc;
  margin: 40px 0 16px;
}
</style>
