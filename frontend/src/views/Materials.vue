<template>
  <div class="materials">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教学资料列表</span>
          <el-button type="primary" @click="$router.push('/app/upload')">上传资料</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类">
          <el-cascader
            v-model="searchForm.categoryId"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'categoryName', children: 'children', emitPath: false }"
            clearable
            placeholder="请选择分类"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 下载进度对话框 -->
      <el-dialog
        v-model="downloading"
        title="下载进度"
        width="450px"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
      >
        <div class="download-progress">
          <div class="file-info">
            <el-icon class="file-icon"><Document /></el-icon>
            <span class="file-name">{{ currentDownloadFile }}</span>
          </div>
          
          <el-progress 
            :percentage="downloadProgress" 
            :stroke-width="20"
            :text-inside="true"
          />
          
          <div class="progress-details">
            <div class="detail-item">
              <span class="detail-label">下载进度:</span>
              <span class="detail-value">{{ downloadProgress }}%</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">下载速度:</span>
              <span class="detail-value">{{ downloadSpeed }}</span>
            </div>
          </div>
          
          <p class="status-text">{{ downloadStatus }}</p>
        </div>
      </el-dialog>

      <el-table :data="materials" v-loading="loading" style="width: 100%">
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="canDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadMaterials"
        @current-change="loadMaterials"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMaterials, downloadMaterial, deleteMaterial } from '@/api/material'
import { getCategoryTree } from '@/api/category'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const loading = ref(false)
const materials = ref([])
const categoryOptions = ref([])

const searchForm = reactive({
  categoryId: null,
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categoryOptions.value = res.data
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadMaterials = async () => {
  loading.value = true
  try {
    const res = await getMaterials({
      page: pagination.page,
      size: pagination.size,
      categoryId: searchForm.categoryId,
      keyword: searchForm.keyword
    })
    materials.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载资料列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadMaterials()
}

const handleReset = () => {
  searchForm.categoryId = null
  searchForm.keyword = ''
  pagination.page = 1
  loadMaterials()
}

const downloadProgress = ref(0)
const downloadStatus = ref('')
const downloading = ref(false)
const currentDownloadFile = ref('')
const downloadSpeed = ref('0 B/s')
const downloadStartTime = ref(0)
const downloadLastLoaded = ref(0)

const handleDownload = async (row) => {
  try {
    downloading.value = true
    downloadProgress.value = 0
    downloadStatus.value = '准备下载...'
    currentDownloadFile.value = row.fileName
    downloadSpeed.value = '0 B/s'
    downloadStartTime.value = Date.now()
    downloadLastLoaded.value = 0
    
    const totalSize = row.fileSize || 0
    
    await downloadMaterial(row.id, row.fileName, (progressEvent) => {
      const loaded = progressEvent.loaded || 0
      const total = progressEvent.total || totalSize
      
      let percentCompleted = 0
      if (total > 0) {
        percentCompleted = Math.round((loaded * 100) / total)
      }
      
      downloadProgress.value = percentCompleted
      
      const currentTime = Date.now()
      const timeDiff = (currentTime - downloadStartTime.value) / 1000
      const loadedDiff = loaded - downloadLastLoaded.value
      
      if (timeDiff > 0 && loadedDiff > 0) {
        const speed = loadedDiff / timeDiff
        downloadSpeed.value = formatFileSize(speed) + '/s'
      }
      
      downloadLastLoaded.value = loaded
      downloadStartTime.value = currentTime
      
      downloadStatus.value = `已下载 ${formatFileSize(loaded)} / ${formatFileSize(total)}`
    })
    
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  } finally {
    downloading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该资料吗？', '提示', {
      type: 'warning'
    })
    await deleteMaterial(row.id)
    ElMessage.success('删除成功')
    loadMaterials()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const canDelete = (row) => {
  return userStore.userInfo?.roleType === 'ADMIN' || row.uploadUser === userStore.userInfo?.id
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
  loadMaterials()
})
</script>

<style scoped>
.materials {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.download-progress {
  text-align: center;
  padding: 10px 0;
}

.download-progress .file-info {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 25px;
}

.download-progress .file-icon {
  font-size: 24px;
  color: #667eea;
  margin-right: 10px;
}

.download-progress .file-name {
  font-weight: bold;
  font-size: 16px;
  word-break: break-all;
  color: #303133;
}

.download-progress .progress-details {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
  margin-bottom: 15px;
}

.download-progress .detail-item {
  text-align: center;
}

.download-progress .detail-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.download-progress .detail-value {
  display: block;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.download-progress .status-text {
  margin-top: 15px;
  color: #909399;
  font-size: 14px;
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>