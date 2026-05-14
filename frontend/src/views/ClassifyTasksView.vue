<template>
  <section class="tasks-page">

    <div class="page-head">
      <div>
        <h2>自动说帐</h2>
        <p>基于大模型对账单执行自动分类，支持 OpenAI 兼容接口。</p>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-num">{{ stats.total }}</span>
        <span class="stat-lbl">总账单</span>
      </div>
      <div class="stat-card ok">
        <span class="stat-num">{{ stats.classified }}</span>
        <span class="stat-lbl">已分类</span>
      </div>
      <div class="stat-card warn">
        <span class="stat-num">{{ stats.unclassified }}</span>
        <span class="stat-lbl">未分类</span>
      </div>
      <div class="stat-card warn">
        <span class="stat-num">{{ stats.pendingReview }}</span>
        <span class="stat-lbl">待复核</span>
      </div>
      <div class="stat-card danger">
        <span class="stat-num">{{ stats.failed }}</span>
        <span class="stat-lbl">异常</span>
      </div>
    </div>

    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-collapse v-model="activeConfigPanel">
        <el-collapse-item title="大模型配置" name="llm">
          <el-form label-width="140px">
            <el-form-item label="启用大模型">
              <el-switch v-model="llmEnabled" @change="saveLlmSettings" />
              <span style="margin-left: 8px; color: #909399; font-size: 13px;">
                开启后调用大模型分类，关闭时使用内置关键字规则
              </span>
            </el-form-item>
            <el-form-item label="API 地址">
              <el-input v-model="llmSettings.api_url" placeholder="https://api.openai.com/v1/chat/completions" @change="saveLlmSettings" />
              <div class="field-tip">支持 OpenAI 及兼容协议的服务（DeepSeek / 阿里百炼 / Ollama 等）</div>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="llmSettings.api_key" type="password" placeholder="sk-..." show-password @change="saveLlmSettings" />
            </el-form-item>
            <el-form-item label="模型名称">
              <el-input v-model="llmSettings.model_name" placeholder="gpt-4o-mini" @change="saveLlmSettings" />
              <div class="field-tip">可选：gpt-4o-mini / deepseek-chat / qwen-turbo 等</div>
            </el-form-item>
            <el-form-item label="最大 Token">
              <el-input-number v-model="maxTokens" :min="256" :max="4096" :step="128" @change="saveLlmSettings" />
            </el-form-item>
            <el-form-item label="温度">
              <el-slider v-model="temperature" :min="0" :max="1" :step="0.05" style="width: 300px" @change="onTemperatureChange" />
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card shadow="never" style="margin-bottom: 16px;">
      <div class="section-title">分类范围</div>
      <el-form inline>
        <el-form-item label="年份">
          <el-date-picker
            v-model="filterYear"
            type="year"
            placeholder="全部年份"
            value-format="YYYY"
            clearable
            style="width: 150px"
            @change="onFilterChange"
          />
        </el-form-item>
        <el-form-item label="月份">
          <el-date-picker
            v-model="filterMonth"
            type="month"
            placeholder="全部月份"
            value-format="YYYY-MM"
            clearable
            style="width: 160px"
            @change="onFilterChange"
          />
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="reclassifyMode" @change="onFilterChange">
            <el-radio :value="false" border>仅未分类与待复核账单</el-radio>
            <el-radio :value="true" border>重新分类全部账单</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="reclassifyMode"
        title="重新分类全部账单"
        type="info"
        description="勾选后将覆盖已有分类结果，对所有匹配条件的账单重新执行分类。"
        show-icon
        :closable="false"
        style="margin-bottom: 12px;"
      />

      <div class="preview-bar">
        <span class="preview-count">
          <template v-if="previewLoading">计算中...</template>
          <template v-else-if="previewCount !== null">
            当前条件下将处理 <strong>{{ previewCount }}</strong> 条账单
            <span v-if="previewCount === 0 && !reclassifyMode" style="color: #e6a23c; display: block; margin-top: 4px;">
              ⚠️ 导入时所有账单被分配了默认分类。请选择「重新分类全部账单」，或先<a @click.stop="handleReset" style="color: #409eff; cursor: pointer; text-decoration: underline;">一键重置所有分类</a>后再执行。
            </span>
            <span v-else-if="previewCount === 0 && reclassifyMode" style="color: #e6a23c; display: block; margin-top: 4px;">
              ⚠️ 所选时间范围内没有账单记录。请先<a href="/imports" style="color: #409eff;">导入账单</a>，或清除年份/月份筛选条件。
            </span>
          </template>
          <template v-else>选择筛选条件后自动计算可分类的账单数</template>
        </span>
        <div style="display: flex; gap: 8px; align-items: center;">
          <el-button :loading="resetting" size="small" @click="handleReset" :disabled="previewCount === 0 || previewCount === null">
            <el-icon style="margin-right: 4px;"><Refresh /></el-icon>重置全部分类
          </el-button>
          <el-tooltip content="清除所有账单的分类结果，恢复到未分类状态" placement="top">
            <span style="font-size: 12px; color: #909399;">清除分类结果后重新开始</span>
          </el-tooltip>
          <el-button type="primary" :loading="running" :disabled="previewCount === 0 || previewCount === null" @click="startClassify">
            执行自动分类
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <div class="section-title">历史任务</div>
      <div class="table-scroll-container">
        <el-table :data="tasks" stripe empty-text="暂无分类任务，设置好范围后点击「执行自动分类」">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="taskNo" label="任务编号" min-width="150" />
        <el-table-column prop="totalCount" label="总量" width="70" />
        <el-table-column prop="successCount" label="成功" width="70" />
        <el-table-column prop="failCount" label="失败" width="70" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'success'" type="success" size="small">成功</el-tag>
            <el-tag v-else-if="row.status === 'partial'" type="warning" size="small">部分成功</el-tag>
            <el-tag v-else-if="row.status === 'failed'" type="danger" size="small">失败</el-tag>
            <el-tag v-else-if="row.status === 'running'" type="primary" size="small">运行中</el-tag>
            <span v-else>{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" min-width="150" />
        <el-table-column prop="finishedAt" label="完成时间" min-width="150" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showTaskDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" :title="'任务详情 - ' + (detail?.taskNo || '')" width="600px">
      <pre class="result-pre">{{ JSON.stringify(detail, null, 2) || '无数据' }}</pre>
    </el-dialog>

  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { createClassifyTask, fetchClassifyTasks, fetchLlmSettings, updateLlmSettings, fetchClassifyStats, fetchClassifyPreview, resetAllCategories } from '../api/classifier'

const tasks = ref([])
const running = ref(false)
const resetting = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const activeConfigPanel = ref([])

const stats = ref({ total: 0, classified: 0, unclassified: 0, pendingReview: 0, failed: 0 })
const previewCount = ref(null)
const previewLoading = ref(false)

const filterYear = ref(null)
const filterMonth = ref(null)
const reclassifyMode = ref(false)

const llmSettings = ref({
  api_url: 'https://api.openai.com/v1/chat/completions',
  api_key: '',
  model_name: 'gpt-4o-mini',
  max_tokens: '1024',
  temperature: '0.1',
  enabled: 'false',
})
const llmEnabled = ref(false)
const temperature = ref(0.1)
const maxTokens = ref(1024)

async function loadLlmSettings() {
  try {
    const resp = await fetchLlmSettings()
    if (resp.success && resp.data) {
      llmSettings.value = { ...llmSettings.value, ...resp.data }
      llmEnabled.value = resp.data.enabled === 'true'
      temperature.value = parseFloat(resp.data.temperature || '0.1')
      maxTokens.value = parseInt(resp.data.max_tokens || '1024')
    }
  } catch { /* ignore */ }
}

async function saveLlmSettings() {
  const settings = {
    ...llmSettings.value,
    enabled: llmEnabled.value ? 'true' : 'false',
    temperature: String(temperature.value),
    max_tokens: String(maxTokens.value),
  }
  try {
    const resp = await updateLlmSettings(settings)
    if (resp.success) return
    ElMessage.error(resp.message || '保存配置失败')
  } catch {
    ElMessage.error('保存配置失败')
  }
}

function onTemperatureChange(val) {
  temperature.value = val
  saveLlmSettings()
}

async function loadStats() {
  try {
    const resp = await fetchClassifyStats()
    if (resp.success && resp.data) stats.value = resp.data
  } catch { /* ignore */ }
}

async function loadPreview() {
  previewLoading.value = true
  try {
    const params = { reclassify: reclassifyMode.value }
    if (filterYear.value) params.year = filterYear.value
    if (filterMonth.value) params.month = filterMonth.value
    const resp = await fetchClassifyPreview(params)
    if (resp.success && resp.data) previewCount.value = resp.data.count
  } catch {
    previewCount.value = null
  } finally {
    previewLoading.value = false
  }
}

function onFilterChange() {
  previewCount.value = null
  loadPreview()
}

async function loadTasks() {
  try {
    const resp = await fetchClassifyTasks()
    if (resp.success) tasks.value = resp.data || []
  } catch {
    ElMessage.error('加载任务列表失败')
  }
}

async function startClassify() {
  running.value = true
  try {
    const payload = { reclassify: reclassifyMode.value }
    if (filterYear.value) payload.year = parseInt(filterYear.value)
    if (filterMonth.value) payload.month = filterMonth.value

    // 显示提示信息
    const loadingMsg = ElMessage({
      message: '正在执行自动分类，请稍候...',
      type: 'info',
      duration: 0, // 不自动关闭
      showClose: false,
    })

    const resp = await createClassifyTask(payload)

    // 关闭加载提示
    loadingMsg.close()

    if (resp.success) {
      detail.value = resp.data
      detailVisible.value = true
      if (resp.data.totalCount === 0) {
        ElMessage.warning('未找到可分类的账单')
      } else {
        ElMessage.success('分类完成，处理 ' + resp.data.totalCount + ' 条账单')
      }
      await loadTasks()
      await loadStats()
      await loadPreview()
      return
    }
    ElMessage.error(resp.message || '分类失败')
  } catch (error) {
    // 更详细的错误处理
    console.error('自动分类错误:', error)

    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      ElMessage.error('分类任务执行时间过长，请稍后在"历史任务"中查看结果')
      // 即使超时也尝试刷新任务列表
      await loadTasks()
    } else if (error.response?.status === 0) {
      ElMessage.error('无法连接到后端服务，请确认服务已启动')
    } else {
      ElMessage.error('请求失败: ' + (error.message || '未知错误'))
    }
  } finally {
    running.value = false
  }
}

