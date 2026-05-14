<template>
  <section class='bills-page fade-in'>
    <div class='page-header'>
      <div class='page-title-group'>
        <h1 class='page-main-title'>逐笔说帐</h1>
        <p class='page-description'>支持按是否计入结算筛选，并可直接修改分类、结算状态或新增账单。</p>
      </div>
      <div class='page-header-actions'>
        <el-button type='primary' class='btn-seal-red' @click='openCreateDialog'>
          <span>✚</span>
          <span>补记一笔</span>
        </el-button>
      </div>
    </div>

    <el-card shadow='never'>
      <div class='filter-form'>
        <div class='filter-fields'>
          <el-form-item label='年份'>
            <el-date-picker v-model='selectedYear' type='year' value-format='YYYY' placeholder='选择年份' style='width: 140px' />
          </el-form-item>
          <el-form-item label='月份'>
            <el-date-picker v-model='selectedMonth' type='month' value-format='YYYY-MM' placeholder='选择月份' style='width: 160px' />
          </el-form-item>
          <el-form-item label='分类'>
            <el-select v-model='selectedCategoryId' clearable placeholder='全部' style='width: 180px'>
              <el-option v-for='item in categories' :key='item.id' :label='item.name' :value='item.id' />
            </el-select>
          </el-form-item>
          <el-form-item label='收支类型'>
            <el-select v-model='selectedIncomeExpenseType' clearable placeholder='全部' style='width: 140px'>
              <el-option label='收入' value='收入' />
              <el-option label='支出' value='支出' />
            </el-select>
          </el-form-item>
          <el-form-item label='来源'>
            <el-select v-model='selectedSource' clearable placeholder='全部' style='width: 140px'>
              <el-option label='微信' value='微信' />
              <el-option label='支付宝' value='支付宝' />
            </el-select>
          </el-form-item>
          <el-form-item label='计入结算'>
            <el-select v-model='selectedSettlementIncluded' clearable placeholder='全部' style='width: 140px'>
              <el-option label='计入' :value='true' />
              <el-option label='不计入' :value='false' />
            </el-select>
          </el-form-item>
        </div>
        <div class='filter-actions'>
          <el-button type='primary' @click='resetAndLoad'>刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow='never'>
      <div class='stats-bar'>
        <div class='stat-item'>
          <span class='stat-label'>总金额</span>
          <span class='stat-value total'>{{ stats.totalAmount.toFixed(2) }}</span>
          <span class='stat-unit'>元</span>
        </div>
        <div class='stat-item'>
          <span class='stat-label'>支出</span>
          <span class='stat-value expense'>{{ stats.expenseAmount.toFixed(2) }}</span>
          <span class='stat-unit'>元</span>
        </div>
        <div class='stat-item'>
          <span class='stat-label'>收入</span>
          <span class='stat-value income'>{{ stats.incomeAmount.toFixed(2) }}</span>
          <span class='stat-unit'>元</span>
        </div>
        <div class='stat-item'>
          <span class='stat-label'>记录数</span>
          <span class='stat-value'>{{ stats.totalCount }}</span>
          <span class='stat-unit'>条</span>
        </div>
      </div>
    </el-card>

    <el-card shadow='never' class='table-card'>
      <div class='table-scroll-container bills-table-scroll'>
        <el-table
          :data='bills'
          stripe
          empty-text='暂无数据'
          flexible
          :fit='false'
        >
          <el-table-column prop='id' label='ID' width='110' />
          <el-table-column label='来源' width='110'>
            <template #default='{ row }'>
              <span v-if='row.source === "微信"' class='source-tag wechat'>微信</span>
              <span v-else-if='row.source === "支付宝"' class='source-tag alipay'>支付宝</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop='tradeTime' label='交易时间' width='190' />
          <el-table-column prop='tradeType' label='交易类型' width='140' />
          <el-table-column prop='incomeExpenseType' label='收支' width='110' />
          <el-table-column prop='counterparty' label='交易对方' width='170' />
          <el-table-column prop='productName' label='商品' width='200' show-overflow-tooltip />
          <el-table-column prop='amount' label='金额' width='130' align='right' class-name='font-mono-table' />
          <el-table-column prop='tradeStatus' label='状态' width='120' />
          <el-table-column label='结算' width='110'>
            <template #default='{ row }'>
              <el-switch
                :model-value='row.settlementIncluded'
                @change='value => changeSettlementIncluded(row, value)'
              />
            </template>
          </el-table-column>
          <el-table-column label='分类' width='210' class-name='col-category'>
            <template #default='{ row }'>
              <el-select
                :model-value='row.categoryId'
                clearable
                placeholder='选择分类'
                style='width: 175px'
                teleported
                @change='value => changeCategory(row, value)'
              >
                <el-option v-for='item in categories' :key='item.id' :label='item.name' :value='item.id' />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label='同步' width='120'>
            <template #default='{ row }'>
              <span v-if='row.categorySyncStatus === "success"' class='sync-tag success'>已同步</span>
              <span v-else-if='row.categorySyncStatus === "review"' class='sync-tag review'>待复核</span>
              <span v-else-if='row.categorySyncStatus === "category_missing"' class='sync-tag review'>分类缺失</span>
              <span v-else-if='row.categorySyncStatus === "failed"' class='sync-tag failed'>分类失败</span>
              <span v-else-if='row.categorySyncStatus === "manual"' class='sync-tag manual'>手动设置</span>
              <span v-else-if='row.categorySyncStatus' class='sync-tag'>{{ row.categorySyncStatus }}</span>
              <span v-else class='sync-tag none'>未同步</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class='bills-pagination'>
        <button class='page-btn' :disabled='page <= 1' @click='prevPage'>上一页</button>
        <span class='page-info'>第 {{ page }} 页</span>
        <button class='page-btn' :disabled='bills.length < pageSize' @click='nextPage'>下一页</button>
        <select class='page-size-select' :value='pageSize' @change='e => { pageSize = Number(e.target.value); resetAndLoad() }'>
          <option :value='10'>10 条/页</option>
          <option :value='20'>20 条/页</option>
          <option :value='50'>50 条/页</option>
        </select>
      </div>
    </el-card>

    <el-dialog v-model='createDialogVisible' width='680px'>
      <template #header>
        <h3 class='dialog-title-fake'>补记一笔</h3>
      </template>
      <el-form label-width='90px'>
        <el-row :gutter='20'>
          <el-col :span='12'>
            <el-form-item label='交易时间'>
              <el-input v-model='createForm.tradeTime' placeholder='2026-05-09 12:00:00' />
            </el-form-item>
          </el-col>
          <el-col :span='12'>
            <el-form-item label='交易类型'>
              <el-input v-model='createForm.tradeType' placeholder='商户消费' />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter='20'>
          <el-col :span='12'>
            <el-form-item label='收支类型'>
              <el-select v-model='createForm.incomeExpenseType' placeholder='请选择' style='width: 100%'>
                <el-option label='收入' value='收入' />
                <el-option label='支出' value='支出' />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span='12'>
            <el-form-item label='金额'>
              <el-input v-model='createForm.amount' type='number' placeholder='0.00' />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter='20'>
          <el-col :span='12'>
            <el-form-item label='交易对方'>
              <el-input v-model='createForm.counterparty' />
            </el-form-item>
          </el-col>
          <el-col :span='12'>
            <el-form-item label='商品'>
              <el-input v-model='createForm.productName' />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter='20'>
          <el-col :span='12'>
            <el-form-item label='支付方式'>
              <el-input v-model='createForm.payMethod' />
            </el-form-item>
          </el-col>
          <el-col :span='12'>
            <el-form-item label='交易状态'>
              <el-input v-model='createForm.tradeStatus' />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter='20'>
          <el-col :span='12'>
            <el-form-item label='交易单号'>
              <el-input v-model='createForm.tradeNo' />
            </el-form-item>
          </el-col>
          <el-col :span='12'>
            <el-form-item label='商户单号'>
              <el-input v-model='createForm.merchantOrderNo' />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter='20'>
          <el-col :span='24'>
            <el-form-item label='备注'>
              <el-input v-model='createForm.remark' />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter='20'>
          <el-col :span='12'>
            <el-form-item label='分类'>
              <el-select v-model='createForm.categoryId' clearable placeholder='不分类' style='width: 100%'>
                <el-option v-for='item in categories' :key='item.id' :label='item.name' :value='item.id' />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span='12'>
            <el-form-item label='计入结算'>
              <el-switch v-model='createForm.settlementIncluded' inline-prompt active-text='是' inactive-text='否' />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click='createDialogVisible = false'>取消</el-button>
        <el-button type='primary' @click='submitCreate'>保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createBill, fetchBills, fetchBillStats, updateBillCategory, updateBillSettlement } from '../api/bill'
