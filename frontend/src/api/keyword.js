import http from './http'

export async function fetchKeywordRules() {
  const response = await http.get('/classifier/keyword-rules')
  return response.data
}

export async function saveKeywordCategory(payload) {
  const response = await http.post('/classifier/keyword-rules/save-category', payload)
  return response.data
}

export async function deleteKeywordRule(id) {
  const response = await http.delete('/classifier/keyword-rules/' + id)
  return response.data
}

export async function refreshKeywordRules() {
  const response = await http.post('/classifier/keyword-rules/refresh')
  return response.data
}

export async function deleteKeywordCategory(categoryCode) {
  const response = await http.post('/classifier/keyword-rules/delete-category', { categoryCode })
  return response.data
}
