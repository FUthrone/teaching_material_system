import request from '@/utils/request'

export const getUsers = (params) => {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

export const getUser = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export const createUser = (data) => {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export const updateUser = (id, data) => {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

export const deleteUser = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export const assignRole = (userId, roleId) => {
  return request({
    url: `/user/${userId}/role/${roleId}`,
    method: 'post'
  })
}