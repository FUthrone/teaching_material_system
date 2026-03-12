import request from '@/utils/request'

export const getPermissions = (params) => {
  return request({
    url: '/permission/list',
    method: 'get',
    params
  })
}

export const getPermission = (id) => {
  return request({
    url: `/permission/${id}`,
    method: 'get'
  })
}

export const createPermission = (data) => {
  return request({
    url: '/permission',
    method: 'post',
    data
  })
}

export const updatePermission = (id, data) => {
  return request({
    url: `/permission/${id}`,
    method: 'put',
    data
  })
}

export const deletePermission = (id) => {
  return request({
    url: `/permission/${id}`,
    method: 'delete'
  })
}

export const assignPermissions = (roleId, permissionIds) => {
  return request({
    url: `/permission/role/${roleId}/assign`,
    method: 'post',
    data: permissionIds
  })
}