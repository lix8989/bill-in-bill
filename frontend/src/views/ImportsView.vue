<template>
  <section class='imports-page fade-in'>
    <div class='page-header'>
      <div>
        <h2>导入说帐</h2>
        <p class='page-description'>导入微信或支付宝账单文件，系统将自动解析并分类</p>
      </div>
    </div>

    <div class='import-grid'>
      <el-card shadow='never'>
        <template #header>
          <div class='card-head'>
            <div>
              <h3>微信账单导入</h3>
              <p class='card-copy'>上传微信导出的 Excel 文件（.xlsx格式）</p>
            </div>
          </div>
        </template>
        <div class='upload-zone' :class='{ "has-file": !!wechatFile }' @click='triggerWechatInput'>
          <div class='upload-icon' v-html='wechatFile ? "📄" : "📄"'></div>
          <p class='upload-hint'>{{ wechatFile ? wechatFile.name : '拖拽文件到此处，或点击选择文件' }}</p>
          <p class='upload-format'>支持 .xlsx 格式，最大 10MB</p>
          <input ref='wechatInputRef' type='file' accept='.xlsx' hidden @change='onWechatFileChange'>
        </div>
        <div v-if='wechatFile' class='upload-actions'>
          <el-button size='small' @click='clearWechatFile'>清除</el-button>
        </div>
        <el-button
          class='import-btn wechat'
          :loading='wechatSubmitting'
          :disabled='!wechatFile'
          @click='submitWechatImport'
        >导入微信账单</el-button>
      </el-card>

      <el-card shadow='never'>
        <template #header>
          <div class='card-head'>
            <div>
              <h3>支付宝账单导入</h3>
              <p class='card-copy'>上传支付宝导出的 CSV 文件</p>
            </div>
          </div>
        </template>
        <div class='upload-zone' :class='{ "has-file": !!alipayFile }' @click='triggerAlipayInput'>
          <div class='upload-icon' v-html='alipayFile ? "📊" : "📊"'></div>
          <p class='upload-hint'>{{ alipayFile ? alipayFile.name : '拖拽文件到此处，或点击选择文件' }}</p>
          <p class='upload-format'>支持 .csv 格式，最大 10MB</p>
          <input ref='alipayInputRef' type='file' accept='.csv,.txt' hidden @change='onAlipayFileChange'>
        </div>
        <div v-if='alipayFile' class='upload-actions'>
          <el-button size='small' @click='clearAlipayFile'>清除</el-button>
        </div>
        <el-button
          class='import-btn alipay'
          :loading='alipaySubmitting'
          :disabled='!alipayFile'
          @click='submitAlipayImport'
        >导入支付宝账单</el-button>
      </el-card>
    </div>

    <el-card v-if='result' shadow='never'>
      <template #header>
        <div class='card-head'>
          <h3>导入结果</h3>
        </div>
      </template>
      <div class='result-stats'>
        <div class='result-stat'><span class='result-label'>总记录数</span><span class='result-value'>{{ result.totalCount }}</span></div>
        <div class='result-stat'><span class='result-label'>成功</span><span class='result-value success'>{{ result.successCount }}</span></div>
        <div class='result-stat'><span class='result-label'>失败</span><span class='result-value danger'>{{ result.failCount }}</span></div>
      </div>
      <el-alert v-if='result.failCount > 0' type='warning' show-icon :closable='false' title='本次导入包含重复或异常记录，已成功导入可用数据。' />
      <div v-if='result.failDetails?.length' class='table-scroll-container'>
        <el-table :data='result.failDetails' stripe>
          <el-table-column prop='rowNumber' label='失败行号' width='120' />
          <el-table-column prop='reason' label='失败原因' min-width='240' />
        </el-table>
      </div>
    </el-card>

    <el-card shadow='never' class='table-card'>
      <template #header>
        <div class='card-head'>
          <h3>导入历史</h3>
        </div>
      </template>
      <div class='table-scroll-container'>
        <el-table :data='history' stripe empty-text='暂无导入历史'>
        <el-table-column prop='createdAt' label='导入时间' min-width='180' />
        <el-table-column prop='sourceFileName' label='文件名' min-width='260' show-overflow-tooltip />
        <el-table-column label='来源' width='100'>
          <template #default='{ row }'>
            <el-tag v-if='row.source' :type='row.source === "微信" ? "primary" : "success"' size='small'>{{ row.source }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop='totalCount' label='总记录数' width='110' />
        <el-table-column prop='successCount' label='成功数' width='100' />
        <el-table-column prop='failCount' label='失败数' width='100' />
        <el-table-column prop='message' label='结果说明' min-width='180' />
      </el-table>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchImportHistory, importWechatBill, importAlipayBill } from '../api/import'

