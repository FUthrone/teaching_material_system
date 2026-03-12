import request from '@/utils/request'

export const getRoles = (params) => {
  return request({
    url: '/role/list',
    method: 'get',
    params
  })
}

export const getRole = (id) => {
  return request({
    url: `/role/${id}`,
    method: 'get'
  })
}

export const createRole = (data) => {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

export const updateRole = (id, data) => {
  return request({
    url: `/role/${id}`,
    method: 'put',
    data
  })
}

export const deleteRole = (id) => {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}