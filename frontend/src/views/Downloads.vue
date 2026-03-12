<template>
  <div class="downloads">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>下载记录</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="资料ID">
          <el-input v-model="searchForm.materialId" placeholder="请输入资料ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="records" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="materialId" label="资料ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="downloadIp" label="下载IP" width="150" />
        <el-table-column prop="createTime" label="下载时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRecords"
        @current-change="loadRecords"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDownloadRecords } from '@/api/download'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const records = ref([])

const searchForm = reactive({
  userId: null,
  materialId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getDownloadRecords({
      page: pagination.page,
      size: pagination.size,
      userId: searchForm.userId,
      materialId: searchForm.materialId
    })
    records.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载下载记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadRecords()
}

const handleReset = () => {
  searchForm.userId = null
  searchForm.materialId = null
  pagination.page = 1
  loadRecords()
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString()
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.downloads {
  height: 100%;
}

.card-header {
  font-weight: bold;
  color: #303133;
}

.search-form {
  margin-bottom: 20px;
}
</style>