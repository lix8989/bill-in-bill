<template>
  <section class="kr-page">
    <div class="page-head">
      <div>
        <h2>关键字规则配置</h2>
        <p>管理自动分类使用的关键字匹配规则。当大模型不可用或未启用时，系统将使用此处配置的规则进行兜底分类。</p>
      </div>
      <div style="display: flex; gap: 8px;">
        <el-button @click="syncCategories" :loading="syncing" icon="Refresh">同步分类设置</el-button>
        <el-button @click="loadRules" :loading="loading">刷新</el-button>
      </div>
    </div>

    <el-alert
      title="规则优先级：大模型分类 > 关键字规则 > 未分类"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 16px;"
    />

    <div class="toolbar">
      <el-checkbox v-model="showAll" @change="loadRules">
        显示全部分类（{{ noRuleCount }} 个分类暂无规则）
      </el-checkbox>
      <span style="color: #909399; font-size: 13px;">默认仅显示已配置关键字规则的分类</span>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="table-scroll-container">
        <el-table :data="filteredRules" stripe empty-text="暂无匹配的规则，请先在分类设置中添加分类">
      <el-table-column label="分类编码" width="140">
        <template #default="{ row }">
          <span class="category-code">{{ row.categoryCode }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类名称" width="120" />
      <el-table-column label="置信度" width="130">
        <template #header>
          <span>
            置信度
            <el-tooltip content="≥85% 自动确认，60%~85% 待复核，<60% 标记失败" placement="top">
              <el-icon style="color: #909399; cursor: help; margin-left: 2px;"><WarningFilled /></el-icon>
            </el-tooltip>
          </span>
        </template>
        <template #default="{ row }">
          <template v-if="row.keywords && row.keywords.length > 0">
            <span :style="{ color: row.confidence >= 0.85 ? '#67c23a' : row.confidence >= 0.6 ? '#e6a23c' : '#f56c6c' }">
              {{ (row.confidence * 100).toFixed(0) }}%
            </span>
          </template>
          <span v-else style="color: #c0c4cc;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="关键字" min-width="300">
        <template #default="{ row }">
          <template v-if="row.keywords && row.keywords.length > 0">
            <el-tag
              v-for="kw in row.keywords"
              :key="kw"
              size="small"
              style="margin: 2px 4px 2px 0;"
            >{{ kw }}</el-tag>
          </template>
          <span v-else style="color: #c0c4cc;">暂无规则，请添加</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="editRule(row)">
            {{ row.keywords && row.keywords.length > 0 ? '编辑' : '添加规则' }}
          </el-button>
          <el-button
            v-if="row.keywords && row.keywords.length > 0"
            link
            type="danger"
            size="small"
            @click="handleDeleteCategory(row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>
    </el-card>

    <el-dialog v-model="editVisible" :title="editForm.categoryCode + ' - ' + editForm.categoryName" width="600px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="分类编码">
          <el-input v-model="editForm.categoryCode" disabled />
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="editForm.categoryName" placeholder="输入分类名称" />
        </el-form-item>
        <el-form-item label="置信度">
          <el-slider v-model="editForm.confidence" :min="0" :max="1" :step="0.01" style="width: 300px" show-input />
        </el-form-item>
        <el-form-item label="关键字列表">
          <div style="width: 100%;">
            <div style="display: flex; gap: 8px; margin-bottom: 8px; flex-wrap: wrap;">
              <el-tag
                v-for="(kw, idx) in editForm.keywords"
                :key="idx"
                closable
                @close="removeKeyword(idx)"
                style="margin: 2px 0;"
              >{{ kw }}</el-tag>
            </div>
            <div style="display: flex; gap: 8px;">
              <el-input v-model="newKeyword" placeholder="输入新关键字" size="small" style="width: 200px;" @keyup.enter="addNewKeyword" />
              <el-button size="small" @click="addNewKeyword">添加</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchKeywordRules, saveKeywordCategory, deleteKeywordCategory, refreshKeywordRules } from '../api/keyword'
