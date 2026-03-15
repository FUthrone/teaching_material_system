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

const handleDownload = async (row) => {
  try {
    await downloadMaterial(row.id, row.fileName)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
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
</style>