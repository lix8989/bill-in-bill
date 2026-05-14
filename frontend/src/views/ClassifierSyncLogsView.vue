<template>
  <section class='sync-logs-page'>
    <el-card shadow='never'>
      <div class='header-row'>
        <div>
          <h2>分类同步日志</h2>
          <p>查看每次模型分类结果回写到账单系统的处理记录。</p>
        </div>
        <el-button type='primary' @click='loadLogs'>刷新</el-button>
      </div>

      <div class='table-scroll-container'>
        <el-table :data='logs' stripe empty-text='暂无同步日志'>
        <el-table-column prop='id' label='ID' width='80' />
        <el-table-column prop='syncBatchNo' label='同步批次' min-width='180' />
        <el-table-column prop='billId' label='账单ID' width='100' />
        <el-table-column prop='requestedCategoryCode' label='分类编码' min-width='160' />
        <el-table-column prop='resolvedCategoryId' label='分类ID' width='100' />
        <el-table-column prop='confidence' label='置信度' width='100' />
        <el-table-column prop='status' label='状态' width='140' />
        <el-table-column prop='message' label='结果说明' min-width='180' />
        <el-table-column prop='reason' label='分类原因' min-width='260' show-overflow-tooltip />
      </el-table>
      </div>

      <div class='pager'>
        <el-button :disabled='page <= 1' @click='prevPage'>上一页</el-button>
        <span>第 {{ page }} 页 / 共 {{ totalPages }} 页（{{ total }} 条）</span>
        <el-button :disabled='page >= totalPages' @click='nextPage'>下一页</el-button>
        <el-select v-model='pageSize' style='width: 120px' @change='onPageSizeChange'>
          <el-option :value='10' label='10 条/页' />
          <el-option :value='20' label='20 条/页' />
          <el-option :value='50' label='50 条/页' />
        </el-select>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchClassifierSyncLogs } from '../api/classifier'

const logs = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

async function loadLogs() {
  try {
    const response = await fetchClassifierSyncLogs({ page: page.value, pageSize: pageSize.value })
    if (response.success) {
      logs.value = response.data?.records || []
      total.value = response.data?.total || 0
      return
    }
    ElMessage.error(response.message || '加载同步日志失败')
  } catch {
    ElMessage.error('加载同步日志失败，请检查后端服务')
  }
}

function prevPage() {
  if (page.value > 1) { page.value -= 1; loadLogs() }
}

function nextPage() {
  if (page.value < totalPages.value) { page.value += 1; loadLogs() }
}

function onPageSizeChange() {
  page.value = 1
  loadLogs()
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.sync-logs-page {
  display: grid;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.header-row h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-900);
}

.header-row p {
  margin: 6px 0 0;
  color: var(--text-500);
  font-size: 14px;
}
</style>
