<template>
  <div class="annual-report-container">
    <!-- 年份选择 -->
    <div class="year-selector" v-if="!currentReport && !loading">
      <h2 class="selector-title">年度账本</h2>
      <p class="selector-subtitle">选择年份查看您的年度财务报告</p>

      <div class="year-cards" v-if="availableYears.length > 0">
        <div
          v-for="year in availableYears"
          :key="year"
          class="year-card"
          @click="loadReport(year)"
        >
          <div class="year-card-inner">
            <span class="year-number">{{ year }}</span>
            <span class="year-label">年度报告</span>
          </div>
          <div class="year-card-seal">戳</div>
        </div>
      </div>

      <el-empty v-else description="暂无账单数据" />
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-if="loading">
      <div class="ink-loader">
        <div class="loader-circle"></div>
        <div class="loader-text">正在生成年度账本...</div>
      </div>
    </div>

    <!-- 报告内容 -->
    <div class="report-wrapper" v-if="currentReport && !loading">
      <div class="report-book">
        <!-- 页面容器 -->
        <div class="pages-container" ref="pagesContainer">
          <!-- 第1页：封面 -->
          <div class="page page-cover" :class="{ active: currentPage === 1 }">
            <div class="cover-content">
              <div class="cover-seal">年度账本</div>
              <h1 class="cover-year">{{ currentReport.year }}</h1>
              <p class="cover-subtitle">我的财务故事</p>
              <div class="cover-decoration">
                <div class="decoration-line"></div>
                <div class="decoration-circle"></div>
                <div class="decoration-line"></div>
              </div>
              <p class="cover-date">记录每一笔收支，见证每一步成长</p>
            </div>
          </div>

          <!-- 第2页：财务总览 -->
          <div class="page page-summary" :class="{ active: currentPage === 2 }">
            <div class="page-header">
              <h2 class="page-title">财务总览</h2>
              <div class="page-seal">壹</div>
            </div>
            <div class="page-content">
              <div class="summary-cards">
                <div class="summary-card income-card">
                  <div class="card-label">年度收入</div>
                  <div class="card-value">{{ formatAmount(currentReport.summary.totalIncome) }}</div>
                  <div class="card-sub">月均 ¥{{ formatAmount(currentReport.summary.avgMonthlyIncome) }}</div>
                  <div class="card-trend" v-if="currentReport.yearlyComparison && currentReport.yearlyComparison.incomeGrowthRate">
                    <span :class="{ positive: currentReport.yearlyComparison.incomeGrowthRate > 0, negative: currentReport.yearlyComparison.incomeGrowthRate < 0 }">
                      {{ currentReport.yearlyComparison.incomeGrowthRate > 0 ? '↑' : '↓' }}{{ Math.abs(currentReport.yearlyComparison.incomeGrowthRate * 100).toFixed(1) }}%
                    </span>
                    <span class="trend-label">同比去年</span>
                  </div>
                </div>
                <div class="summary-card expense-card">
                  <div class="card-label">年度支出</div>
                  <div class="card-value">{{ formatAmount(currentReport.summary.totalExpense) }}</div>
                  <div class="card-sub">月均 ¥{{ formatAmount(currentReport.summary.avgMonthlyExpense) }}</div>
                  <div class="card-trend" v-if="currentReport.yearlyComparison && currentReport.yearlyComparison.expenseGrowthRate !== null">
                    <span :class="{ positive: currentReport.yearlyComparison.expenseGrowthRate < 0, negative: currentReport.yearlyComparison.expenseGrowthRate > 0 }">
                      {{ currentReport.yearlyComparison.expenseGrowthRate > 0 ? '↑' : '↓' }}{{ Math.abs(currentReport.yearlyComparison.expenseGrowthRate * 100).toFixed(1) }}%
                    </span>
                    <span class="trend-label">同比去年</span>
                  </div>
                </div>
                <div class="summary-card balance-card">
                  <div class="card-label">年度结余</div>
                  <div class="card-value" :class="{ negative: currentReport.summary.balance < 0 }">
                    {{ currentReport.summary.balance >= 0 ? '' : '-' }}¥{{ formatAmount(Math.abs(currentReport.summary.balance)) }}
                  </div>
                  <div class="card-sub">储蓄率 {{ (currentReport.summary.savingsRate * 100).toFixed(0) }}%</div>
                  <div class="card-trend" v-if="currentReport.yearlyComparison && currentReport.yearlyComparison.savingsRateChange !== null">
                    <span :class="{ positive: currentReport.yearlyComparison.savingsRateChange > 0, negative: currentReport.yearlyComparison.savingsRateChange < 0 }">
                      {{ currentReport.yearlyComparison.savingsRateChange > 0 ? '↑' : '↓' }}{{ Math.abs(currentReport.yearlyComparison.savingsRateChange * 100).toFixed(1) }}%
                    </span>
                    <span class="trend-label">同比去年</span>
                  </div>
                </div>
              </div>
              <div class="transaction-summary">
                <div class="transaction-item">
                  <span class="transaction-icon">📝</span>
                  <span class="transaction-text">全年共 <strong>{{ currentReport.summary.transactionCount }}</strong> 笔交易</span>
                </div>
                <div class="transaction-item">
                  <span class="transaction-icon">📅</span>
                  <span class="transaction-text">平均每天 <strong>{{ (currentReport.summary.transactionCount / 365).toFixed(1) }}</strong> 笔</span>
                </div>
                <div class="transaction-item">
                  <span class="transaction-icon">💰</span>
                  <span class="transaction-text">平均每笔 <strong>¥{{ formatAmount((currentReport.summary.totalExpense / currentReport.summary.transactionCount).toFixed(0)) }}</strong></span>
                </div>
              </div>

              <!-- 消费来源统计 -->
              <div class="payment-sources" v-if="currentReport.paymentSources && currentReport.paymentSources.length > 0">
                <div class="payment-title">消费来源</div>
                <div class="payment-items">
                  <div
                    v-for="source in currentReport.paymentSources"
                    :key="source.sourceName"
                    class="payment-item"
                    :class="source.sourceName === '微信' ? 'wechat' : 'alipay'"
                  >
                    <div class="payment-icon">{{ source.sourceName === '微信' ? '💬' : '💙' }}</div>
                    <div class="payment-info">
                      <div class="payment-name">{{ source.sourceName }}</div>
                      <div class="payment-bar">
                        <div class="bar-fill" :style="{ width: (source.percentage * 100) + '%' }"></div>
                      </div>
                    </div>
                    <div class="payment-stats">
                      <div class="payment-amount">¥{{ formatAmount(source.amount) }}</div>
                      <div class="payment-count">{{ source.transactionCount }}笔</div>
                    </div>
                    <div class="payment-percent">{{ (source.percentage * 100).toFixed(0) }}%</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 第3页：月度回顾 -->
          <div class="page page-monthly" :class="{ active: currentPage === 3 }">
            <div class="page-header">
              <h2 class="page-title">月度回顾</h2>
              <div class="page-seal">贰</div>
            </div>
            <div class="page-content">
              <div class="monthly-chart">
                <svg class="line-chart" viewBox="0 0 600 200">
                  <!-- 网格线 -->
                  <line v-for="i in 5" :key="'grid-' + i"
                    :x1="0" :y1="i * 40" :x2="600" :y2="i * 40"
                    class="chart-grid"
                  />
                  <!-- 收入线 -->
                  <polyline
                    :points="generateLinePoints(currentReport.monthlyStats, 'income')"
                    class="line-income"
                    fill="none"
                  />
                  <!-- 支出线 -->
                  <polyline
                    :points="generateLinePoints(currentReport.monthlyStats, 'expense')"
                    class="line-expense"
                    fill="none"
                  />
                  <!-- 数据点 -->
                  <circle
                    v-for="(month, index) in currentReport.monthlyStats"
                    :key="'income-' + index"
                    :cx="index * (600 / 12) + 25"
                    :cy="200 - (month.income / getMaxMonthlyAmount() * 180)"
                    r="4"
                    class="point-income"
                  />
                  <circle
                    v-for="(month, index) in currentReport.monthlyStats"
                    :key="'expense-' + index"
                    :cx="index * (600 / 12) + 25"
                    :cy="200 - (month.expense / getMaxMonthlyAmount() * 180)"
                    r="4"
                    class="point-expense"
                  />
                </svg>
                <div class="chart-legend">
                  <span class="legend-item"><span class="legend-color income"></span>收入</span>
                  <span class="legend-item"><span class="legend-color expense"></span>支出</span>
                </div>
              </div>
              <div class="monthly-highlights">
                <div class="highlight-card best-month">
                  <div class="highlight-label">最高收入月份</div>
                  <div class="highlight-value">{{ formatMonthName(getMaxIncomeMonth().month) }}</div>
                  <div class="highlight-sub">¥{{ formatAmount(getMaxIncomeMonth().income) }}</div>
                </div>
                <div class="highlight-card peak-expense">
                  <div class="highlight-label">最高支出月份</div>
                  <div class="highlight-value">{{ formatMonthName(getMaxExpenseMonth().month) }}</div>
                  <div class="highlight-sub">¥{{ formatAmount(getMaxExpenseMonth().expense) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 第4页：分类聚焦 -->
          <div class="page page-category" :class="{ active: currentPage === 4 }">
            <div class="page-header">
              <h2 class="page-title">分类聚焦</h2>
              <div class="page-seal">叁</div>
            </div>
            <div class="page-content">
              <div class="category-chart">
                <svg class="pie-chart" viewBox="0 0 200 200">
                  <circle
                    v-for="(cat, index) in currentReport.categoryStats.slice(0, 5)"
                    :key="cat.categoryName"
                    :cx="100"
                    :cy="100"
                    :r="80"
                    :stroke-dasharray="`${cat.percentage * 502} 502`"
                    :stroke-dashoffset="`-${getCategoryOffset(index) * 502}`"
                    :stroke="getCategoryColor(index)"
                    fill="none"
                    class="pie-segment"
                  />
                </svg>
                <div class="pie-legend">
                  <div
                    v-for="(cat, index) in currentReport.categoryStats.slice(0, 5)"
                    :key="cat.categoryName"
                    class="legend-row"
                  >
                    <span class="legend-dot" :style="{ background: getCategoryColor(index) }"></span>
                    <span class="legend-name">{{ cat.categoryName }}</span>
                    <span class="legend-amount">¥{{ formatAmount(cat.amount) }}</span>
                    <span class="legend-value">{{ (cat.percentage * 100).toFixed(0) }}%</span>
                  </div>
                </div>
              </div>
              <div class="top-categories-list">
                <div class="list-header">
                  <span class="list-icon">🏆</span>
                  <span class="list-title">支出TOP5</span>
                </div>
                <div class="top-category-items">
                  <div
                    v-for="(cat, index) in currentReport.categoryStats.slice(0, 5)"
                    :key="cat.categoryName"
                    class="top-category-item"
                  >
                    <div class="category-rank">{{ index + 1 }}</div>
                    <div class="category-info">
                      <div class="category-name">{{ cat.categoryName }}</div>
                      <div class="category-bar">
                        <div class="bar-fill" :style="{ width: (cat.percentage * 100) + '%', background: getCategoryColor(index) }"></div>
                      </div>
                    </div>
                    <div class="category-amount">¥{{ formatAmount(cat.amount) }}</div>
                    <div class="category-count">{{ cat.transactionCount }}笔</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 第5页：收入来源 -->
          <div class="page page-income" :class="{ active: currentPage === 5 }">
            <div class="page-header">
              <h2 class="page-title">收入来源</h2>
              <div class="page-seal">肆</div>
            </div>
            <div class="page-content">
              <div class="income-top3">
                <div
                  v-for="(source, index) in getTopIncomeSources()"
                  :key="source.sourceName"
                  class="income-top-item"
                  :class="'rank-' + (index + 1)"
                >
                  <div class="rank-badge">{{ index + 1 }}</div>
                  <div class="source-info">
                    <div class="source-name">{{ source.sourceName }}</div>
                    <div class="source-amount">¥{{ formatAmount(source.amount) }}</div>
                  </div>
                  <div class="source-percentage">{{ (source.percentage * 100).toFixed(1) }}%</div>
                  <div class="source-count">{{ source.transactionCount }}笔</div>
                </div>
              </div>
              <div class="income-insight">
                <div class="insight-icon">💡</div>
                <div class="insight-text">{{ getIncomeInsight() }}</div>
              </div>
            </div>
          </div>

          <!-- 第6页：消费习惯 -->
          <div class="page page-habits" :class="{ active: currentPage === 6 }">
            <div class="page-header">
              <h2 class="page-title">消费习惯</h2>
              <div class="page-seal">伍</div>
            </div>
            <div class="page-content">
              <div class="persona-card">
                <div class="persona-avatar">
                  <span class="persona-emoji">{{ getPersonaEmoji() }}</span>
                </div>
                <div class="persona-info">
                  <div class="persona-type">{{ currentReport.spendingHabits.personaType }}</div>
                  <div class="persona-tags">
                    <span
                      v-for="tag in currentReport.spendingHabits.tags"
                      :key="tag"
                      class="tag"
                    >{{ tag }}</span>
                  </div>
                </div>
              </div>
              <div class="habits-grid">
                <div class="habit-item">
                  <div class="habit-icon">⏰</div>
                  <div class="habit-content">
                    <div class="habit-label">高峰时段</div>
                    <div class="habit-value">{{ currentReport.spendingHabits.peakHours?.peakHour || '-' }}</div>
                  </div>
                </div>
                <div class="habit-item">
                  <div class="habit-icon">📆</div>
                  <div class="habit-content">
                    <div class="habit-label">最活跃日</div>
                    <div class="habit-value">{{ currentReport.spendingHabits.peakHours?.peakDayOfWeek || '-' }}</div>
                  </div>
                </div>
                <div class="habit-item">
                  <div class="habit-icon">💰</div>
                  <div class="habit-content">
                    <div class="habit-label">平均每笔</div>
                    <div class="habit-value">¥{{ currentReport.summary.avgMonthlyExpense > 0 ?
                      (currentReport.summary.totalExpense / currentReport.summary.transactionCount).toFixed(0) : 0 }}</div>
                  </div>
                </div>
                <div class="habit-item">
                  <div class="habit-icon">📈</div>
                  <div class="habit-content">
                    <div class="habit-label">时段活跃度</div>
                    <div class="habit-value">{{ ((currentReport.spendingHabits.peakHours?.peakHourRatio || 0) * 100).toFixed(0) }}%</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 第7页：年度对比 -->
          <div class="page page-comparison" :class="{ active: currentPage === 7 }">
            <div class="page-header">
              <h2 class="page-title">年度对比</h2>
              <div class="page-seal">陆</div>
            </div>
            <div class="page-content" v-if="currentReport.yearlyComparison">
              <div class="comparison-cards">
                <div class="comparison-item income-item">
                  <div class="comparison-header">
                    <span class="comparison-icon">💰</span>
                    <span class="comparison-label">收入对比</span>
                  </div>
                  <div class="comparison-values">
                    <div class="value-group last-year">
                      <div class="value-label">{{ currentReport.year - 1 }}年</div>
                      <div class="value-amount">¥{{ formatAmount(currentReport.yearlyComparison.lastYear?.lastYearIncome || 0) }}</div>
                    </div>
                    <div class="vs-arrow">→</div>
                    <div class="value-group current-year">
                      <div class="value-label">{{ currentReport.year }}年</div>
                      <div class="value-amount">¥{{ formatAmount(currentReport.summary.totalIncome) }}</div>
                    </div>
                  </div>
                  <div class="comparison-diff" :class="{ positive: currentReport.yearlyComparison.incomeGrowthRate > 0 }">
                    <span class="diff-icon">{{ currentReport.yearlyComparison.incomeGrowthRate > 0 ? '↑' : '↓' }}</span>
                    <span class="diff-value">¥{{ formatAmount(Math.abs(currentReport.summary.totalIncome - (currentReport.yearlyComparison.lastYear?.lastYearIncome || 0))) }}</span>
                    <span class="diff-percent">({{ (currentReport.yearlyComparison.incomeGrowthRate * 100).toFixed(1) }}%)</span>
                  </div>
                </div>

                <div class="comparison-item expense-item">
                  <div class="comparison-header">
                    <span class="comparison-icon">💸</span>
                    <span class="comparison-label">支出对比</span>
                  </div>
                  <div class="comparison-values">
                    <div class="value-group last-year">
                      <div class="value-label">{{ currentReport.year - 1 }}年</div>
                      <div class="value-amount">¥{{ formatAmount(currentReport.yearlyComparison.lastYear?.lastYearExpense || 0) }}</div>
                    </div>
                    <div class="vs-arrow">→</div>
                    <div class="value-group current-year">
                      <div class="value-label">{{ currentReport.year }}年</div>
                      <div class="value-amount">¥{{ formatAmount(currentReport.summary.totalExpense) }}</div>
                    </div>
                  </div>
                  <div class="comparison-diff" :class="{ positive: currentReport.yearlyComparison.expenseGrowthRate < 0 }">
                    <span class="diff-icon">{{ currentReport.yearlyComparison.expenseGrowthRate > 0 ? '↑' : '↓' }}</span>
                    <span class="diff-value">¥{{ formatAmount(Math.abs(currentReport.summary.totalExpense - (currentReport.yearlyComparison.lastYear?.lastYearExpense || 0))) }}</span>
                    <span class="diff-percent">({{ (currentReport.yearlyComparison.expenseGrowthRate * 100).toFixed(1) }}%)</span>
                  </div>
                </div>

                <div class="comparison-item savings-item">
                  <div class="comparison-header">
                    <span class="comparison-icon">🏦</span>
                    <span class="comparison-label">储蓄率</span>
                  </div>
                  <div class="savings-rate-compare">
                    <div class="rate-bar">
                      <div class="rate-fill last-year" :style="{ width: ((currentReport.yearlyComparison.lastYear?.lastYearSavingsRate || 0) * 100) + '%' }"></div>
                      <div class="rate-label">{{ currentReport.year - 1 }}</div>
                    </div>
                    <div class="rate-value">{{ ((currentReport.yearlyComparison.lastYear?.lastYearSavingsRate || 0) * 100).toFixed(0) }}%</div>
                  </div>
                  <div class="savings-rate-compare">
                    <div class="rate-bar">
                      <div class="rate-fill current-year" :style="{ width: (currentReport.summary.savingsRate * 100) + '%' }"></div>
                      <div class="rate-label">{{ currentReport.year }}</div>
                    </div>
                    <div class="rate-value highlight">{{ (currentReport.summary.savingsRate * 100).toFixed(0) }}%</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 第8页：年度总结 -->
          <div class="page page-insights" :class="{ active: currentPage === 8 }">
            <div class="page-header">
              <h2 class="page-title">年度总结</h2>
              <div class="page-seal">柒</div>
            </div>
            <div class="page-content">
              <div class="insights-section">
                <div class="insights-keywords">
                  <span
                    v-for="keyword in currentReport.insights.keywords"
                    :key="keyword"
                    class="keyword-tag"
                  >{{ keyword }}</span>
                </div>
                <div class="insights-summary">
                  {{ getGeneratedSummary() }}
                </div>

                <!-- 消费习惯总结 -->
                <div class="payment-habit-summary" v-if="currentReport.paymentSources && currentReport.paymentSources.length > 0">
                  <div class="habit-title">💳 消费习惯</div>
                  <div class="habit-content">
                    {{ getPaymentHabitSummary() }}
                  </div>
                  <div class="habit-detail">
                    <div
                      v-for="source in currentReport.paymentSources"
                      :key="source.sourceName"
                      class="habit-source-item"
                    >
                      <span class="source-icon">{{ source.sourceName === '微信' ? '💬' : '💙' }}</span>
                      <span class="source-text">{{ source.sourceName }}消费¥{{ formatAmount(source.amount) }}，占比{{ (source.percentage * 100).toFixed(0) }}%</span>
                    </div>
                  </div>
                </div>

                <div class="insights-suggestions">
                  <div class="suggestions-title">💡 改进建议</div>
                  <ul class="suggestions-list">
                    <li v-for="(suggestion, index) in getGeneratedSuggestions()" :key="index">
                      {{ suggestion }}
                    </li>
                  </ul>
                </div>
                <div class="insights-encouragement">
                  {{ currentReport.insights.encouragement }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 导航控制 -->
        <div class="report-navigation">
          <button class="nav-btn prev-btn" @click="prevPage" :disabled="currentPage === 1">
            <span class="btn-text">上一页</span>
          </button>
          <div class="page-indicator">
            <span class="current-page">{{ currentPage }}</span>
            <span class="page-separator">/</span>
            <span class="total-pages">8</span>
          </div>
          <button class="nav-btn next-btn" @click="nextPage" :disabled="currentPage === 8">
            <span class="btn-text">下一页</span>
          </button>
        </div>

        <!-- 年份切换 -->
        <button class="year-switch-btn" @click="backToYearSelect">
          <span>切换年份</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { fetchAnnualReport, fetchAvailableYears } from '../api/report'
import { ElMessage } from 'element-plus'

const availableYears = ref([])
const currentReport = ref(null)
const currentPage = ref(1)
const loading = ref(false)

// 加载可用年份
onMounted(async () => {
  await loadAvailableYears()
  setupKeyboardNavigation()
})

onUnmounted(() => {
  removeKeyboardNavigation()
})

// 键盘导航
const handleKeydown = (e) => {
  if (!currentReport.value) return
  if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
    nextPage()
  } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
    prevPage()
  }
}

const setupKeyboardNavigation = () => {
  window.addEventListener('keydown', handleKeydown)
}

const removeKeyboardNavigation = () => {
  window.removeEventListener('keydown', handleKeydown)
}

// 加载可用年份
const loadAvailableYears = async () => {
  try {
    const result = await fetchAvailableYears()
    if (result.success) {
      availableYears.value = result.data
    } else {
      ElMessage.error(result.message || '获取年份列表失败')
    }
  } catch (error) {
    console.error('获取年份列表失败:', error)
    ElMessage.error('获取年份列表失败: ' + error.message)
  }
}

// 加载报告
const loadReport = async (year) => {
  loading.value = true
  try {
    const result = await fetchAnnualReport(year)
    if (result.success) {
      currentReport.value = result.data
      currentPage.value = 1
    } else {
      ElMessage.error(result.message || '获取报告失败')
    }
  } catch (error) {
    console.error('获取报告失败:', error)
    ElMessage.error('获取报告失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 页面导航
const nextPage = () => {
  if (currentPage.value < 8) {
    currentPage.value++
  }
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const backToYearSelect = () => {
  currentReport.value = null
  currentPage.value = 1
}

// 格式化金额
const formatAmount = (amount) => {
  if (!amount) return '0'
  return amount.toLocaleString('zh-CN')
}

// 格式化月份名称
const formatMonthName = (monthStr) => {
  if (!monthStr) return '-'
  const months = ['一月', '二月', '三月', '四月', '五月', '六月',
                  '七月', '八月', '九月', '十月', '十一月', '十二月']
  const month = parseInt(monthStr.substring(5, 7))
  return months[month - 1] || monthStr
}

// 获取最大月度金额
const getMaxMonthlyAmount = () => {
  if (!currentReport.value?.monthlyStats) return 10000
  const maxIncome = Math.max(...currentReport.value.monthlyStats.map(m => m.income || 0))
  const maxExpense = Math.max(...currentReport.value.monthlyStats.map(m => m.expense || 0))
  return Math.max(maxIncome, maxExpense, 10000)
}

// 生成折线图点坐标
const generateLinePoints = (data, type) => {
  if (!data) return ''
  const max = getMaxMonthlyAmount()
  return data.map((month, index) => {
    const x = index * (600 / 12) + 25
    const y = 200 - ((month[type] || 0) / max * 180)
    return `${x},${y}`
  }).join(' ')
}

// 获取最高收入月份
const getMaxIncomeMonth = () => {
  if (!currentReport.value?.monthlyStats) return { month: '-', income: 0 }
  return currentReport.value.monthlyStats.reduce((max, month) =>
    month.income > max.income ? month : max
  , currentReport.value.monthlyStats[0])
}

// 获取最高支出月份
const getMaxExpenseMonth = () => {
  if (!currentReport.value?.monthlyStats) return { month: '-', expense: 0 }
  return currentReport.value.monthlyStats.reduce((max, month) =>
    month.expense > max.expense ? month : max
  , currentReport.value.monthlyStats[0])
}

// 获取分类颜色
const getCategoryColor = (index) => {
  const colors = ['#2D6A4F', '#C84630', '#D4A853', '#6B8E8F', '#E8B89D']
  return colors[index % colors.length]
}

// 获取分类偏移量
const getCategoryOffset = (index) => {
  if (!currentReport.value?.categoryStats) return 0
  let offset = 0
  for (let i = 0; i < index; i++) {
    offset += currentReport.value.categoryStats[i]?.percentage || 0
  }
  return offset
}

// 获取用户画像表情
const getPersonaEmoji = () => {
  const type = currentReport.value?.spendingHabits?.personaType
  const emojiMap = {
    '夜猫子美食家': '🌙',
    '理性购物者': '🛍️',
    '品质生活家': '✨',
    '理财小能手': '💰',
    '稳健理财者': '📊'
  }
  return emojiMap[type] || '👤'
}

// 获取Top3收入来源
const getTopIncomeSources = () => {
  const sources = currentReport.value?.incomeSources || []
  return sources.slice(0, 3)
}

// 获取支付方式消费习惯总结
const getPaymentHabitSummary = () => {
  const paymentSources = currentReport.value?.paymentSources || []
  if (paymentSources.length === 0) return '暂无支付方式数据。'

  if (paymentSources.length === 1) {
    const source = paymentSources[0]
    return `你是典型的${source.sourceName}用户，全年${source.sourceName}消费占比达${(source.percentage * 100).toFixed(0)}%，共${source.transactionCount}笔交易，合计¥${formatAmount(source.amount)}。建议合理搭配使用不同支付方式，享受更多优惠和便利。`
  }

  const maxSource = paymentSources.reduce((max, s) => s.percentage > max.percentage ? s : max)
  const secondSource = paymentSources.find(s => s.sourceName !== maxSource.sourceName)

  if (maxSource.percentage > 0.7) {
    return `你是${maxSource.sourceName}的重度用户，${maxSource.percentage > 0.8 ? '超过80%' : '接近70%'}的消费都通过${maxSource.sourceName}完成。${secondSource ? `${secondSource.sourceName}使用较少，仅占${(secondSource.percentage * 100).toFixed(0)}%` : ''}。建议根据不同场景选择支付方式，比如线上购物用支付宝，社交转账用微信。`
  } else if (Math.abs(paymentSources[0].percentage - paymentSources[1].percentage) < 0.1) {
    return `你的支付习惯很均衡，微信和支付宝使用比例相当（${(paymentSources[0].percentage * 100).toFixed(0)}% vs ${(paymentSources[1].percentage * 100).toFixed(0)}%）。这种灵活的支付方式搭配很好，能够根据不同场景选择最合适的支付工具。`
  } else {
    return `你主要使用${maxSource.sourceName}进行消费，占比${(maxSource.percentage * 100).toFixed(0)}%。${secondSource ? `${secondSource.sourceName}作为辅助支付方式，占比${(secondSource.percentage * 100).toFixed(0)}%` : ''}。保持这种支付习惯，同时关注各平台的优惠活动，可以让每一笔消费更划算。`
  }
}

// 生成更准确的总结
const getGeneratedSummary = () => {
  const report = currentReport.value
  if (!report) return ''

  const { summary, yearlyComparison } = report
  let text = `这一年，你一共支付了${summary.transactionCount}次，平均每天${(summary.transactionCount / 365).toFixed(1)}笔交易。\n\n`
  text += `你赚到了¥${formatAmount(summary.totalIncome)}，花掉了¥${formatAmount(summary.totalExpense)}，`

  if (summary.balance >= 0) {
    text += `结余¥${formatAmount(summary.balance)}。\n\n`
    text += `你的年度关键词是：稳健成长、理性消费。`

    if (yearlyComparison?.incomeGrowthRate > 0) {
      text += `相比去年，收入增长了${(yearlyComparison.incomeGrowthRate * 100).toFixed(0)}%，`
    }
    if (yearlyComparison?.savingsRateChange > 0) {
      text += `储蓄率提升了${(yearlyComparison.savingsRateChange * 100).toFixed(0)}个百分点。`
    }
  } else {
    text += `超支¥${formatAmount(Math.abs(summary.balance))}。\n\n`
    text += `你的年度关键词是：需要调整、控制支出。建议明年更加关注支出管理，合理规划每一笔消费。`
  }

  return text
}

// 生成改进建议
const getGeneratedSuggestions = () => {
  const report = currentReport.value
  if (!report) return []

  const { summary, yearlyComparison } = report
  const suggestions = []

  if (summary.balance < 0) {
    suggestions.push('今年支出超过收入，建议立即审查并削减不必要的开支，制定每月支出预算。')
  } else if (summary.savingsRate < 0.2) {
    suggestions.push('储蓄率偏低，建议将储蓄率提升到20%以上，每月设置固定储蓄目标。')
  } else if (summary.savingsRate < 0.4) {
    suggestions.push('建议将储蓄率提升到40%以上，每年可多储蓄¥' + formatAmount(summary.totalIncome * 0.1) + '。')
  } else {
    suggestions.push('你的储蓄率已经很高了，可以考虑增加投资理财的比重，让钱为你赚钱。')
  }

  if (yearlyComparison?.expenseGrowthRate > 0.1) {
    suggestions.push('今年支出增长较快，建议设置月度消费上限，避免冲动消费。')
  } else if (yearlyComparison?.expenseGrowthRate < 0) {
    suggestions.push('今年你成功控制了支出，消费观念更加成熟，继续保持！')
  }

  const sources = report.incomeSources || []
  if (sources.length <= 2) {
    suggestions.push('收入来源较为单一，建议学习投资理财知识，逐步实现收入来源多元化。')
  }

  return suggestions.slice(0, 4)
}

// 获取收入洞察
const getIncomeInsight = () => {
  const sources = currentReport.value?.incomeSources || []

  if (sources.length === 0) {
    return '暂无收入数据记录。'
  }

  if (sources.length === 1) {
    return `您的收入来源非常单一，全部来自${sources[0].sourceName}。建议积极拓展收入渠道，增加副业或投资收入，降低财务风险。`
  } else if (sources.length === 2) {
    return `您有${sources.length}个收入来源，主要以${sources[0].sourceName}为主。建议继续优化收入结构，增加被动收入比例。`
  } else {
    const top3 = sources.slice(0, 3).map(s => s.sourceName).join('、')
    return `您有${sources.length}个收入来源，前三位是${top3}。收入来源比较多元化，这是很好的财务状态！`
  }
}
</script>

<style scoped>
.annual-report-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #F5F1E8 0%, #EDE8D8 100%);
  padding: 40px 20px;
}

/* 年份选择器 */
.year-selector {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
}

.selector-title {
  font-size: 48px;
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 10px;
  position: relative;
  display: inline-block;
}

.selector-title::after {
  content: '账';
  position: absolute;
  top: -20px;
  right: -30px;
  font-size: 60px;
  color: #C84630;
  opacity: 0.3;
  font-family: "STXingkai", "KaiTi", serif;
}

.selector-subtitle {
  font-size: 18px;
  color: #6B8E8F;
  margin-bottom: 40px;
}

.year-cards {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.year-card {
  width: 200px;
  height: 250px;
  background: white;
  border: 2px solid #2D6A4F;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(45, 106, 79, 0.1);
}

.year-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(45, 106, 79, 0.2);
}

.year-card-inner {
  text-align: center;
}

.year-number {
  display: block;
  font-size: 72px;
  font-weight: bold;
  color: #2D6A4F;
  line-height: 1;
  font-family: "STXingkai", "KaiTi", serif;
}

.year-label {
  display: block;
  font-size: 16px;
  color: #6B8E8F;
  margin-top: 10px;
}

.year-card-seal {
  position: absolute;
  bottom: 15px;
  right: 15px;
  width: 50px;
  height: 50px;
  border: 3px solid #C84630;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #C84630;
  font-family: "STXingkai", "KaiTi", serif;
  transform: rotate(-15deg);
  opacity: 0.8;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.ink-loader {
  text-align: center;
}

.loader-circle {
  width: 80px;
  height: 80px;
  border: 4px solid #EDE8D8;
  border-top-color: #2D6A4F;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loader-text {
  color: #6B8E8F;
  font-size: 16px;
}

/* 报告容器 */
.report-wrapper {
  max-width: 900px;
  margin: 0 auto;
}

.report-book {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(45, 106, 79, 0.15);
  overflow: hidden;
  position: relative;
}

/* 页面容器 */
.pages-container {
  min-height: 600px;
  position: relative;
}

.page {
  padding: 40px;
  min-height: 600px;
  display: none;
  animation: fadeIn 0.5s ease;
}

.page.active {
  display: block;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 2px solid #EDE8D8;
  padding-bottom: 20px;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  color: #2D6A4F;
  margin: 0;
}

.page-seal {
  width: 60px;
  height: 60px;
  border: 3px solid #C84630;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #C84630;
  font-family: "STXingkai", "KaiTi", serif;
  transform: rotate(-10deg);
}

/* 封面页 */
.page-cover {
  background: linear-gradient(135deg, #2D6A4F 0%, #3D7A5F 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.page-cover .page-header {
  display: none;
}

.cover-content {
  text-align: center;
}

.cover-seal {
  font-size: 48px;
  font-family: "STXingkai", "KaiTi", serif;
  color: #D4A853;
  margin-bottom: 30px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.cover-year {
  font-size: 120px;
  font-weight: bold;
  margin: 0;
  font-family: "STXingkai", "KaiTi", serif;
  text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.2);
}

.cover-subtitle {
  font-size: 28px;
  margin: 20px 0;
  opacity: 0.9;
}

.cover-decoration {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin: 30px 0;
}

.decoration-line {
  width: 100px;
  height: 2px;
  background: #D4A853;
}

.decoration-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #D4A853;
  border-radius: 50%;
}

.cover-date {
  font-size: 16px;
  opacity: 0.8;
  margin-top: 40px;
}

/* 财务总览页 */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.summary-card {
  padding: 25px;
  border-radius: 12px;
  text-align: center;
  border: 2px solid #EDE8D8;
}

.income-card {
  background: linear-gradient(135deg, #2D6A4F 0%, #3D7A5F 100%);
  color: white;
  border-color: #2D6A4F;
}

.expense-card {
  background: linear-gradient(135deg, #C84630 0%, #D85640 100%);
  color: white;
  border-color: #C84630;
}

.balance-card {
  background: linear-gradient(135deg, #D4A853 0%, #E4B863 100%);
  color: white;
  border-color: #D4A853;
}

.card-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.card-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.card-value.negative {
  color: #C84630;
}

.card-sub {
  font-size: 14px;
  opacity: 0.8;
}

.card-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin-top: 8px;
  font-size: 13px;
}

.card-trend span:first-child {
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 4px;
}

.card-trend .positive {
  background: rgba(45, 106, 79, 0.2);
  color: #90EE90;
}

.card-trend .negative {
  background: rgba(200, 70, 48, 0.2);
  color: #FFB6B6;
}

.trend-label {
  opacity: 0.7;
  font-size: 12px;
}

.transaction-summary {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
  padding: 20px;
  background: #F5F1E8;
  border-radius: 12px;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.transaction-icon {
  font-size: 24px;
}

.transaction-text {
  color: #6B8E8F;
}

.transaction-text strong {
  color: #2D6A4F;
  font-size: 18px;
}

/* 消费来源统计 */
.payment-sources {
  margin-top: 25px;
  padding: 20px;
  background: #F5F1E8;
  border-radius: 12px;
}

.payment-title {
  font-size: 16px;
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 15px;
}

.payment-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  border-left: 4px solid #2D6A4F;
}

.payment-item.wechat {
  border-left-color: #07C160;
}

.payment-item.alipay {
  border-left-color: #1677FF;
}

.payment-icon {
  font-size: 28px;
}

.payment-info {
  flex: 1;
}

.payment-name {
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 6px;
}

.payment-bar {
  height: 8px;
  background: #EDE8D8;
  border-radius: 4px;
  overflow: hidden;
}

.payment-stats {
  text-align: right;
}

.payment-amount {
  font-weight: bold;
  color: #C84630;
  font-size: 16px;
}

.payment-count {
  font-size: 12px;
  color: #6B8E8F;
}

.payment-percent {
  font-size: 18px;
  font-weight: bold;
  color: #2D6A4F;
  min-width: 60px;
  text-align: right;
}

/* 月度回顾页 */
.monthly-chart {
  margin-bottom: 30px;
}

.line-chart {
  width: 100%;
  height: 200px;
}

.chart-grid {
  stroke: #EDE8D8;
  stroke-width: 1;
}

.line-income {
  stroke: #2D6A4F;
  stroke-width: 3;
  fill: none;
}

.line-expense {
  stroke: #C84630;
  stroke-width: 3;
  fill: none;
}

.point-income {
  fill: #2D6A4F;
}

.point-expense {
  fill: #C84630;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6B8E8F;
}

.legend-color {
  width: 20px;
  height: 4px;
  border-radius: 2px;
}

.legend-color.income {
  background: #2D6A4F;
}

.legend-color.expense {
  background: #C84630;
}

.monthly-highlights {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.highlight-card {
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  border: 2px solid #EDE8D8;
}

.best-month {
  background: #E8F5E9;
  border-color: #2D6A4F;
}

.peak-expense {
  background: #FFEBEE;
  border-color: #C84630;
}

.highlight-label {
  font-size: 14px;
  color: #6B8E8F;
  margin-bottom: 8px;
}

.highlight-value {
  font-size: 24px;
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 5px;
}

.highlight-sub {
  font-size: 14px;
  color: #6B8E8F;
}

/* 分类聚焦页 */
.category-chart {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 30px;
}

.pie-chart {
  width: 200px;
  height: 200px;
}

.pie-segment {
  stroke-width: 30;
  transition: all 0.3s ease;
  cursor: pointer;
}

.pie-segment:hover {
  stroke-width: 35;
}

.pie-legend {
  flex: 1;
}

.legend-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #EDE8D8;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-name {
  flex: 1;
  color: #6B8E8F;
}

.legend-amount {
  color: #2D6A4F;
  font-weight: bold;
  margin-right: 10px;
}

.legend-value {
  font-weight: bold;
  color: #C84630;
}

.top-categories-list {
  padding: 20px;
  background: #F5F1E8;
  border-radius: 12px;
}

.list-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.list-icon {
  font-size: 24px;
}

.list-title {
  font-size: 18px;
  font-weight: bold;
  color: #2D6A4F;
}

.top-category-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.top-category-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border-left: 4px solid #2D6A4F;
}

.category-rank {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #2D6A4F;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.category-info {
  flex: 1;
}

.category-name {
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 4px;
}

.category-bar {
  height: 6px;
  background: #EDE8D8;
  border-radius: 3px;
  overflow: hidden;
}

.category-amount {
  font-weight: bold;
  color: #C84630;
  font-size: 16px;
}

.category-count {
  font-size: 12px;
  color: #6B8E8F;
}

.top-category-detail {
  padding: 25px;
  background: linear-gradient(135deg, #FFF8F0 0%, #FFF5E6 100%);
  border-radius: 12px;
  border: 2px solid #D4A853;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.detail-icon {
  font-size: 28px;
}

.detail-title {
  font-size: 18px;
  font-weight: bold;
  color: #2D6A4F;
}

.detail-content {
  text-align: center;
}

.detail-name {
  font-size: 24px;
  font-weight: bold;
  color: #C84630;
  margin-bottom: 10px;
}

.detail-amount {
  font-size: 36px;
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 5px;
}

.detail-count {
  font-size: 14px;
  color: #6B8E8F;
}

/* 收入来源页 */
.income-top3 {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
}

.income-top-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 2px solid #EDE8D8;
  transition: all 0.3s ease;
}

.income-top-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(45, 106, 79, 0.1);
}

.income-top-item.rank-1 {
  border-color: #D4A853;
  background: linear-gradient(135deg, #FFF8F0 0%, white 100%);
}

.income-top-item.rank-2 {
  border-color: #C0C0C0;
}

.income-top-item.rank-3 {
  border-color: #CD7F32;
}

.rank-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
  color: white;
}

.rank-1 .rank-badge {
  background: linear-gradient(135deg, #D4A853 0%, #E4B863 100%);
}

.rank-2 .rank-badge {
  background: linear-gradient(135deg, #C0C0C0 0%, #D3D3D3 100%);
}

.rank-3 .rank-badge {
  background: linear-gradient(135deg, #CD7F32 0%, #E4A166 100%);
}

.source-info {
  flex: 1;
}

.source-name {
  font-size: 18px;
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 4px;
}

.source-amount {
  font-size: 14px;
  color: #6B8E8F;
}

.source-percentage {
  font-size: 20px;
  font-weight: bold;
  color: #C84630;
}

.source-count {
  font-size: 12px;
  color: #999;
  background: #F5F1E8;
  padding: 4px 8px;
  border-radius: 12px;
}

.income-insight {
  padding: 20px;
  background: linear-gradient(135deg, #E8F5E9 0%, #F1F8F4 100%);
  border-radius: 12px;
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.insight-icon {
  font-size: 28px;
}

.insight-text {
  flex: 1;
  color: #2D6A4F;
  line-height: 1.6;
}

/* 消费习惯页 */
.persona-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 25px;
  background: linear-gradient(135deg, #FFF8F0 0%, #FFF5E6 100%);
  border-radius: 12px;
  border: 2px solid #D4A853;
  margin-bottom: 30px;
}

.persona-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: white;
  border: 3px solid #D4A853;
  display: flex;
  align-items: center;
  justify-content: center;
}

.persona-emoji {
  font-size: 40px;
}

.persona-info {
  flex: 1;
}

.persona-type {
  font-size: 24px;
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 10px;
}

.persona-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 5px 12px;
  background: white;
  border: 1px solid #D4A853;
  border-radius: 20px;
  font-size: 14px;
  color: #D4A853;
}

.habits-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.habit-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #F5F1E8;
  border-radius: 12px;
}

.habit-icon {
  font-size: 32px;
}

.habit-label {
  font-size: 14px;
  color: #6B8E8F;
  margin-bottom: 5px;
}

.habit-value {
  font-size: 18px;
  font-weight: bold;
  color: #2D6A4F;
}

/* 年度对比页 */
.comparison-cards {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comparison-item {
  padding: 25px;
  background: white;
  border-radius: 12px;
  border: 2px solid #EDE8D8;
}

.comparison-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.comparison-icon {
  font-size: 28px;
}

.comparison-label {
  font-size: 18px;
  font-weight: bold;
  color: #2D6A4F;
}

.comparison-values {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 20px;
}

.value-group {
  text-align: center;
}

.value-label {
  font-size: 14px;
  color: #6B8E8F;
  margin-bottom: 8px;
}

.value-amount {
  font-size: 28px;
  font-weight: bold;
  color: #2D6A4F;
}

.vs-arrow {
  font-size: 32px;
  color: #D4A853;
}

.comparison-diff {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px;
  background: #F5F1E8;
  border-radius: 8px;
}

.comparison-diff.positive {
  background: linear-gradient(135deg, #E8F5E9 0%, #F1F8F4 100%);
}

.comparison-diff.negative {
  background: linear-gradient(135deg, #FFEBEE 0%, #FFF5F5 100%);
}

.diff-icon {
  font-size: 24px;
  font-weight: bold;
}

.diff-value {
  font-size: 20px;
  font-weight: bold;
  color: #2D6A4F;
}

.diff-percent {
  font-size: 14px;
  color: #6B8E8F;
}

.savings-item {
  padding: 20px;
}

.savings-rate-compare {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.rate-bar {
  flex: 1;
  height: 24px;
  background: #EDE8D8;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.rate-fill {
  height: 100%;
  border-radius: 12px;
  transition: width 1s ease;
}

.rate-fill.last-year {
  background: linear-gradient(90deg, #BDC3C7 0%, #D3D3D3 100%);
}

.rate-fill.current-year {
  background: linear-gradient(90deg, #D4A853 0%, #E4B863 100%);
}

.rate-label {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: white;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.rate-value {
  font-size: 20px;
  font-weight: bold;
  color: #2D6A4F;
  min-width: 60px;
  text-align: right;
}

.rate-value.highlight {
  color: #D4A853;
  font-size: 24px;
}

.comparison-chart {
  margin-bottom: 30px;
}

.comparison-bars {
  margin-bottom: 20px;
}

.bar-group {
  margin-bottom: 25px;
}

.bar-label {
  font-size: 14px;
  color: #6B8E8F;
  margin-bottom: 10px;
}

.bar-container {
  display: flex;
  gap: 5px;
  height: 30px;
  background: #F5F1E8;
  border-radius: 4px;
  overflow: hidden;
}

.bar-bar {
  height: 100%;
  transition: width 1s ease;
}

.last-year-bar {
  background: #BDC3C7;
}

.current-year-bar {
  position: relative;
}

.income-bar {
  background: #2D6A4F;
}

.expense-bar {
  background: #C84630;
}

.bar-change {
  text-align: right;
  font-size: 14px;
  font-weight: bold;
  margin-top: 5px;
}

.bar-change.positive {
  color: #2D6A4F;
}

.bar-change.negative {
  color: #C84630;
}

.comparison-legend {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 5px;
}

.legend-dot.last-year {
  background: #BDC3C7;
}

.legend-dot.current-year {
  background: #2D6A4F;
}

.savings-comparison {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  padding: 25px;
  background: linear-gradient(135deg, #E8F5E9 0%, #F1F8F4 100%);
  border-radius: 12px;
}

.savings-item {
  text-align: center;
}

.savings-label {
  display: block;
  font-size: 14px;
  color: #6B8E8F;
  margin-bottom: 5px;
}

.savings-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #2D6A4F;
}

.savings-value.highlight {
  color: #D4A853;
  font-size: 28px;
}

.savings-arrow {
  font-size: 24px;
  color: #6B8E8F;
}

/* 年度总结页 */
.insights-section {
  text-align: center;
}

.insights-keywords {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 30px;
}

.keyword-tag {
  padding: 10px 20px;
  background: linear-gradient(135deg, #2D6A4F 0%, #3D7A5F 100%);
  color: white;
  border-radius: 25px;
  font-size: 16px;
  font-weight: bold;
}

.insights-summary {
  font-size: 18px;
  line-height: 2;
  color: #6B8E8F;
  text-align: left;
  padding: 25px;
  background: #F5F1E8;
  border-radius: 12px;
  margin-bottom: 30px;
  white-space: pre-line;
}

/* 消费习惯总结 */
.payment-habit-summary {
  text-align: left;
  padding: 25px;
  background: linear-gradient(135deg, #FFF8F0 0%, #FFF5E6 100%);
  border-radius: 12px;
  border: 2px solid #D4A853;
  margin-bottom: 30px;
}

.habit-title {
  font-size: 18px;
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 15px;
}

.habit-content {
  font-size: 16px;
  line-height: 1.8;
  color: #6B8E8F;
  margin-bottom: 20px;
}

.habit-detail {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 15px;
  border-top: 1px dashed #D4A853;
}

.habit-source-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #6B8E8F;
}

.source-icon {
  font-size: 20px;
}

.source-text {
  flex: 1;
}

.insights-suggestions {
  text-align: left;
  margin-bottom: 30px;
}

.suggestions-title {
  font-size: 20px;
  font-weight: bold;
  color: #2D6A4F;
  margin-bottom: 15px;
}

.suggestions-list {
  list-style: none;
  padding: 0;
}

.suggestions-list li {
  padding: 12px 15px;
  background: white;
  border-left: 4px solid #D4A853;
  margin-bottom: 10px;
  color: #6B8E8F;
  line-height: 1.6;
}

.insights-encouragement {
  padding: 30px;
  background: linear-gradient(135deg, #FFF8F0 0%, #FFF5E6 100%);
  border-radius: 12px;
  font-size: 18px;
  line-height: 2;
  color: #2D6A4F;
  white-space: pre-line;
  font-style: italic;
}

/* 导航控制 */
.report-navigation {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 40px;
  background: #F5F1E8;
  border-top: 1px solid #EDE8D8;
}

.nav-btn {
  padding: 12px 30px;
  background: white;
  border: 2px solid #2D6A4F;
  border-radius: 8px;
  color: #2D6A4F;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-btn:hover:not(:disabled) {
  background: #2D6A4F;
  color: white;
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-indicator {
  font-size: 18px;
  color: #2D6A4F;
  font-weight: bold;
}

.current-page {
  font-size: 24px;
}

.page-separator {
  margin: 0 5px;
}

.total-pages {
  font-size: 16px;
  color: #6B8E8F;
}

.year-switch-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 10px 20px;
  background: white;
  border: 1px solid #EDE8D8;
  border-radius: 8px;
  color: #6B8E8F;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
}

.year-switch-btn:hover {
  background: #F5F1E8;
  color: #2D6A4F;
}
</style>
