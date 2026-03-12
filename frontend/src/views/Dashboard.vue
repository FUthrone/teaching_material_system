<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalMaterials }}</div>
              <div class="stat-label">总资料数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon><Download /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalDownloads }}</div>
              <div class="stat-label">总下载次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCategories }}</div>
              <div class="stat-label">分类数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近上传的资料</span>
            </div>
          </template>
          <el-table :data="recentMaterials" style="width: 100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="fileName" label="文件名" />
            <el-table-column prop="uploadTime" label="上传时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近下载记录</span>
            </div>
          </template>
          <el-table :data="recentDownloads" style="width: 100%">
            <el-table-column prop="materialTitle" label="资料标题" />
            <el-table-column prop="userName" label="下载用户" />
            <el-table-column prop="downloadTime" label="下载时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMaterials } from '@/api/material'
import { getDownloadRecords } from '@/api/download'
import { getUsers } from '@/api/user'
import { getCategoryTree } from '@/api/category'

const stats = ref({
  totalMaterials: 0,
  totalDownloads: 0,
  totalUsers: 0,
  totalCategories: 0
})

const recentMaterials = ref([])
const recentDownloads = ref([])

const loadStats = async () => {
  try {
    const materialsRes = await getMaterials({ page: 1, size: 1 })
    stats.value.totalMaterials = materialsRes.data.total

    const downloadsRes = await getDownloadRecords({ page: 1, size: 1 })
    stats.value.totalDownloads = downloadsRes.data.total

    const usersRes = await getUsers({ page: 1, size: 1 })
    stats.value.totalUsers = usersRes.data.total

    const categoriesRes = await getCategoryTree()
    stats.value.totalCategories = categoriesRes.data.length

    const recentMaterialsRes = await getMaterials({ page: 1, size: 5 })
    recentMaterials.value = recentMaterialsRes.data.records.map(item => ({
      ...item,
      uploadTime: new Date(item.createTime).toLocaleString()
    }))

    const recentDownloadsRes = await getDownloadRecords({ page: 1, size: 5 })
    recentDownloads.value = recentDownloadsRes.data.records.map(item => ({
      materialTitle: item.materialId,
      userName: item.userId,
      downloadTime: new Date(item.createTime).toLocaleString()
    }))
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}

.stat-info {
  text-align: right;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  font-weight: bold;
  color: #303133;
}
</style>