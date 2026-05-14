<template>
  <section class='dashboard-page fade-in'>
    <div class='dashboard-hero'>
      <div class='hero-text'>
        <p class='hero-kicker'>总览</p>
        <h2 class='hero-title'>说帐总览</h2>
        <p class='hero-copy'>查看年度与月度的收入支出情况，从趋势图可直接下钻到对应月份账单明细。</p>
      </div>
      <div class='hero-form'>
        <el-date-picker v-model='selectedYear' type='year' value-format='YYYY' placeholder='选择年份' style='width: 140px' @change='loadDashboard' />
        <el-date-picker v-model='selectedMonth' type='month' value-format='YYYY-MM' placeholder='选择月份' style='width: 160px' @change='loadDashboard' />
        <el-button type='primary' class='btn-seal-refresh' @click='loadDashboard'>刷新</el-button>
      </div>
    </div>

    <div class='summary-grid'>
      <article class='metric-card income'>
        <p class='metric-label'>年度收入</p>
        <h3 class='metric-value income'>{{ dashboard.yearlyIncome }}</h3>
        <p class='metric-foot'>所选年份收入合计</p>
        <div class='metric-source'>
          <span class='source-wechat'>微信 {{ dashboard.yearlyIncomeBySource?.wechat ?? 0 }}</span>
          <span class='source-divider'>|</span>
          <span class='source-alipay'>支付宝 {{ dashboard.yearlyIncomeBySource?.alipay ?? 0 }}</span>
        </div>
      </article>
      <article class='metric-card expense'>
        <p class='metric-label'>年度支出</p>
        <h3 class='metric-value expense'>{{ dashboard.yearlyExpense }}</h3>
        <p class='metric-foot'>所选年份支出合计</p>
        <div class='metric-source'>
          <span class='source-wechat'>微信 {{ dashboard.yearlyExpenseBySource?.wechat ?? 0 }}</span>
          <span class='source-divider'>|</span>
          <span class='source-alipay'>支付宝 {{ dashboard.yearlyExpenseBySource?.alipay ?? 0 }}</span>
        </div>
      </article>
      <article class='metric-card income'>
        <p class='metric-label'>月度收入</p>
        <h3 class='metric-value income'>{{ dashboard.monthlyIncome }}</h3>
        <p class='metric-foot'>{{ selectedMonth }} 收入</p>
        <div class='metric-source'>
          <span class='source-wechat'>微信 {{ dashboard.monthlyIncomeBySource?.wechat ?? 0 }}</span>
          <span class='source-divider'>|</span>
          <span class='source-alipay'>支付宝 {{ dashboard.monthlyIncomeBySource?.alipay ?? 0 }}</span>
        </div>
      </article>
      <article class='metric-card expense'>
        <p class='metric-label'>月度支出</p>
        <h3 class='metric-value expense'>{{ dashboard.monthlyExpense }}</h3>
        <p class='metric-foot'>{{ selectedMonth }} 支出</p>
        <div class='metric-source'>
          <span class='source-wechat'>微信 {{ dashboard.monthlyExpenseBySource?.wechat ?? 0 }}</span>
          <span class='source-divider'>|</span>
          <span class='source-alipay'>支付宝 {{ dashboard.monthlyExpenseBySource?.alipay ?? 0 }}</span>
        </div>
      </article>
    </div>

    <div class='chart-grid'>
      <el-card shadow='never'>
        <template #header>
          <div class='card-head'>
            <div>
              <h3>年度收入支出趋势</h3>
              <p class='card-copy'>按月分别统计收入与支出，点击柱子可跳转到对应月份明细。</p>
            </div>
            <span class='card-action' @click='goToBillsByYear'>查看账单 →</span>
          </div>
        </template>
        <div class='chart-toolbar'>
          <el-radio-group v-model='trendSource' size='small' @change='renderCharts'>
            <el-radio-button value='全部'>全部</el-radio-button>
            <el-radio-button value='微信'>微信</el-radio-button>
            <el-radio-button value='支付宝'>支付宝</el-radio-button>
          </el-radio-group>
        </div>
        <div ref='yearTrendChartRef' class='chart-box trend-chart'></div>
      </el-card>

      <el-card shadow='never'>
        <template #header>
          <div class='card-head'>
            <div>
              <h3>年度支出分类</h3>
              <p class='card-copy'>所选年份支出按分类从高到低排列，柱子越长表示支出越多。</p>
            </div>
            <span class='card-action' @click='goToBillsByYear'>查看账单 →</span>
          </div>
        </template>
        <div class='chart-toolbar'>
          <el-radio-group v-model='categorySource' size='small' @change='renderCharts'>
            <el-radio-button value='全部'>全部</el-radio-button>
            <el-radio-button value='微信'>微信</el-radio-button>
            <el-radio-button value='支付宝'>支付宝</el-radio-button>
          </el-radio-group>
        </div>
        <div ref='yearCategoryChartRef' class='chart-box'></div>
      </el-card>

      <el-card shadow='never'>
        <template #header>
          <div class='card-head'>
            <div>
              <h3>月度支出分类</h3>
              <p class='card-copy'>所选月份支出按分类占比统计。</p>
            </div>
            <span class='card-action' @click='goToBillsByMonth(selectedMonth)'>查看账单 →</span>
          </div>
        </template>
        <div ref='monthCategoryChartRef' class='chart-box'></div>
      </el-card>

      <el-card shadow='never'>
        <template #header>
          <div class='card-head'>
            <div>
              <h3>月度每日收支趋势</h3>
              <p class='card-copy'>所选月份每日收入与支出分布。</p>
            </div>
            <span class='card-action' @click='goToBillsByMonth(selectedMonth)'>查看账单 →</span>
          </div>
        </template>
        <div ref='monthDayChartRef' class='chart-box trend-chart'></div>
      </el-card>
    </div>

    <div class='table-grid'>
      <el-card shadow='never'>
        <template #header>
          <div class='card-head'>
            <div>
              <h3>年度支出 Top10</h3>
              <p class='card-copy'>所选年份金额最高的支出记录。</p>
            </div>
          </div>
        </template>
        <div class='table-scroll-container'>
          <el-table :data='dashboard.yearlyTopItems' stripe empty-text='暂无数据'>
            <el-table-column prop='productName' label='商品' min-width='180' />
            <el-table-column prop='amount' label='金额' min-width='120' />
          </el-table>
        </div>
      </el-card>

      <el-card shadow='never'>
        <template #header>
          <div class='card-head'>
            <div>
              <h3>月度支出 Top10</h3>
              <p class='card-copy'>所选月份金额最高的支出记录。</p>
            </div>
          </div>
        </template>
        <div class='table-scroll-container'>
          <el-table :data='dashboard.monthlyTopItems' stripe empty-text='暂无数据'>
          <el-table-column prop='productName' label='商品' min-width='180' />
          <el-table-column prop='amount' label='金额' min-width='120' />
        </el-table>
        </div>
      </el-card>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { init } from 'echarts/core'