import { Refresh, WarningFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const syncing = ref(false)
const rules = ref([])
const showAll = ref(false)

const filteredRules = computed(() => {
  if (showAll.value) return rules.value
  return rules.value.filter(r => r.keywords && r.keywords.length > 0)
})

const noRuleCount = computed(() => {
  return rules.value.filter(r => !r.keywords || r.keywords.length === 0).length
})

const editVisible = ref(false)
const editForm = ref({ categoryCode: '', categoryName: '', confidence: 0.85, keywords: [] })
const newKeyword = ref('')
const saving = ref(false)

async function loadRules() {
  loading.value = true
  try {
    const resp = await fetchKeywordRules()
    if (resp.success) rules.value = resp.data || []
    else ElMessage.error(resp.message || '加载规则失败')
  } catch {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

async function syncCategories() {
  syncing.value = true
  try {
    await refreshKeywordRules()
    await loadRules()
    ElMessage.success('分类同步完成')
  } catch {
    ElMessage.error('同步请求失败')
  } finally {
    syncing.value = false
  }
}

function editRule(row) {
  editForm.value = {
    categoryCode: row.categoryCode,
    categoryName: row.categoryName,
    confidence: row.confidence || 0.85,
    keywords: [...(row.keywords || [])],
  }
  newKeyword.value = ''
  editVisible.value = true
}

function addNewKeyword() {
  const kw = newKeyword.value.trim()
  if (!kw) return
  if (editForm.value.keywords.includes(kw)) {
    ElMessage.warning('该关键字已存在')
    return
  }
  editForm.value.keywords.push(kw)
  newKeyword.value = ''
}

function removeKeyword(idx) {
  editForm.value.keywords.splice(idx, 1)
}

async function saveEdit() {
  if (!editForm.value.categoryCode) {
    ElMessage.warning('分类编码不能为空')
    return
  }
  if (editForm.value.keywords.length === 0) {
    ElMessage.warning('至少需要一个关键字')
    return
  }
  saving.value = true
  try {
    const resp = await saveKeywordCategory({
      categoryCode: editForm.value.categoryCode,
      categoryName: editForm.value.categoryName,
      confidence: editForm.value.confidence,
      keywords: editForm.value.keywords,
    })
    if (resp.success) {
      ElMessage.success('保存成功')
      editVisible.value = false
      await loadRules()
      return
    }
    ElMessage.error(resp.message || '保存失败')
  } catch {
    ElMessage.error('保存请求失败')
  } finally {
    saving.value = false
  }
}

async function handleDeleteCategory(row) {
  try {
    await ElMessageBox.confirm(
      '确定删除分类 ' + row.categoryCode + ' 的所有关键字规则？删除后该分类将不再出现在关键字规则列表中。',
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    const resp = await deleteKeywordCategory(row.categoryCode)
    if (resp.success) {
      ElMessage.success('规则已删除')
      await loadRules()
      return
    }
    ElMessage.error(resp.message || '删除失败')
  } catch {
    ElMessage.error('删除请求失败')
  }
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.kr-page {
  display: grid;
  gap: var(--space-4);
}
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0;
}
.page-head h2 { margin: 0; font-size: 26px; font-weight: 700; color: var(--text-900); }
.page-head p { margin: 6px 0 0; color: var(--text-500); font-size: 14px; }
.toolbar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 8px 0;
}

/* ── 分类编码样式 - 纯文本展示 ── */
.category-code {
  font-family: 'Courier New', 'JetBrains Mono', monospace;
  font-size: 13px;
  color: var(--ink-medium);
  font-weight: 500;
}

/* 置信度指示器优化 */
:deep(.el-table__cell) {
  font-family: 'Noto Serif SC', serif;
}

:deep(.el-table .cell) {
  font-size: 13px;
}
</style>