async function handleReset() {
  resetting.value = true
  try {
    const resp = await resetAllCategories()
    if (resp.success) {
      ElMessage.success('已重置 ' + (resp.data?.resetCount || 0) + ' 条账单分类，可重新执行自动分类')
      await loadStats()
      await loadPreview()
      return
    }
    ElMessage.error(resp.message || '重置失败')
  } catch {
    ElMessage.error('重置请求失败')
  } finally {
    resetting.value = false
  }
}

function showTaskDetail(row) {
  detail.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadLlmSettings()
  loadStats()
  loadPreview()
  loadTasks()
})
</script>

<style scoped>
.tasks-page {
  display: grid;
  gap: var(--space-4);
}
.page-head { margin-bottom: 0; }
.page-head h2 { margin: 0; }
.page-head p { margin: 6px 0 0; color: var(--text-500); }

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--space-3);
}
.stat-card {
  background: var(--bg-elevated);
  border: 1px solid var(--border-100);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  text-align: center;
}
.stat-card.ok { background: #f0f9eb; }
.stat-card.warn { background: #fdf6ec; }
.stat-card.danger { background: #fef0f0; }
.stat-num {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-900);
  line-height: 1.2;
}
.stat-lbl {
  display: block;
  font-size: 13px;
  color: var(--text-500);
  margin-top: 4px;
}

.section-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: var(--space-3);
  color: var(--text-800);
}

.field-tip {
  font-size: 12px;
  color: var(--text-400);
  margin-top: 4px;
  line-height: 1.4;
}

.preview-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  padding: 12px 16px;
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-100);
}
.preview-count {
  font-size: 14px;
  color: var(--text-500);
}
.preview-count strong {
  color: var(--text-900);
  font-size: 18px;
}

.result-pre {
  background: var(--bg-subtle);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  overflow-x: auto;
  white-space: pre-wrap;
  font-size: 13px;
  max-height: 400px;
  overflow-y: auto;
}
</style>