import { fetchDashboard } from '../api/dashboard'

use([CanvasRenderer, BarChart, PieChart, GridComponent, LegendComponent, TooltipComponent])

const router = useRouter()
const now = new Date()
const monthText = String(now.getMonth() + 1).padStart(2, '0')
const selectedYear = ref(String(now.getFullYear()))
const selectedMonth = ref(String(now.getFullYear()) + '-' + monthText)
const dashboard = ref({
  yearlyIncome: 0,
  yearlyExpense: 0,
  monthlyIncome: 0,
  monthlyExpense: 0,
  yearlyCategoryStats: [],
  monthlyCategoryStats: [],
  yearlyTopItems: [],
  monthlyTopItems: [],
  yearMonthTrend: [],
  yearMonthTrendWechat: [],
  yearMonthTrendAlipay: [],
  yearlyCategoryStatsWechat: [],
  yearlyCategoryStatsAlipay: [],
  monthlyDayTrend: [],
})

const yearTrendChartRef = ref(null)
const yearCategoryChartRef = ref(null)
const monthCategoryChartRef = ref(null)
const monthDayChartRef = ref(null)
let yearTrendChart = null
let yearCategoryChart = null
let monthCategoryChart = null
let monthDayChart = null

const trendSource = ref('全部')
const categorySource = ref('全部')

