import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo as getUserInfoApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
  }

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const login = async (credentials) => {
    const res = await loginApi(credentials)
    setToken(res.data.token)
    setUserInfo(res.data.userInfo)
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
  }

  return {
    token,
    userInfo,
    setToken,
    clearToken,
    setUserInfo,
    login,
    register,
    getUserInfo,
    logout
  }
})