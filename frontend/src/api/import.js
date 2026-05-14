import http from './http'

export function importWechatBill(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/imports/wechat', formData, {
    timeout: 120000,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function importAlipayBill(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/imports/alipay', formData, {
    timeout: 120000,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export async function fetchImportHistory() {
  const response = await http.get('/imports/history')
  return response.data
}