const palette = ['#22c55e', '#3b82f6', '#f59e0b', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#6366f1']

function trendData() {
  if (trendSource.value === '微信') return dashboard.value.yearMonthTrendWechat || []
  if (trendSource.value === '支付宝') return dashboard.value.yearMonthTrendAlipay || []
  return dashboard.value.yearMonthTrend || []
}

function categoryData() {
  if (categorySource.value === '微信') return dashboard.value.yearlyCategoryStatsWechat || []
  if (categorySource.value === '支付宝') return dashboard.value.yearlyCategoryStatsAlipay || []
  return dashboard.value.yearlyCategoryStats || []
}

const yearTrendOption = computed(() => {
  const data = trendData()
  return {
    color: ['#22c55e', '#ef4444'],
    grid: { left: 42, right: 16, top: 30, bottom: 26 },
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    xAxis: { type: 'category', data: data.map(item => item.month), axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#94a3b8' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } }, axisLabel: { color: '#94a3b8' } },
    series: [
      { type: 'bar', name: '收入', barWidth: 16, data: data.map(item => item.incomeAmount), itemStyle: { borderRadius: [6, 6, 0, 0] } },
      { type: 'bar', name: '支出', barWidth: 16, data: data.map(item => item.expenseAmount), itemStyle: { borderRadius: [6, 6, 0, 0] } },
    ],
  }
})

const yearCategoryOption = computed(() => {
  const sorted = [...categoryData()].sort((a, b) => b.amount - a.amount)
  const barData = sorted.map((item, i) => ({
    value: item.amount,
    itemStyle: { color: palette[i % palette.length] }
  }))
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 100, right: 80, top: 10, bottom: 10 },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } }, axisLabel: { color: '#94a3b8' } },
    yAxis: { type: 'category', data: sorted.map(item => item.categoryName || '未分类'), axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#64748b', fontWeight: 500 } },
    series: [{
      type: 'bar',
      data: barData,
      barMaxWidth: 28,
      itemStyle: { borderRadius: [0, 6, 6, 0] },
      label: { show: true, position: 'right', formatter: params => params.value.toFixed(2), color: '#64748b' }
    }]
  }
})

const monthCategoryOption = computed(() => ({
  color: palette,
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: '#64748b' } },
  series: [{ type: 'pie', radius: ['42%', '70%'], center: ['50%', '44%'], itemStyle: { borderColor: '#ffffff', borderWidth: 3 }, data: dashboard.value.monthlyCategoryStats.map(item => ({ name: item.categoryName || '未分类', value: item.amount })) }],
}))

const monthDayOption = computed(() => ({
  color: ['#22c55e', '#ef4444'],
  grid: { left: 42, right: 16, top: 30, bottom: 26 },
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  xAxis: { type: 'category', data: (dashboard.value.monthlyDayTrend || []).map(item => item.day), axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#94a3b8' } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } }, axisLabel: { color: '#94a3b8' } },
  series: [
    { type: 'bar', name: '收入', barWidth: 12, data: (dashboard.value.monthlyDayTrend || []).map(item => item.incomeAmount), itemStyle: { borderRadius: [4, 4, 0, 0] } },
    { type: 'bar', name: '支出', barWidth: 12, data: (dashboard.value.monthlyDayTrend || []).map(item => item.expenseAmount), itemStyle: { borderRadius: [4, 4, 0, 0] } },
  ],
}))

async function loadDashboard() {
  try {
    const response = await fetchDashboard({ year: selectedYear.value, month: selectedMonth.value })
    if (!response.success) {
      ElMessage.error(response.message || '加载首页分析失败')
      return
    }
    dashboard.value = response.data || dashboard.value
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error('加载首页分析失败，请检查后端服务')
  }
}

function renderCharts() {
  disposeCharts()
  if (yearTrendChartRef.value) {
    yearTrendChart = init(yearTrendChartRef.value)
    yearTrendChart.setOption(yearTrendOption.value)
    yearTrendChart.on('click', params => {
      if (params && params.name) {
        goToBillsByMonth(params.name, params.seriesName)
      }
    })
  }
  if (yearCategoryChartRef.value) {
    yearCategoryChart = init(yearCategoryChartRef.value)
    yearCategoryChart.setOption(yearCategoryOption.value)
  }
  if (monthCategoryChartRef.value) {
    monthCategoryChart = init(monthCategoryChartRef.value)
    monthCategoryChart.setOption(monthCategoryOption.value)
  }
  if (monthDayChartRef.value) {
    monthDayChart = init(monthDayChartRef.value)
    monthDayChart.setOption(monthDayOption.value)
  }
}

function disposeCharts() {
  if (yearTrendChart) { yearTrendChart.dispose(); yearTrendChart = null }
  if (yearCategoryChart) { yearCategoryChart.dispose(); yearCategoryChart = null }
  if (monthCategoryChart) { monthCategoryChart.dispose(); monthCategoryChart = null }
  if (monthDayChart) { monthDayChart.dispose(); monthDayChart = null }
}

function resizeCharts() {
  if (yearTrendChart) yearTrendChart.resize()
  if (yearCategoryChart) yearCategoryChart.resize()
  if (monthCategoryChart) monthCategoryChart.resize()
  if (monthDayChart) monthDayChart.resize()
}

function goToBillsByYear() {
  router.push({ path: '/bills', query: { year: selectedYear.value } })
}

function goToBillsByMonth(month, incomeExpenseType) {
  const query = { month }
  if (incomeExpenseType) {
    query.incomeExpenseType = incomeExpenseType
  }
  router.push({ path: '/bills', query })
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  disposeCharts()
})
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: var(--space-6);
}

