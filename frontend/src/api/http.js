import axios from 'axios'

const defaultBaseURL =
  import.meta.env.VITE_API_BASE_URL ||
  (import.meta.env.DEV ? 'http://localhost:8080/api' : '/api')

const http = axios.create({
  baseURL: defaultBaseURL,
  timeout: 30000, // 默认30秒超时
})

export default http
