<template>
  <el-dialog
    v-model="visible"
    title=""
    width="680px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="quick-bill-dialog"
  >
    <template #header>
      <div class="dialog-header">
        <span class="header-icon">✚</span>
        <span class="header-title">补记一笔</span>
        <el-button
          class="header-close"
          :icon="Close"
          text
          @click="handleClose"
        />
      </div>
    </template>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" class="bill-form">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="交易时间" prop="tradeTime">
            <el-date-picker
              v-model="form.tradeTime"
              type="datetime"
              placeholder="选择交易时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="金额" prop="amount">
            <el-input-number
              v-model="form.amount"
              :min="0"
              :precision="2"
              :controls-position="right"
              placeholder="请输入金额"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="交易对方" prop="counterparty">
            <el-input
              v-model="form.counterparty"
              placeholder="如：星巴克"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="商品名称" prop="productName">
            <el-input
              v-model="form.productName"
              placeholder="如：拿铁咖啡"
              clearable
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="收支类型" prop="incomeExpenseType">
            <el-radio-group v-model="form.incomeExpenseType" class="type-radio-group">
              <el-radio-button label="支出" class="type-radio expense">支出</el-radio-button>
              <el-radio-button label="收入" class="type-radio income">收入</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="支付方式" prop="source">
            <el-select v-model="form.source" placeholder="请选择支付方式" style="width: 100%">
              <el-option label="微信" value="微信">
                <span class="option-icon">💬</span>
                <span>微信</span>
              </el-option>
              <el-option label="支付宝" value="支付宝">
                <span class="option-icon">💙</span>
                <span>支付宝</span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="分类" prop="categoryId">
        <el-select
          v-model="form.categoryId"
          placeholder="请选择分类"
          style="width: 100%"
          filterable
          clearable
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          >
            <span class="category-option">
              <span class="category-code">{{ category.code }}</span>
              <span class="category-name">{{ category.name }}</span>
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          placeholder="请输入备注（可选）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" size="large">取消</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="submitting"
          size="large"
          class="btn-submit"
        >
          <span class="btn-icon">💾</span>
          <span>保存</span>
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { createBill } from '../api/bill'
import { fetchCategories } from '../api/category'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
const submitting = ref(false)
const categories = ref([])
const formRef = ref()

const form = reactive({
  tradeTime: '',
  counterparty: '',
  productName: '',
  amount: null,
  incomeExpenseType: '支出',
  source: '微信',
  categoryId: null,
  remark: ''
})

const rules = {
  tradeTime: [{ required: true, message: '请选择交易时间', trigger: 'change' }],
  counterparty: [{ required: true, message: '请输入交易对方', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  incomeExpenseType: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  source: [{ required: true, message: '请选择支付方式', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadCategories()
    resetForm()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const loadCategories = async () => {
  try {
    const result = await fetchCategories()
    if (result.success) {
      categories.value = result.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const resetForm = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  const second = String(now.getSeconds()).padStart(2, '0')

  Object.assign(form, {
    tradeTime: `${year}-${month}-${day} ${hour}:${minute}:${second}`,
    counterparty: '',
    productName: '',
    amount: null,
    incomeExpenseType: '支出',
    source: '微信',
    categoryId: null,
    remark: ''
  })

  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitting.value = true

    const billData = {
      tradeTime: form.tradeTime,
      counterparty: form.counterparty,
      productName: form.productName,
      amount: form.amount,
      incomeExpenseType: form.incomeExpenseType,
      source: form.source,
      categoryId: form.categoryId,
      remark: form.remark
    }

    const result = await createBill(billData)

    if (result.success) {
      ElMessage.success('记账成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(result.message || '记账失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('记账失败: ' + error.message)
    }
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.quick-bill-dialog {
  --dialog-bg: #FFFFFF;
  --border-color: #EDE8D8;
  --primary-color: #C84630;
  --text-color: #2D6A4F;
  --text-secondary: #6B8E8F;
  --expense-color: #C84630;
  --income-color: #2D6A4F;
}

/* 对话框整体样式 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

:deep(.el-dialog__body) {
  padding: 30px 40px;
}

:deep(.el-dialog__footer) {
  padding: 20px 40px;
  background: #F5F1E8;
  border-top: 1px solid #EDE8D8;
}

/* 自定义头部 */
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 40px;
  background: linear-gradient(135deg, #2D6A4F 0%, #3D7A5F 100%);
}

.header-icon {
  font-size: 24px;
  color: #D4A853;
}

.header-title {
  flex: 1;
  font-size: 20px;
  font-weight: bold;
  color: white;
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
}

.header-close {
  color: white !important;
  font-size: 20px;
}

.header-close:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* 表单样式 */
.bill-form {
  margin: 0;
}

:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-row) {
  margin-bottom: 8px;
}

:deep(.el-form-item__label) {
  color: var(--text-color);
  font-weight: 500;
  padding-right: 12px;
}

:deep(.el-input__wrapper),
:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  border-color: var(--border-color);
  border-radius: 8px;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__inner:hover),
::deep(.el-textarea__inner:hover) {
  border-color: var(--primary-color);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-input__inner:focus),
:deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(200, 70, 48, 0.1);
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  padding-right: 40px;
}

/* 收支类型单选按钮 */
.type-radio-group {
  display: flex;
  width: 100%;
  gap: 0;
}

.type-radio {
  flex: 1;
}

:deep(.el-radio-button) {
  height: 32px;
}

:deep(.el-radio-button__inner) {
  width: 100%;
  height: 32px;
  line-height: 28px;
  border-radius: 8px;
  background: white;
  border: 2px solid var(--border-color);
  color: var(--text-secondary);
  font-weight: 500;
  padding: 0 12px;
}

.type-radio.expense :deep(.el-radio-button__original:checked + .el-radio-button__inner) {
  background: var(--expense-color);
  border-color: var(--expense-color);
  color: white;
  box-shadow: 0 2px 8px rgba(200, 70, 48, 0.3);
}

.type-radio.income :deep(.el-radio-button__original:checked + .el-radio-button__inner) {
  background: var(--income-color);
  border-color: var(--income-color);
  color: white;
  box-shadow: 0 2px 8px rgba(45, 106, 79, 0.3);
}

/* 下拉选项样式 */
:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

.option-icon {
  font-size: 18px;
  margin-right: 6px;
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-code {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #999;
  background: #F5F1E8;
  padding: 2px 6px;
  border-radius: 4px;
}

.category-name {
  font-weight: 500;
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

:deep(.el-button--large) {
  height: 40px;
  padding: 0 25px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
}

:deep(.el-button--default) {
  background: white;
  border: 2px solid var(--border-color);
  color: var(--text-color);
}

:deep(.el-button--default:hover) {
  background: #F5F1E8;
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.btn-submit {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  letter-spacing: 0.1em;
}

.btn-submit:hover {
  background: #D85640;
  border-color: #D85640;
}

.btn-icon {
  margin-right: 6px;
  font-size: 18px;
}

/* 表单验证错误样式 */
:deep(.el-form-item.is-error .el-input__wrapper),
:deep(.el-form-item.is-error .el-textarea__inner) {
  border-color: #F56C6C;
}

:deep(.el-form-item.is-error .el-form-item__error) {
  color: #F56C6C;
}
</style>
