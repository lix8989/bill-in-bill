import http from './http'

export async function fetchBills(params = {}) {
  const response = await http.get('/bills', { params })
  return response.data
}

export async function createBill(payload) {
  const response = await http.post('/bills', payload)
  return response.data
}

export async function updateBillCategory(id, categoryId) {
  const response = await http.put('/bills/' + id + '/category', { categoryId })
  return response.data
}

export async function batchConfirmBillCategory(ids) {
  const response = await http.post('/bills/batch-confirm-category', { ids })
  return response.data
}

export async function updateBillSettlement(id, settlementIncluded) {
  const response = await http.put('/bills/' + id + '/settlement', { settlementIncluded })
  return response.data
}

export async function countByMatch(payload) {
  const response = await http.post('/bills/count-by-match', payload)
  return response.data
}

export async function batchUpdateByMatch(payload) {
  const response = await http.post('/bills/batch-update-by-match', payload)
  return response.data
}

export async function fetchBillStats(params = {}) {
  const response = await http.get('/bills/stats', { params })
  return response.data
}
