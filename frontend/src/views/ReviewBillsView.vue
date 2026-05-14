<template>
  <section class="review-page">
    <div class="page-head">
      <div>
        <h2>说帐复核</h2>
        <p>置信度在 0.60~0.85 之间的待复核账单，请确认或修正分类。</p>
      </div>
    </div>

    <el-card shadow="never" class="batch-card">
      <div class="batch-header">
        <span class="batch-title">批量分类</span>
        <el-button type="primary" size="small" @click="showBatchDialog = true">按条件批量分类</el-button>
      </div>
      <div class="batch-hint">通过交易对方和商品名称匹配待复核账单，批量确认分类并同步。</div>
    </el-card>

    <el-dialog v-model="showBatchDialog" title="批量分类" width="500px" :close-on-click-modal="false">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="交易对方">
          <el-input v-model="batchForm.counterparty" placeholder="输入交易对方名称（支持模糊匹配）" />
        </el-form-item>
        <el-form-item label="商品">
          <el-input v-model="batchForm.productName" placeholder="输入商品名称（支持模糊匹配）" />
        </el-form-item>
        <el-form-item label="目标分类">
          <el-select v-model="batchForm.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="matchCount !== null" label="匹配数量">
          <el-tag type="warning">{{ matchCount }} 条账单</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchDialog = false">取消</el-button>
        <el-button :disabled="!canBatch" @click="previewMatch">预览匹配</el-button>
        <el-button type="primary" :disabled="!canBatch || matchCount === 0" :loading="batchUpdateLoading" @click="doBatchUpdate">确认批量更新</el-button>
      </template>
    </el-dialog>

    <el-card shadow="never" class="table-card">
      <div class="batch-bar" v-if="selectedIds.length > 0">
        <span class="selected-info">已选择 {{ selectedIds.length }} 条</span>
        <el-button type="primary" size="small" :loading="batchLoading" @click="handleBatchConfirm">批量确认同步</el-button>
        <el-button size="small" @click="clearSelection">取消选择</el-button>
      </div>

      <div class="table-scroll-container">
        <el-table
          ref="tableRef"
          :data="bills"
          stripe
          empty-text="暂无待复核账单"
          @selection-change="onSelectionChange"
        >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="tradeTime" label="交易时间" min-width="150" />
        <el-table-column prop="counterparty" label="交易对方" min-width="140" />
        <el-table-column prop="productName" label="商品" min-width="180" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="100" align="right" class-name="font-mono-table" />
        <el-table-column label="置信度" width="100">
          <template #default="{ row }">
            <el-tag :type="confidenceType(row.categoryConfidence)" size="small">
              {{ (row.categoryConfidence * 100).toFixed(0) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="模型分类" min-width="120">
          <template #default="{ row }">
            {{ categoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column label="分类原因" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.categorySyncReason || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.categoryId"
              type="primary"
              size="small"
              :loading="syncingId === row.id"
              @click="onConfirm(row)"
            >确认同步</el-button>
            <el-select
              v-model="row.categoryId"
              placeholder="选择分类"
              size="small"
              style="width: 120px"
              @change="value => onApprove(row, value)"
            >
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      </div>

      <div class="pager">
        <el-button :disabled="page <= 1" @click="prevPage">上一页</el-button>
        <span>第 {{ page }} 页 / 每页 {{ pageSize }} 条</span>
        <el-button :disabled="bills.length < pageSize" @click="nextPage">下一页</el-button>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchBills, updateBillCategory, batchConfirmBillCategory, countByMatch, batchUpdateByMatch } from '../api/bill'
import { fetchCategories } from '../api/category'

const bills = ref([])
const categories = ref([])
const page = ref(1)
const pageSize = ref(20)
const syncingId = ref(null)
const selectedIds = ref([])
const batchLoading = ref(false)
const tableRef = ref(null)

const showBatchDialog = ref(false)
const matchCount = ref(null)
const batchUpdateLoading = ref(false)
const batchForm = ref({ counterparty: '', productName: '', categoryId: null })

const canBatch = computed(() => {
  return (batchForm.value.counterparty || batchForm.value.productName) && batchForm.value.categoryId
})

async function previewMatch() {
  matchCount.value = null
  try {
    const resp = await countByMatch({ counterparty: batchForm.value.counterparty, productName: batchForm.value.productName, categorySyncStatus: 'review' })
    if (resp.success) {
      matchCount.value = resp.data.matchCount || 0
      if (matchCount.value === 0) {
        ElMessage.info('没有匹配的待复核账单')
      }
      return
    }
    ElMessage.error(resp.message || '预览失败')
  } catch {
    ElMessage.error('预览失败，请检查后端服务')
  }
}

async function doBatchUpdate() {
  ElMessageBox.confirm(`将更新 ${matchCount.value} 条待复核账单的分类并同步，确认继续？`, '确认批量更新', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    batchUpdateLoading.value = true
    try {
      const resp = await batchUpdateByMatch({ counterparty: batchForm.value.counterparty, productName: batchForm.value.productName, categoryId: batchForm.value.categoryId, categorySyncStatus: 'review' })
      if (resp.success) {
        ElMessage.success(`成功更新 ${resp.data.updatedCount} 条账单`)
        showBatchDialog.value = false
        matchCount.value = null
        batchForm.value = { counterparty: '', productName: '', categoryId: null }
        await loadBills()
        return
      }
      ElMessage.error(resp.message || '批量更新失败')
    } catch {
      ElMessage.error('批量更新失败，请检查后端服务')
    } finally {
      batchUpdateLoading.value = false
    }
  }).catch(() => {})
}

async function loadBills() {
  try {
    const resp = await fetchBills({ categorySyncStatus: 'review', page: page.value, pageSize: pageSize.value })
    if (resp.success) {
      bills.value = resp.data || []
      return
    }
    ElMessage.error(resp.message || '加载失败')
  } catch {
    ElMessage.error('加载失败，请检查后端服务')
  }
}

async function loadCategories() {
  try {
    const resp = await fetchCategories()
    if (resp.success) categories.value = resp.data || []
  } catch { /* ignore */ }
}

function categoryName(id) {
  const cat = categories.value.find(c => c.id === id)
  return cat ? cat.name : '未分类'
}

function confidenceType(val) {
  if (!val) return 'info'
  if (val >= 0.85) return 'success'
  if (val >= 0.60) return 'warning'
  return 'danger'
}

function onSelectionChange(selection) {
  selectedIds.value = selection.map(r => r.id)
}

function clearSelection() {
  if (tableRef.value) tableRef.value.clearSelection()
  selectedIds.value = []
}

async function onApprove(row, categoryId) {
  if (!categoryId) return
  syncingId.value = row.id
  try {
    const resp = await updateBillCategory(row.id, categoryId)
    if (resp.success) {
      row.categorySyncStatus = 'manual'
      ElMessage.success('分类已确认')
      return
    }
    ElMessage.error(resp.message || '更新失败')
  } catch {
    ElMessage.error('更新失败')
  } finally {
    syncingId.value = null
  }
}

async function onConfirm(row) {
  if (!row.categoryId) return
  syncingId.value = row.id
  try {
    const resp = await updateBillCategory(row.id, row.categoryId)
    if (resp.success) {
      row.categorySyncStatus = 'manual'
      ElMessage.success('分类已确认并同步')
      return
    }
    ElMessage.error(resp.message || '同步失败')
  } catch {
    ElMessage.error('同步请求失败')
  } finally {
    syncingId.value = null
  }
}

async function handleBatchConfirm() {
  if (selectedIds.value.length === 0) return
  batchLoading.value = true
  try {
    const resp = await batchConfirmBillCategory(selectedIds.value)
    if (resp.success) {
      ElMessage.success('批量确认完成，已处理 ' + (resp.data?.confirmedCount || 0) + ' 条')
      clearSelection()
      await loadBills()
      return
    }
    ElMessage.error(resp.message || '批量确认失败')
  } catch {
    ElMessage.error('批量确认请求失败')
  } finally {
    batchLoading.value = false
  }
}

function prevPage() {
  if (page.value > 1) { page.value -= 1; loadBills() }
}
function nextPage() {
  page.value += 1; loadBills()
}

onMounted(async () => {
  await loadCategories()
  await loadBills()
})
</script>

<style scoped>
.review-page { display: grid; }
.page-head { margin-bottom: var(--space-4); }
.page-head h2 { margin: 0; font-size: 26px; font-weight: 700; color: var(--text-900); }
.page-head p { margin: 6px 0 0; color: var(--text-500); font-size: 14px; }
.batch-card { margin-bottom: var(--space-4); }
.batch-header { display: flex; justify-content: space-between; align-items: center; }
.batch-title { font-weight: 600; font-size: 15px; color: var(--text-800); }
.batch-hint { margin-top: 8px; font-size: 13px; color: var(--text-400); }
.batch-bar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 8px 0;
  margin-bottom: 8px;
}
.selected-info {
  font-size: 14px;
  color: var(--text-500);
}
</style>
