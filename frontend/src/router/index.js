import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/session-expired',
    name: 'SessionExpired',
    component: () => import('@/views/SessionExpired.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/app',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/app/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'materials',
        name: 'Materials',
        component: () => import('@/views/Materials.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'upload',
        name: 'Upload',
        component: () => import('@/views/Upload.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/Categories.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'downloads',
        name: 'Downloads',
        component: () => import('@/views/Downloads.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { requiresAuth: true, permission: 'user:manage' }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/Roles.vue'),
        meta: { requiresAuth: true, permission: 'role:manage' }
      },
      {
        path: 'permissions',
        name: 'Permissions',
        component: () => import('@/views/Permissions.vue'),
        meta: { requiresAuth: true, permission: 'permission:manage' }
      },
      {
        path: '/404',
        name: 'Error',
        component: () => import('@/views/Error.vue'),
        meta: { requiresAuth: false }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  console.log('路由守卫 - 目标路由:', to.path)
  console.log('路由守卫 - 需要认证:', to.meta.requiresAuth)
  console.log('路由守卫 - 需要权限:', to.meta.permission)
  console.log('路由守卫 - 当前用户:', userStore.userInfo)
  console.log('路由守卫 - 当前token:', userStore.token)
  
  // 未登录且需要认证，重定向到登录页
  if (to.meta.requiresAuth && !userStore.token) {
    console.log('路由守卫 - 未登录，重定向到登录页')
    next('/login')
    return
  }
  
  // 已登录用户访问登录/注册页，重定向到首页
  if ((to.path === '/login' || to.path === '/register') && userStore.token) {
    console.log('路由守卫 - 已登录，重定向到首页')
    next('/app')
    return
  }
  
  // 如果存在token但userInfo为空，先获取用户信息
  if (userStore.token && !userStore.userInfo) {
    console.log('路由守卫 - 存在token但userInfo为空，先获取用户信息')
    try {
      await userStore.getUserInfo()
      console.log('路由守卫 - 用户信息获取成功:', userStore.userInfo)
    } catch (error) {
      console.error('路由守卫 - 获取用户信息失败:', error)
      console.log('路由守卫 - JWT可能已过期，跳转登录过期页面')
      // 清除token
      userStore.clearToken()
      // 跳转到登录过期页面
      next('/session-expired')
      return
    }
  }
  
  // 通过所有检查，允许访问
  console.log('路由守卫 - 允许访问')
  next()
})

export default router