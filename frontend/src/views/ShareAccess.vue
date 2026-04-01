<template>
  <div class="share-access">
    <el-card class="share-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-icon class="share-icon"><Share /></el-icon>
          <span>文件分享</span>
        </div>
      </template>

      <div v-if="error" class="error-container">
        <el-result
          icon="error"
          :title="error"
        >
          <template #extra>
            <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
          </template>
        </el-result>
      </div>

      <div v-else-if="needPassword && !authenticated" class="password-container">
        <el-form @submit.prevent="handlePasswordSubmit">
          <el-form-item label="访问密码">
            <el-input
              v-model="password"
              type="password"
              placeholder="请输入访问密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit" :loading="verifying">
              验证
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-else-if="materialInfo" class="material-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="文件名称">
            {{ materialInfo.fileName }}
          </el-descriptions-item>
          <el-descriptions-item label="文件大小">
            {{ formatFileSize(materialInfo.fileSize) }}
          </el-descriptions-item>
          <el-descriptions-item label="资料标题">
            {{ materialInfo.title }}
          </el-descriptions-item>
          <el-descriptions-item label="资料描述">
            {{ materialInfo.description }}
          </el-descriptions-item>
          <el-descriptions-item label="下载次数">
            {{ materialInfo.downloadCount }}
          </el-descriptions-item>
          <el-descriptions-item label="分享有效期" v-if="materialInfo.expireTime">
            {{ formatDateTime(materialInfo.expireTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="剩余下载次数" v-if="materialInfo.maxDownloads > 0">
            {{ materialInfo.maxDownloads - materialInfo.currentDownloads }} 次
          </el-descriptions-item>
        </el-descriptions>

        <div class="download-section">
          <el-button
            type="primary"
            size="large"
            @click="handleDownload"
            :loading="downloading"
          >
            <el-icon><Download /></el-icon>
            下载文件
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getShareInfo, downloadShare } from '@/api/share'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(true)
const error = ref('')
const needPassword = ref(false)
const authenticated = ref(false)
const password = ref('')
const verifying = ref(false)
const downloading = ref(false)
const materialInfo = ref(null)
const shareCode = ref('')

const loadShareInfo = async () => {
  shareCode.value = route.params.code
  if (!shareCode.value) {
    error.value = '分享链接无效'
    loading.value = false
    return
  }

  try {
    const res = await getShareInfo(shareCode.value)
    
    if (res.code === 200) {
      materialInfo.value = res.data
      needPassword.value = res.data.needPassword
      authenticated.value = !res.data.needPassword
    } else {
      error.value = res.message || '获取分享信息失败'
    }
  } catch (err) {
    error.value = err.message || '分享链接不存在或已失效'
  } finally {
    loading.value = false
  }
}

const handlePasswordSubmit = async () => {
  if (!password.value) {
    ElMessage.warning('请输入访问密码')
    return
  }

  verifying.value = true
  try {
    const res = await getShareInfo(shareCode.value, password.value)
    
    if (res.code === 200) {
      authenticated.value = true
      materialInfo.value = res.data
      ElMessage.success('验证成功')
    } else {
      ElMessage.error(res.message || '密码错误')
    }
  } catch (err) {
    ElMessage.error(err.message || '验证失败')
  } finally {
    verifying.value = false
  }
}

const handleDownload = async () => {
  downloading.value = true
  try {
    const response = await downloadShare(shareCode.value, password.value)
    
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.download = materialInfo.value.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
    
    if (materialInfo.value.maxDownloads > 0) {
      materialInfo.value.currentDownloads++
    }
  } catch (err) {
    ElMessage.error(err.message || '下载失败')
  } finally {
    downloading.value = false
  }
}

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const formatDateTime = (dateString) => {
  return new Date(dateString).toLocaleString()
}

onMounted(() => {
  loadShareInfo()
})
</script>

<style scoped>
.share-access {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.share-card {
  width: 100%;
  max-width: 600px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
}

.share-icon {
  font-size: 28px;
  margin-right: 10px;
  color: #667eea;
}

.error-container {
  padding: 20px 0;
}

.password-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px 0;
}

.material-info {
  padding: 20px 0;
}

.download-section {
  margin-top: 30px;
  text-align: center;
}

.download-section .el-button {
  width: 200px;
}

:deep(.el-descriptions__label) {
  font-weight: bold;
  width: 120px;
}
</style>
