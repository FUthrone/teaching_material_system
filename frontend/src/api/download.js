import request from '@/utils/request'

export const getDownloadRecords = (params) => {
  return request({
    url: '/download/records',
    method: 'get',
    params
  })
}

export const getDownloadCountByMaterial = (materialId) => {
  return request({
    url: `/download/count/material/${materialId}`,
    method: 'get'
  })
}

export const getDownloadCountByUser = (userId) => {
  return request({
    url: `/download/count/user/${userId}`,
    method: 'get'
  })
}