.dashboard-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 32px;
  padding: 32px;
  background: linear-gradient(135deg,
    rgba(45, 106, 79, 0.08) 0%,
    rgba(45, 106, 79, 0.02) 100%
  );
  border: 2px solid rgba(45, 106, 79, 0.2);
  border-radius: var(--radius-xl);
  box-shadow:
    0 8px 24px rgba(42, 42, 42, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.hero-kicker {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.2em;
  color: var(--color-cyan);
  margin: 0 0 var(--space-3);
  padding-left: 18px;
  position: relative;
}

.hero-kicker::before {
  content: '◆';
  position: absolute;
  left: 0;
  font-size: 10px;
  color: var(--color-cyan);
}

.hero-title {
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--ink-black);
  margin: 0 0 var(--space-2);
  letter-spacing: 0.1em;
}

.hero-copy {
  font-size: 14px;
  color: var(--ink-light);
  margin: 0;
  max-width: 600px;
}

.hero-form {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

/* ── Seal Refresh Button ── */
.btn-seal-refresh {
  width: 64px !important;
  height: 64px !important;
  border: 3px solid var(--color-cyan) !important;
  border-radius: 4px !important;
  background: transparent !important;
  color: var(--color-cyan) !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  padding: 0 !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  box-shadow:
    inset 0 0 0 1px rgba(45, 106, 79, 0.1),
    0 4px 12px rgba(45, 106, 79, 0.2) !important;
  transition: all 0.3s ease !important;
  font-family: 'KaiTi', 'STKaiti', '楷体', serif !important;
}

.btn-seal-refresh:hover {
  transform: translateY(-2px) rotate(-3deg) !important;
  box-shadow:
    inset 0 0 0 2px rgba(45, 106, 79, 0.15),
    0 8px 16px rgba(45, 106, 79, 0.3) !important;
  background: rgba(45, 106, 79, 0.04) !important;
}

/* ── Card Action Link ── */
.card-action {
  color: var(--color-cyan);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s ease;
}

.card-action:hover {
  color: #235A42;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
}

.metric-card {
  background: linear-gradient(135deg, var(--bg-paper), #EDE8DD);
  border-radius: var(--radius-lg);
  padding: 24px;
  position: relative;
  overflow: hidden;
  box-shadow:
    0 8px 24px rgba(42, 42, 42, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg,
    transparent,
    rgba(45, 106, 79, 0.5),
    rgba(45, 106, 79, 0.3),
    transparent
  );
}

.metric-card.income::before {
  background: linear-gradient(90deg,
    transparent,
    rgba(212, 168, 83, 0.6),
    rgba(212, 168, 83, 0.3),
    transparent
  );
}

.metric-card.expense::before {
  background: linear-gradient(90deg,
    transparent,
    rgba(200, 70, 48, 0.6),
    rgba(200, 70, 48, 0.3),
    transparent
  );
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow:
    0 12px 32px rgba(42, 42, 42, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.metric-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--ink-light);
  margin: 0 0 var(--space-3);
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--ink-black);
  margin: 0 0 var(--space-2);
  font-family: 'Courier New', monospace;
}

.metric-value.income {
  color: #C49A6A;
  text-shadow: 0 2px 4px rgba(196, 154, 106, 0.2);
}

.metric-value.expense {
  color: #A63820;
  text-shadow: 0 2px 4px rgba(166, 56, 32, 0.2);
}

.metric-foot {
  font-size: 12px;
  color: var(--ink-light);
  margin: 0;
}

.metric-source {
  display: flex;
  gap: var(--space-2);
  align-items: center;
  font-size: 12px;
  color: var(--ink-light);
  margin-top: var(--space-3);
}

.source-wechat { color: var(--color-cyan); }
.source-alipay { color: var(--color-green); }
.source-divider { color: rgba(42, 42, 42, 0.2); }

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-5);
}

