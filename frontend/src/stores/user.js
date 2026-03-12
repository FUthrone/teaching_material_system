import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo as getUserInfoApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const permissions = ref([])

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
  }

  const setUserInfo = (info) => {
    console.log('setUserInfo被调用，传入的info:', info)
    console.log('setUserInfo - info对象:', JSON.stringify(info))
    
    userInfo.value = info
    console.log('setUserInfo执行后，userInfo.value:', userInfo.value)
    console.log('setUserInfo - userInfo.value === info:', userInfo.value === info)
    
    if (userInfo.value === null) {
      console.error('setUserInfo失败，userInfo仍然为null')
    }
  }

  const setPermissions = (perms) => {
    permissions.value = perms
  }

  const hasPermission = (permission) => {
    if (!permission) return true
    return permissions.value.includes(permission)
  }

  const login = async (credentials) => {
    console.log('=== 登录方法被调用 ===')
    console.log('登录参数:', credentials)
    const res = await loginApi(credentials)
    console.log('登录响应:', res)
    console.log('登录响应数据:', res.data)
    console.log('登录响应token:', res.data?.token)
    console.log('登录响应userInfo:', res.data?.userInfo)
    
    setToken(res.data.token)
    setUserInfo(res.data.userInfo)
    
    console.log('登录方法执行完成')
    console.log('store中的token:', token.value)
    console.log('store中的userInfo:', userInfo.value)
    
    return res
  }

  const register = async (data) => {
    return await registerApi(data)
  }

  const getUserInfo = async () => {
    const res = await getUserInfoApi()
    setUserInfo(res.data)
    return res
  }

  const logout = () => {
    clearToken()
    userInfo.value = null
    permissions.value = []
  }

  return {
    token,
    userInfo,
    permissions,
    setToken,
    clearToken,
    setUserInfo,
    setPermissions,
    hasPermission,
    login,
    register,
    getUserInfo,
    logout
  }
})