import http from './http'

export async function fetchDashboard(params = {}) {
  const response = await http.get('/dashboard', { params })
  return response.data
}
