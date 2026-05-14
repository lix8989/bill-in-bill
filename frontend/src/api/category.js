import http from './http'

export async function fetchCategories() {
  const response = await http.get('/categories')
  return response.data
}

export async function createCategory(payload) {
  const response = await http.post('/categories', payload)
  return response.data
}

export async function updateCategory(id, payload) {
  const response = await http.put('/categories/' + id, payload)
  return response.data
}

export async function deleteCategory(id) {
  const response = await http.delete('/categories/' + id)
  return response.data
}
