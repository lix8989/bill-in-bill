<template>
  <section class='categories-page fade-in'>
    <div class='page-header'>
      <div>
        <h2>说帐分类</h2>
        <p class='page-description'>管理账单分类，支持新增、编辑和删除分类。</p>
      </div>
    </div>

    <el-card shadow='never'>
      <template #header>
        <div class='card-head'>
          <h3>新增分类</h3>
        </div>
      </template>
      <el-form inline>
        <el-form-item label='分类名称'>
          <el-input v-model='newCategoryName' placeholder='输入新分类名称' clearable />
        </el-form-item>
        <el-form-item>
          <el-button type='primary' :loading='submitting' @click='submitCategory'>新增分类</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow='never' class='table-card'>
      <template #header>
        <div class='card-head'>
          <h3>分类列表</h3>
        </div>
      </template>
      <div class='table-scroll-container'>
        <el-table :data='categories' stripe>
        <el-table-column prop='id' label='ID' width='100' />
        <el-table-column label='分类名称' min-width='220'>
          <template #default='{ row }'>
            <el-input v-if='editingId === row.id' v-model='editingName' />
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label='操作' width='220'>
          <template #default='{ row }'>
            <el-button v-if='editingId !== row.id' link type='primary' @click='startEdit(row)'>编辑</el-button>
            <el-button v-if='editingId === row.id' link type='primary' @click='saveEdit(row.id)'>保存</el-button>
            <el-button v-if='editingId === row.id' link @click='cancelEdit'>取消</el-button>
            <el-button link type='danger' @click='removeCategory(row.id)'>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createCategory, deleteCategory, fetchCategories, updateCategory } from '../api/category'

const categories = ref([])
const newCategoryName = ref('')
const submitting = ref(false)
const editingId = ref(null)
const editingName = ref('')

async function loadCategories() {
  try {
    const response = await fetchCategories()
    if (response.success) {
      categories.value = response.data || []
      return
    }
    ElMessage.error(response.message || '加载分类失败')
  } catch (error) {
    ElMessage.error('加载分类失败，请检查后端服务')
  }
}

async function submitCategory() {
  if (!newCategoryName.value.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  submitting.value = true
  try {
    const response = await createCategory({ name: newCategoryName.value.trim() })
    if (response.success) {
      ElMessage.success('分类已创建')
      newCategoryName.value = ''
      await loadCategories()
      return
    }
    ElMessage.error(response.message || '创建分类失败')
  } catch (error) {
    ElMessage.error('创建分类失败，请检查后端服务')
  } finally {
    submitting.value = false
  }
}

function startEdit(row) {
  editingId.value = row.id
  editingName.value = row.name
}

function cancelEdit() {
  editingId.value = null
  editingName.value = ''
}

async function saveEdit(id) {
  if (!editingName.value.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    const response = await updateCategory(id, { name: editingName.value.trim() })
    if (response.success) {
      ElMessage.success('分类已更新')
      cancelEdit()
      await loadCategories()
      return
    }
    ElMessage.error(response.message || '更新分类失败')
  } catch (error) {
    ElMessage.error('更新分类失败，请检查后端服务')
  }
}

async function removeCategory(id) {
  try {
    const response = await deleteCategory(id)
    if (response.success) {
      ElMessage.success('分类已删除')
      await loadCategories()
      return
    }
    ElMessage.error(response.message || '删除分类失败')
  } catch (error) {
    ElMessage.error('删除分类失败，请检查后端服务')
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.categories-page {
  display: grid;
  gap: var(--space-4);
}

.card-head h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-800);
  margin: 0;
}
</style>