import { fetchCategories } from '../api/category'

// 暴露刷新方法给父组件
defineExpose({
  refreshBills: () => loadBills()
})

const route = useRoute()
const bills = ref([])
const categories = ref([])
const selectedYear = ref('')
const selectedMonth = ref('')
const selectedCategoryId = ref(null)
const selectedIncomeExpenseType = ref('')
const selectedSource = ref('')
const selectedSettlementIncluded = ref(null)
const stats = ref({ totalAmount: 0, incomeAmount: 0, expenseAmount: 0, totalCount: 0 })
const page = ref(1)
const pageSize = ref(20)
const createDialogVisible = ref(false)
const createForm = ref(defaultCreateForm())

function defaultCreateForm() {
  return {
    tradeTime: '',
    tradeType: '',
    incomeExpenseType: '支出',
    counterparty: '',
    productName: '',
    amount: '',
    payMethod: '',
    tradeStatus: '',
    tradeNo: '',
    merchantOrderNo: '',
    remark: '',
    categoryId: null,
    settlementIncluded: true,
  }
}

function syncFiltersFromRoute() {
  selectedYear.value = route.query.year ? String(route.query.year) : ''
  selectedMonth.value = route.query.month ? String(route.query.month) : ''
  selectedIncomeExpenseType.value = route.query.incomeExpenseType ? String(route.query.incomeExpenseType) : ''
}

