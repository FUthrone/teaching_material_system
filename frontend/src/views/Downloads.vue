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
        <el-table-column prop="materialTitle" label="资料标题" width="200" show-overflow-tooltip />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { getDownloadRecords } from '@/api/download'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const userRoleType = computed(() => userInfo.value?.roleType)

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
    const params = {
      page: pagination.page,
      size: pagination.size,
      userId: searchForm.userId,
      materialId: searchForm.materialId
    }
    
    console.log('加载下载记录 - 参数:', params)
    console.log('加载下载记录 - 用户信息:', userInfo.value)
    console.log('加载下载记录 - 用户ID:', userInfo.value?.id)
    console.log('加载下载记录 - 用户角色:', userRoleType.value)
    
    const res = await getDownloadRecords(params)
    
    console.log('加载下载记录 - 响应数据:', res.data)
    console.log('加载下载记录 - 记录总数:', res.data.total)
    
    if (userRoleType.value === 'STUDENT') {
      console.log('学生角色，过滤记录')
      console.log('当前用户ID:', userInfo.value?.id)
      console.log('记录数据:', res.data.records)
      const currentUserId = Number(userInfo.value?.id)
      records.value = res.data.records.filter(record => {
        const recordUserId = Number(record.userId)
        console.log('比较:', recordUserId, '===', currentUserId, '结果:', recordUserId === currentUserId)
        return recordUserId === currentUserId
      })
      console.log('过滤后的记录数:', records.value.length)
    } else {
      console.log('管理员/教师角色，显示所有记录')
      records.value = res.data.records
    }
    
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载下载记录失败:', error)
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