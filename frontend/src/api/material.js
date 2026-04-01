import request from '@/utils/request'

export const getMaterials = (params) => {
  return request({
    url: '/material/list',
    method: 'get',
    params
  })
}

export const getPersonalFiles = (params) => {
  return request({
    url: '/material/personal',
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

export const uploadMaterial = (formData, onUploadProgress) => {
  return request({
    url: '/material/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onUploadProgress
  })
}

export const deleteMaterial = (id) => {
  return request({
    url: `/material/${id}`,
    method: 'delete'
  })
}

export const downloadMaterial = (id, fileName, onDownloadProgress) => {
  return request({
    url: `/material/download/${id}`,
    method: 'get',
    responseType: 'blob',
    onDownloadProgress: onDownloadProgress
  }).then(response => {
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  })
}

export const getMaterialsByCategory = (categoryId) => {
  return request({
    url: `/material/category/${categoryId}`,
    method: 'get'
  })
}