.table-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--space-5);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
}

.card-head h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-dark);
  margin: 0;
}

.card-copy {
  font-size: 12px;
  color: var(--ink-light);
  margin: 4px 0 0;
}

.chart-toolbar {
  margin-top: var(--space-3);
  display: flex;
  justify-content: flex-start;
}

/* ── 自定义水墨古风切换器 ── */
.chart-toolbar :deep(.el-radio-group) {
  display: inline-flex;
  background: var(--bg-paper);
  border: 2px solid rgba(42, 42, 42, 0.15);
  border-radius: var(--radius-sm);
  padding: 3px;
  gap: 3px;
  box-shadow: inset 0 1px 3px rgba(42, 42, 42, 0.05);
  position: relative;
}

/* 印章装饰角 */
.chart-toolbar :deep(.el-radio-group)::before {
  content: '';
  position: absolute;
  top: -1px;
  left: -1px;
  width: 12px;
  height: 12px;
  border-top: 2px solid var(--color-cyan);
  border-left: 2px solid var(--color-cyan);
  border-radius: 2px 0 0 0;
  opacity: 0.4;
}

.chart-toolbar :deep(.el-radio-group)::after {
  content: '';
  position: absolute;
  bottom: -1px;
  right: -1px;
  width: 12px;
  height: 12px;
  border-bottom: 2px solid var(--color-cyan);
  border-right: 2px solid var(--color-cyan);
  border-radius: 0 0 2px 0;
  opacity: 0.4;
}

.chart-toolbar :deep(.el-radio-button__inner) {
  background: transparent !important;
  border: none !important;
  color: var(--ink-medium) !important;
  font-size: 12px !important;
  font-weight: 500 !important;
  padding: 5px 14px !important;
  border-radius: 2px !important;
  transition: all 0.3s ease !important;
  font-family: 'Noto Serif SC', serif !important;
  box-shadow: none !important;
  position: relative;
  overflow: hidden;
}

.chart-toolbar :deep(.el-radio-button__inner::before) {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(45, 106, 79, 0.08), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.chart-toolbar :deep(.el-radio-button:hover .el-radio-button__inner) {
  color: var(--ink-dark) !important;
  background: rgba(45, 106, 79, 0.05) !important;
}

.chart-toolbar :deep(.el-radio-button:hover .el-radio-button__inner::before) {
  opacity: 1;
}

.chart-toolbar :deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: linear-gradient(135deg, rgba(45, 106, 79, 0.15), rgba(45, 106, 79, 0.08)) !important;
  color: var(--color-cyan) !important;
  font-weight: 600 !important;
  box-shadow:
    inset 0 1px 2px rgba(45, 106, 79, 0.1),
    0 1px 2px rgba(42, 42, 42, 0.05) !important;
}

.chart-toolbar :deep(.el-radio-button.is-active .el-radio-button__inner::before) {
  opacity: 1;
  background: linear-gradient(135deg, rgba(45, 106, 79, 0.12), rgba(45, 106, 79, 0.05));
}

/* 印章圆点指示器 */
.chart-toolbar :deep(.el-radio-button.is-active .el-radio-button__inner::after) {
  content: '◆';
  position: absolute;
  left: 4px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 8px;
  color: var(--color-cyan);
  opacity: 0.6;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.trend-chart {
  height: 360px;
}

@media (max-width: 1200px) {
  .summary-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 960px) {
  .dashboard-hero { flex-direction: column; align-items: stretch; }
  .hero-form { justify-content: flex-start; }
  .summary-grid { grid-template-columns: 1fr; }
  .chart-grid { grid-template-columns: 1fr; }
  .table-grid { grid-template-columns: 1fr; }
}
</style>