const wechatFile = ref(null)
const alipayFile = ref(null)
const wechatSubmitting = ref(false)
const alipaySubmitting = ref(false)
const wechatInputRef = ref(null)
const alipayInputRef = ref(null)
const result = ref(null)
const history = ref([])

function triggerWechatInput() {
  wechatInputRef.value?.click()
}

function triggerAlipayInput() {
  alipayInputRef.value?.click()
}

function clearWechatFile() {
  wechatFile.value = null
  if (wechatInputRef.value) wechatInputRef.value.value = ''
}

function clearAlipayFile() {
  alipayFile.value = null
  if (alipayInputRef.value) alipayInputRef.value.value = ''
}

function onWechatFileChange(event) {
  wechatFile.value = event.target.files?.[0] || null
}

function onAlipayFileChange(event) {
  alipayFile.value = event.target.files?.[0] || null
}

async function loadHistory() {
  try {
    const response = await fetchImportHistory()
    if (response.success) {
      history.value = response.data || []
      return
    }
    ElMessage.error(response.message || '加载导入历史失败')
  } catch {
    ElMessage.error('加载导入历史失败，请检查后端服务')
  }
}

async function submitWechatImport() {
  if (!wechatFile.value) {
    ElMessage.warning('请先选择要导入的 Excel 文件')
    return
  }

  wechatSubmitting.value = true
  result.value = null
  try {
    const response = await importWechatBill(wechatFile.value)
    if (response.data?.success) {
      result.value = response.data.data
      if ((response.data.data?.failCount || 0) > 0) {
        ElMessage.warning('导入完成，部分记录未导入')
      } else {
        ElMessage.success('导入完成')
      }
      await loadHistory()
      return
    }
    ElMessage.error(response.data?.message || '导入失败')
  } catch (error) {
    const message = error?.response?.data?.message || '导入失败，请检查后端服务'
    ElMessage.error(message)
  } finally {
    wechatSubmitting.value = false
  }
}

async function submitAlipayImport() {
  if (!alipayFile.value) {
    ElMessage.warning('请先选择要导入的 CSV 文件')
    return
  }

  alipaySubmitting.value = true
  result.value = null
  try {
    const response = await importAlipayBill(alipayFile.value)
    if (response.data?.success) {
      result.value = response.data.data
      if ((response.data.data?.failCount || 0) > 0) {
        ElMessage.warning('导入完成，部分记录未导入')
      } else {
        ElMessage.success('导入完成')
      }
      await loadHistory()
      return
    }
    ElMessage.error(response.data?.message || '导入失败')
  } catch (error) {
    const message = error?.response?.data?.message || '导入失败，请检查后端服务'
    ElMessage.error(message)
  } finally {
    alipaySubmitting.value = false
  }
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.imports-page {
  display: grid;
  gap: var(--space-4);
}

.import-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.card-head h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-800);
  margin: 0;
}

.card-copy {
  font-size: 12px;
  color: var(--text-400);
  margin: 4px 0 0;
}

.upload-zone {
  border: 2px dashed var(--border-200);
  border-radius: var(--radius-xl);
  padding: 36px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: var(--space-3);
}

.upload-zone:hover {
  border-color: var(--brand-400);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.03), rgba(37, 99, 235, 0.01));
}

.upload-zone.has-file {
  border-color: var(--brand-400);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(37, 99, 235, 0.02));
}

.upload-icon {
  font-size: 40px;
  margin-bottom: 12px;
  line-height: 1;
}

.upload-hint {
  font-size: 14px;
  color: var(--text-500);
  margin: 0 0 6px;
  line-height: 1.4;
}

.upload-zone.has-file .upload-hint {
  font-weight: 600;
  color: var(--brand-600);
  font-size: 13px;
}

.upload-format {
  font-size: 12px;
  color: var(--text-400);
  margin: 0;
}

.upload-actions {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-3);
}

.import-btn {
  width: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.15s ease;
  border: none;
  white-space: nowrap;
  position: relative;
  overflow: hidden;
}

.import-btn.wechat {
  background: var(--gradient-brand);
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.20);
}

.import-btn.wechat:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.30);
  transform: translateY(-1px);
}

.import-btn.alipay {
  background: var(--gradient-success);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.20);
}

.import-btn.alipay:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.30);
  transform: translateY(-1px);
}

.import-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.result-stats {
  display: flex;
  gap: var(--space-8);
  margin-bottom: var(--space-4);
}

.result-stat {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.result-label {
  font-size: 13px;
  color: var(--text-400);
}

.result-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-900);
}

.result-value.success { color: var(--success-600); }
.result-value.danger { color: var(--danger-600); }

@media (max-width: 960px) {
  .import-grid { grid-template-columns: 1fr; }
}
</style>
