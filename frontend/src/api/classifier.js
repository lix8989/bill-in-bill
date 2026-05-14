import http from './http'

export async function syncClassifierCategories(payload) {
  const response = await http.post('/classifier/sync/categories', payload)
  return response.data
}

export async function fetchClassifierSyncLogs(params = {}) {
  const response = await http.get('/classifier/sync/logs', { params })
  return response.data
}

export async function fetchClassifierMappings() {
  const response = await http.get('/classifier/sync/categories/mappings')
  return response.data
}

export async function createClassifyTask(payload) {
  // 自动分类任务可能需要较长时间，设置5分钟超时
  const response = await http.post('/classifier/tasks/auto-classify', payload, {
    timeout: 300000, // 5分钟超时
  })
  return response.data
}

export async function fetchClassifyTasks() {
  const response = await http.get('/classifier/tasks')
  return response.data
}

export async function fetchClassifyTask(taskNo) {
  const response = await http.get('/classifier/tasks/' + taskNo)
  return response.data
}

export async function fetchLlmSettings() {
  const response = await http.get('/classifier/llm-settings')
  return response.data
}

export async function updateLlmSettings(settings) {
  const response = await http.put('/classifier/llm-settings', settings)
  return response.data
}

export async function fetchClassifyStats() {
  const response = await http.get('/classifier/tasks/stats')
  return response.data
}

export async function fetchClassifyPreview(params) {
  const response = await http.get('/classifier/tasks/preview', { params })
  return response.data
}

export async function resetAllCategories() {
  const response = await http.post('/classifier/tasks/reset')
  return response.data
}
