import request from '@/utils/request'

export const createShare = (materialId, password, expireDays, maxDownloads) => {
  return request({
    url: '/share/create',
    method: 'post',
    params: {
      materialId,
      password,
      expireDays,
      maxDownloads
    }
  })
}

export const getShareInfo = (shareCode, password) => {
  return request({
    url: `/share/${shareCode}`,
    method: 'get',
    params: {
      password
    }
  })
}

export const downloadShare = (shareCode, password) => {
  return request({
    url: `/share/download/${shareCode}`,
    method: 'get',
    responseType: 'blob',
    params: {
      password
    }
  })
}
