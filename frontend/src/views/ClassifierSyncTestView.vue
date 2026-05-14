<template>
  <section class="sync-test-page">
    <el-card shadow="never">
      <div class="header-row">
        <div>
          <h2>分类同步调试</h2>
          <p>手动构造分类同步请求并发送，验证回写链路是否正常。</p>
        </div>
      </div>

      <el-form label-width="120px">
        <el-form-item label="同步批次号">
          <el-input v-model="syncBatchNo" placeholder="留空自动生成" />
        </el-form-item>
      </el-form>

      <el-divider>同步条目</el-divider>

      <div v-for="(item, idx) in items" :key="idx" style="margin-bottom: 16px;">
        <el-card shadow="always">
          <div class="item-header">
            <span>条目 #{{ idx + 1 }}</span>
            <el-button type="danger" size="small" @click="removeItem(idx)">删除</el-button>
          </div>
          <el-form label-width="110px">
            <el-form-item label="账单 ID">
              <el-input v-model="item.billId" placeholder="优先匹配" />
            </el-form-item>
            <el-form-item label="ImportKey">
              <el-input v-model="item.importKey" placeholder="billId为空时使用" />
            </el-form-item>
            <el-form-item label="分类编码">
              <el-select v-model="item.categoryCode" filterable placeholder="选择或输入">
                <el-option v-for="cat in categories" :key="cat.categoryCode" :label="cat.name + ' (' + cat.categoryCode + ')'" :value="cat.categoryCode" />
              </el-select>
            </el-form-item>
            <el-form-item label="置信度">
              <el-slider v-model="item.confidence" :min="0" :max="1" :step="0.01" style="width: 300px;" />
            </el-form-item>
            <el-form-item label="分类原因">
              <el-input v-model="item.reason" type="textarea" :rows="2" placeholder="模型分类原因说明" />
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <el-button @click="addItem" style="margin-bottom: 16px;">添加条目</el-button>
      <el-button type="primary" :loading="loading" @click="sendSync">发送同步</el-button>
    </el-card>

    <el-card v-if="result" shadow="never" style="margin-top: 16px;">
      <h3>同步结果</h3>
      <pre class="result-pre">{{ JSON.stringify(result, null, 2) }}</pre>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchClassifierMappings, syncClassifierCategories } from '../api/classifier'

const categories = ref([])
const syncBatchNo = ref('')
const items = ref([])
const loading = ref(false)
const result = ref(null)

function addItem() {
  items.value.push({
    billId: null,
    importKey: '',
    categoryCode: '',
    confidence: 0.85,
    reason: '',
  })
}

function removeItem(idx) {
  items.value.splice(idx, 1)
}

async function sendSync() {
  if (items.value.length === 0) {
    ElMessage.warning('请至少添加一个同步条目')
    return
  }
  loading.value = true
  result.value = null
  try {
    const payload = {
      syncBatchNo: syncBatchNo.value || undefined,
      items: items.value.map(item => ({
        billId: item.billId ? Number(item.billId) : undefined,
        importKey: item.importKey || undefined,
        categoryCode: item.categoryCode,
        confidence: item.confidence,
        reason: item.reason || undefined,
      })),
    }
    const response = await syncClassifierCategories(payload)
    if (response.success) {
      result.value = response.data
      ElMessage.success('同步请求已处理')
      return
    }
    ElMessage.error(response.message || '同步失败')
  } catch {
    ElMessage.error('同步请求失败，请检查后端服务')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const resp = await fetchClassifierMappings()
    if (resp.success) {
      categories.value = resp.data || []
    }
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.sync-test-page {
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
.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
  font-weight: 600;
  color: var(--text-800);
}
.result-pre {
  background: var(--bg-subtle);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  overflow-x: auto;
}
</style>