async function loadCategories() {
  try {
    const response = await fetchCategories()
    if (response.success) {
      categories.value = response.data || []
      return
    }
    ElMessage.error(response.message || '加载分类失败')
  } catch {
    ElMessage.error('加载分类失败，请检查后端服务')
  }
}

async function loadBills() {
  try {
    const params = {
      year: selectedYear.value || undefined,
      month: selectedMonth.value || undefined,
      categoryId: selectedCategoryId.value || undefined,
      incomeExpenseType: selectedIncomeExpenseType.value || undefined,
      settlementIncluded: selectedSettlementIncluded.value,
      source: selectedSource.value || undefined,
      page: page.value,
      pageSize: pageSize.value,
    }
    const [billRes, statsRes] = await Promise.all([
      fetchBills(params),
      fetchBillStats(params),
    ])
    if (billRes.success) {
      bills.value = billRes.data || []
    } else {
      ElMessage.error(billRes.message || '加载账单失败')
    }
    if (statsRes.success) {
      stats.value = statsRes.data || { totalAmount: 0, incomeAmount: 0, expenseAmount: 0, totalCount: 0 }
    }
  } catch {
    ElMessage.error('加载账单失败，请检查后端服务')
  }
}

function openCreateDialog() {
  createForm.value = defaultCreateForm()
  createDialogVisible.value = true
}

async function submitCreate() {
  try {
    const payload = {
      ...createForm.value,
      amount: Number(createForm.value.amount),
      categoryId: createForm.value.categoryId || undefined,
      settlementIncluded: createForm.value.settlementIncluded,
    }
    const response = await createBill(payload)
    if (response.success) {
      ElMessage.success('账单已创建')
      createDialogVisible.value = false
      await loadBills()
      return
    }
    ElMessage.error(response.message || '创建账单失败')
  } catch {
    ElMessage.error('创建账单失败，请检查后端服务')
  }
}

async function changeCategory(row, categoryId) {
  try {
    const response = await updateBillCategory(row.id, categoryId)
    if (response.success) {
      row.categoryId = categoryId
      const category = categories.value.find(item => item.id === categoryId)
      row.categoryName = category ? category.name : ''
      ElMessage.success('分类已更新')
      return
    }
    ElMessage.error(response.message || '更新分类失败')
  } catch {
    ElMessage.error('更新分类失败，请检查后端服务')
  }
}

async function changeSettlementIncluded(row, settlementIncluded) {
  const previousValue = row.settlementIncluded
  row.settlementIncluded = settlementIncluded
  try {
    const response = await updateBillSettlement(row.id, settlementIncluded)
    if (response.success) {
      ElMessage.success(settlementIncluded ? '已标记为计入结算' : '已标记为不计入结算')
      return
    }
    row.settlementIncluded = previousValue
    ElMessage.error(response.message || '更新结算标记失败')
  } catch {
    row.settlementIncluded = previousValue
    ElMessage.error('更新结算标记失败，请检查后端服务')
  }
}

function resetAndLoad() {
  page.value = 1
  loadBills()
}

function prevPage() {
  if (page.value > 1) {
    page.value -= 1
    loadBills()
  }
}

