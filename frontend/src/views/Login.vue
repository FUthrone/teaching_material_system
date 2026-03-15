<template>
  <div class="login-container">
    <div class="login-left">
      <div class="system-info">
        <h1 class="system-title">高校教学资料管理系统</h1>
        <p class="system-subtitle">打造智慧教育，共享优质资源</p>
        
        <div class="features">
          <div class="feature-item">
            <el-icon class="feature-icon"><Document /></el-icon>
            <div class="feature-content">
              <h3>资料管理</h3>
              <p>支持多种格式文件上传，智能分类管理</p>
            </div>
          </div>
          
          <div class="feature-item">
            <el-icon class="feature-icon"><Folder /></el-icon>
            <div class="feature-content">
              <h3>分类体系</h3>
              <p>多级分类结构，快速定位所需资料</p>
            </div>
          </div>
          
          <div class="feature-item">
            <el-icon class="feature-icon"><Download /></el-icon>
            <div class="feature-content">
              <h3>下载追踪</h3>
              <p>完整的下载记录，数据统计分析</p>
            </div>
          </div>
          
          <div class="feature-item">
            <el-icon class="feature-icon"><User /></el-icon>
            <div class="feature-content">
              <h3>权限管理</h3>
              <p>灵活的角色权限，保障数据安全</p>
            </div>
          </div>
        </div>
        
        <div class="stats">
          <div class="stat-item">
            <div class="stat-number">1000+</div>
            <div class="stat-label">教学资料</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">500+</div>
            <div class="stat-label">活跃用户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">50+</div>
            <div class="stat-label">学科分类</div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="login-right">
      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <h2>欢迎登录</h2>
            <p>请输入您的账号信息</p>
          </div>
        </template>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
          </el-form-item>
          <el-form-item>
            <div class="login-footer">
              <span>还没有账号？</span>
              <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/app')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  color: #fff;
}

.system-info {
  max-width: 600px;
  text-align: center;
}

.system-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.system-subtitle {
  font-size: 24px;
  margin-bottom: 60px;
  opacity: 0.9;
}

.features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  margin-bottom: 60px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  text-align: left;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.15);
}

.feature-icon {
  font-size: 32px;
  margin-right: 15px;
  color: #fff;
}

.feature-content h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: bold;
}

.feature-content p {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}

.stats {
  display: flex;
  justify-content: center;
  gap: 60px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 16px;
  opacity: 0.8;
}

.login-right {
  width: 450px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.login-card {
  width: 100%;
  border: none;
  box-shadow: none;
  background: transparent;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 28px;
}

.card-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.login-footer {
  width: 100%;
  text-align: center;
}

:deep(.el-form-item__label) {
  font-weight: bold;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 16px;
  padding: 12px 0;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}
</style>