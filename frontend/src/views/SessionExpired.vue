<template>
  <div class="session-expired-container">
    <el-card class="session-expired-card">
      <div class="icon-container">
        <el-icon class="expired-icon"><WarningFilled /></el-icon>
      </div>
      <h1 class="expired-title">登录已过期</h1>
      <p class="expired-message">您的登录状态已过期，请重新登录以继续使用系统</p>
      <el-button type="primary" size="large" @click="goToLogin" class="login-button">
        <el-icon><User /></el-icon>
        重新登录
      </el-button>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const goToLogin = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.session-expired-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.session-expired-card {
  width: 450px;
  text-align: center;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.icon-container {
  margin-bottom: 30px;
}

.expired-icon {
  font-size: 80px;
  color: #E6A23C;
}

.expired-title {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}

.expired-message {
  font-size: 16px;
  color: #606266;
  margin-bottom: 40px;
  line-height: 1.6;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 8px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}
</style>