function nextPage() {
  page.value += 1
  loadBills()
}

watch(() => route.query, () => {
  syncFiltersFromRoute()
  page.value = 1
  loadBills()
})

onMounted(async () => {
  syncFiltersFromRoute()
  await loadCategories()
  await loadBills()
})
</script>

<style scoped>
.bills-page {
  display: grid;
  gap: var(--space-4);
  min-width: 0;
  overflow-x: hidden;
}

/* ── Page Header ── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 20px;
  border-bottom: 2px solid rgba(42, 42, 42, 0.12);
  position: relative;
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  width: 120px;
  height: 4px;
  background: linear-gradient(90deg, var(--color-cyan), transparent);
  border-radius: 2px;
}

.page-title-group h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-black);
  margin: 0 0 8px;
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  letter-spacing: 0.1em;
}

.page-description {
  font-size: 14px;
  color: var(--ink-light);
  line-height: 1.6;
  margin: 0;
}

.page-header-actions {
  display: flex;
  gap: 12px;
}

/* ── Red Seal Button ── */
.btn-seal-red {
  background: var(--color-red) !important;
  color: var(--color-white) !important;
  border: none !important;
  box-shadow:
    0 4px 12px rgba(200, 70, 48, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2) !important;
  font-family: 'KaiTi', 'STKaiti', '楷体', serif !important;
  letter-spacing: 0.1em !important;
}

.btn-seal-red:hover {
  transform: translateY(-2px) !important;
  box-shadow:
    0 6px 16px rgba(200, 70, 48, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2) !important;
}

/* ── Filter ── */
.filter-form {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
}

.filter-fields {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0;
  flex: 1;
}

.filter-actions {
  flex-shrink: 0;
  padding-top: 2px;
}

@media (max-width: 1100px) {
  .filter-form {
    flex-direction: column;
  }
}

/* ── Table Card / Scroll ── */
/* 列宽合计约 1720px，略增以含边框；强制表比可视区宽时由外层 .table-scroll-container 横向滚动 */
.bills-table-scroll :deep(.el-table) {
  width: max-content;
  min-width: 100%;
  max-width: none;
}

/* ── Source Tags ── */
.source-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  line-height: 1.4;
}

.source-tag.wechat {
  background: rgba(45, 106, 79, 0.12);
  color: var(--color-cyan);
}

.source-tag.alipay {
  background: rgba(34, 197, 94, 0.12);
  color: var(--color-green);
}

/* ── Category Column ── */
:deep(td.col-category) {
  padding-left: 0 !important;
  padding-right: 0 !important;
}

/* ── Sync Tags ── */
.sync-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  line-height: 1.4;
  white-space: nowrap;
}

.sync-tag.success {
  background: rgba(74, 124, 89, 0.12);
  color: var(--color-green);
}

.sync-tag.review {
  background: rgba(212, 168, 83, 0.12);
  color: var(--color-brown);
}

.sync-tag.failed {
  background: rgba(200, 70, 48, 0.12);
  color: var(--color-red);
}

.sync-tag.manual {
  background: rgba(74, 106, 165, 0.12);
  color: var(--color-indigo);
}

.sync-tag.none {
  color: var(--ink-light);
}

/* ── Pagination ── */
.bills-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 24px 0;
  margin-top: 8px;
}

.page-btn {
  padding: 8px 20px;
  background: var(--bg-paper);
  border: 1px solid rgba(42, 42, 42, 0.12);
  border-radius: 4px;
  color: var(--ink-medium);
  font-size: 13px;
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--color-cyan);
  color: var(--color-cyan);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 13px;
  color: var(--ink-medium);
}

.page-size-select {
  padding: 8px 12px;
  border: 1px solid rgba(42, 42, 42, 0.12);
  border-radius: 4px;
  background: var(--bg-paper);
  color: var(--ink-medium);
  font-size: 13px;
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  cursor: pointer;
}

.page-size-select:hover {
  border-color: var(--color-cyan);
}

.page-size-select:focus {
  outline: none;
  border-color: var(--color-cyan);
  box-shadow: 0 0 0 3px rgba(45, 106, 79, 0.1);
}

/* ── Dialog Title ── */
.dialog-title-fake {
  font-size: 18px;
  font-weight: 600;
  color: var(--ink-dark);
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  letter-spacing: 0.1em;
  margin: 0;
}

:deep(.el-dialog) {
  animation: dialogSlideIn 0.3s ease;
}

@keyframes dialogSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
