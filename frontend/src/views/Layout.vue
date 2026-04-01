<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">
        <h3>教学资料管理</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/app/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/app/materials">
          <el-icon><Document /></el-icon>
          <span>资料列表</span>
        </el-menu-item>
        <el-menu-item index="/app/personal-files">
          <el-icon><FolderOpened /></el-icon>
          <span>个人文件</span>
        </el-menu-item>
        <el-menu-item index="/app/upload">
          <el-icon><Upload /></el-icon>
          <span>资料上传</span>
        </el-menu-item>
        <el-menu-item index="/app/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/app/downloads">
          <el-icon><Download /></el-icon>
          <span>下载记录</span>
        </el-menu-item>
        <el-menu-item index="/app/users" v-if="canAccess('user:manage')">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/app/roles" v-if="canAccess('role:manage')">
          <el-icon><UserFilled /></el-icon>
          <span>角色管理</span>
        </el-menu-item>
        <el-menu-item index="/app/permissions" v-if="canAccess('permission:manage')">
          <el-icon><Lock /></el-icon>
          <span>权限管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/app' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <el-icon><User /></el-icon>
                {{ userInfo?.realName || '用户' }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => {
  const info = userStore.userInfo
  console.log('Layout computed userInfo:', info)
  console.log('Layout userInfo类型:', typeof info)
  console.log('Layout userInfo是否为Proxy:', info && typeof info === 'object')
  return info
})
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => {
  const titles = {
    '/app/dashboard': '首页',
    '/app/materials': '资料列表',
    '/app/upload': '资料上传',
    '/app/categories': '分类管理',
    '/app/downloads': '下载记录',
    '/app/users': '用户管理',
    '/app/roles': '角色管理',
    '/app/permissions': '权限管理'
  }
  return titles[route.path] || '首页'
})

const hasPermission = (permission) => {
  console.log('权限检查:', permission, '用户角色:', userInfo.value?.roleType)
  return true
}

const canAccess = (permission) => {
  console.log('访问控制检查:', permission)
  console.log('用户信息:', userInfo.value)
  console.log('用户角色:', userInfo.value?.roleType)
  
  if (!userInfo.value) {
    console.log('用户信息为空，拒绝访问')
    return false
  }
  
  const roleType = userInfo.value?.roleType
  console.log('角色类型:', roleType)
  
  if (roleType === 'ADMIN') {
    console.log('管理员角色，允许所有访问')
    return true
  }
  
  if (permission === 'user:manage' && (roleType === 'TEACHER' || roleType === 'ADMIN')) {
    console.log('允许用户管理访问')
    return true
  }
  
  if (permission === 'role:manage' && roleType === 'ADMIN') {
    console.log('允许角色管理访问')
    return true
  }
  
  if (permission === 'permission:manage' && roleType === 'ADMIN') {
    console.log('允许权限管理访问')
    return true
  }
  
  console.log('拒绝访问:', permission)
  return false
}

const handleCommand = (command) => {
  console.log('菜单命令:', command)
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('退出成功')
    router.push('/login')
  } else if (command === 'clearCache') {
    userStore.clearToken()
    ElMessage.success('缓存已清除，请重新登录')
    router.push('/login')
  }
}

onMounted(async () => {
  console.log('Layout组件已挂载')
  console.log('当前用户信息:', userInfo.value)
  console.log('用户角色:', userInfo.value?.roleType)
  if (!userInfo.value) {
    try {
      await userStore.getUserInfo()
      console.log('获取用户信息成功')
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: #f5f7fa;
}

.el-aside {
  background: #304156;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 2px solid #004085;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.el-header {
  background: #304156;
  border-bottom: 2px solid #004085;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 15px 20px;
  height: 100%;
  box-sizing: border-box;
}

.breadcrumb {
  font-weight: bold;
  font-size: 20px;
  color: #fff;
  text-align: left;
  line-height: 1.5;
}

:deep(.el-breadcrumb__item) {
  font-size: 20px;
}

:deep(.el-breadcrumb__inner) {
  color: #fff !important;
  font-weight: bold !important;
}

:deep(.el-breadcrumb__separator) {
  color: #fff !important;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #fff;
  font-weight: bold;
  font-size: 16px;
}

.el-dropdown-link:hover {
  color: #409EFF;
}

.el-main {
  background: #fff;
  padding: 20px;
}

.el-menu {
  background: transparent;
  border: none;
}

.el-menu-item {
  color: #fff;
  font-weight: bold;
  font-size: 16px;
}

.el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}
</style>