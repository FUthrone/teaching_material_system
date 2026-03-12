import request from '@/utils/request'

export const getCategoryTree = () => {
  return request({
    url: '/category/tree',
    method: 'get'
  })
}

export const getCategory = (id) => {
  return request({
    url: `/category/${id}`,
    method: 'get'
  })
}

export const createCategory = (data) => {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

export const updateCategory = (id, data) => {
  return request({
    url: `/category/${id}`,
    method: 'put',
    data
  })
}

export const deleteCategory = (id) => {
  return request({
    url: `/category/${id}`,
    method: 'delete'
  })
}