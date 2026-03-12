import request from '@/utils/request'

export const getMaterials = (params) => {
  return request({
    url: '/material/list',
    method: 'get',
    params
  })
}

export const getMaterial = (id) => {
  return request({
    url: `/material/${id}`,
    method: 'get'
  })
}

export const uploadMaterial = (formData) => {
  return request({
    url: '/material/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const deleteMaterial = (id) => {
  return request({
    url: `/material/${id}`,
    method: 'delete'
  })
}

export const downloadMaterial = (id) => {
  return `/api/material/download/${id}`
}

export const getMaterialsByCategory = (categoryId) => {
  return request({
    url: `/material/category/${categoryId}`,
    method: 'get'
  })
}