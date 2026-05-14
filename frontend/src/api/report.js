import http from './http'

/**
 * 获取年度账本报告
 * @param {number} year 年份
 * @returns {Promise} 年度报告数据
 */
export async function fetchAnnualReport(year) {
  const response = await http.get(`/reports/annual/${year}`)
  return response.data
}

/**
 * 获取可用年份列表
 * @returns {Promise} 年份列表
 */
export async function fetchAvailableYears() {
  const response = await http.get('/reports/years')
  return response.data
}

/**
 * 生成年度报告PDF
 * @param {number} year 年份
 * @returns {Promise} PDF文件数据
 */
export async function fetchAnnualReportPdf(year) {
  const response = await http.get(`/reports/annual/${year}/pdf`, {
    responseType: 'blob'
  })
  return response.data
}
