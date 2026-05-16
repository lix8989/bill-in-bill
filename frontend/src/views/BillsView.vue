<template>
  <section class='bills-page fade-in'>
    <header class='proto-hero'>
      <div class='proto-hero__main'>
        <p class='proto-kicker'>账单流水</p>
        <h1 class='proto-title'>逐笔说帐</h1>
        <p class='proto-lead'>按条件查看与筛选账单，行内可改分类与是否计入结算。</p>
      </div>
      <div class='proto-hero__cta'>
        <el-button type='primary' class='btn-seal-red' @click='openCreateDialog'>
          <span>✚</span>
          <span>补记一笔</span>
        </el-button>
      </div>
    </header>

    <el-card shadow='never' class='proto-ledger'>
      <div class='proto-ledger__grid'>
        <div class='proto-ledger__col proto-ledger__col--filters'>
          <div class='proto-rail'>
            <span class='proto-rail__mark'>查</span>
            <div class='proto-rail__text'>
              <h2 class='proto-rail__title'>查询条件</h2>
              <p class='proto-rail__sub'>先圈定时间范围，再收窄分类与渠道</p>
            </div>
          </div>
          <div class='proto-groups'>
            <div class='proto-chip-group'>
              <span class='proto-chip-group__label'>周期</span>
              <div class='proto-chip-group__controls'>
                <el-form-item label='年份'>
                  <el-date-picker v-model='selectedYear' type='year' value-format='YYYY' placeholder='选择年份' style='width: 130px' />
                </el-form-item>
                <el-form-item label='月份'>
                  <el-date-picker v-model='selectedMonth' type='month' value-format='YYYY-MM' placeholder='选择月份' style='width: 148px' />
                </el-form-item>
              </div>
            </div>
            <div class='proto-chip-group'>
              <span class='proto-chip-group__label'>账目</span>
              <div class='proto-chip-group__controls proto-chip-group__controls--wrap'>
                <el-form-item label='分类'>
                  <el-select v-model='selectedCategoryId' clearable placeholder='全部' style='width: 168px'>
                    <el-option v-for='item in categories' :key='item.id' :label='item.name' :value='item.id' />
                  </el-select>
                </el-form-item>
                <el-form-item label='收支'>
                  <el-select v-model='selectedIncomeExpenseType' clearable placeholder='全部' style='width: 120px'>
                    <el-option label='收入' value='收入' />
                    <el-option label='支出' value='支出' />
                  </el-select>
                </el-form-item>
                <el-form-item label='来源'>
                  <el-select v-model='selectedSource' clearable placeholder='全部' style='width: 120px'>
                    <el-option label='微信' value='微信' />
                    <el-option label='支付宝' value='支付宝' />
                  </el-select>
                </el-form-item>
                <el-form-item label='结算'>
                  <el-select v-model='selectedSettlementIncluded' clearable placeholder='全部' style='width: 120px'>
                    <el-option label='计入' :value='true' />
                    <el-option label='不计入' :value='false' />
                  </el-select>
                </el-form-item>
              </div>
            </div>
            <div class='proto-chip-group proto-chip-group--actions'>
              <el-button type='primary' class='proto-plain-refresh' @click='resetAndLoad'>刷新明细</el-button>
            </div>
          </div>
        </div>

        <div class='proto-ledger__divider' aria-hidden='true'></div>

        <div class='proto-ledger__col proto-ledger__col--summary'>
          <div class='proto-rail'>
            <span class='proto-rail__mark proto-rail__mark--muted'>汇总</span>
            <div class='proto-rail__text'>
              <h2 class='proto-rail__title'>当页汇总</h2>
              <p class='proto-rail__sub'>与上方筛选口径一致的总金额与小计</p>
            </div>
          </div>
          <div class='proto-tiles'>
            <article class='proto-tile'>
              <p class='proto-tile__label'>总金额</p>
              <p class='proto-tile__value total'>{{ stats.totalAmount.toFixed(2) }}<span class='proto-tile__unit'>元</span></p>
            </article>
            <article class='proto-tile'>
              <p class='proto-tile__label'>支出</p>
              <p class='proto-tile__value expense'>{{ stats.expenseAmount.toFixed(2) }}<span class='proto-tile__unit'>元</span></p>
            </article>
            <article class='proto-tile'>
              <p class='proto-tile__label'>收入</p>
              <p class='proto-tile__value income'>{{ stats.incomeAmount.toFixed(2) }}<span class='proto-tile__unit'>元</span></p>
            </article>
            <article class='proto-tile'>
              <p class='proto-tile__label'>记录数</p>
              <p class='proto-tile__value plain'>{{ stats.totalCount }}<span class='proto-tile__unit'>条</span></p>
            </article>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow='never' class='table-card proto-table-card'>
      <template #header>
        <div class='proto-table-meta'>
          <div>
            <h2 class='proto-table-meta__title'>流水明细</h2>
            <p class='proto-table-meta__desc'>
              列较多时可在表格底部横向滚动查看全部列；可在行内更改分类或「计入结算」。
            </p>
          </div>
        </div>
      </template>
      <div class='table-scroll-container bills-table-scroll'>
        <el-table
          :data='bills'
          stripe
          empty-text='暂无数据'
          :fit='false'
          scrollbar-always-on
          style='width: 100%'
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
          <el-table-column label='结算' width='110' fixed='right'>
            <template #default='{ row }'>
              <el-switch
                :model-value='row.settlementIncluded'
                @change='value => changeSettlementIncluded(row, value)'
              />
            </template>
          </el-table-column>
          <el-table-column label='分类' width='210' class-name='col-category' fixed='right'>
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
          <el-table-column label='同步' width='120' fixed='right'>
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
        <select class='page-size-select' :value='pageSize' @change='e => setPageSize(Number(e.target.value))'>
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
import { useBillsPage } from './bills/useBillsPage'

const {
  bills,
  categories,
  selectedYear,
  selectedMonth,
  selectedCategoryId,
  selectedIncomeExpenseType,
  selectedSource,
  selectedSettlementIncluded,
  stats,
  page,
  pageSize,
  createDialogVisible,
  createForm,
  loadBills,
  openCreateDialog,
  submitCreate,
  changeCategory,
  changeSettlementIncluded,
  resetAndLoad,
  prevPage,
  nextPage,
  setPageSize,
} = useBillsPage()

defineExpose({
  refreshBills: () => loadBills(),
})
</script>

<style scoped>
.bills-page {
  display: grid;
  /* 单列也要 minmax(0,1fr)，否则宽表格会把 grid 列撑出视口 */
  grid-template-columns: minmax(0, 1fr);
  gap: var(--space-5);
  min-width: 0;
  max-width: 100%;
  overflow-x: hidden;
}

.proto-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
  padding: var(--space-5) var(--space-6);
  background:
    linear-gradient(135deg, rgba(45, 106, 79, 0.07), transparent 45%),
    linear-gradient(180deg, rgba(237, 232, 221, 0.9), rgba(245, 241, 232, 0.5));
  border: 1px solid rgba(45, 106, 79, 0.14);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  position: relative;
}

.proto-hero::after {
  content: '';
  position: absolute;
  left: var(--space-6);
  bottom: 12px;
  width: min(280px, 40%);
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(90deg, var(--color-cyan), transparent);
  pointer-events: none;
}

.proto-kicker {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.35em;
  text-transform: uppercase;
  color: var(--color-cyan);
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
}

.proto-title {
  margin: 0 0 10px;
  font-size: 30px;
  font-weight: 700;
  color: var(--color-black);
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  letter-spacing: 0.12em;
}

.proto-lead {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--ink-light);
}

.proto-hero__cta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

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

.proto-plain-refresh {
  font-family: 'KaiTi', 'STKaiti', '楷体', serif !important;
  letter-spacing: 0.08em !important;
  border-color: var(--color-cyan) !important;
  background: rgba(45, 106, 79, 0.08) !important;
  color: var(--color-cyan) !important;
  box-shadow: inset 0 0 0 1px rgba(45, 106, 79, 0.12) !important;
}

.proto-plain-refresh:hover {
  background: rgba(45, 106, 79, 0.14) !important;
  border-color: var(--brand-600) !important;
  color: var(--brand-600) !important;
}

.proto-ledger {
  border: 1px solid rgba(42, 42, 42, 0.08) !important;
  border-radius: var(--radius-lg) !important;
  background: linear-gradient(180deg, #faf7ef 0%, var(--bg-paper) 100%) !important;
}

.proto-ledger :deep(.el-card__body) {
  padding: var(--space-6) !important;
}

.proto-ledger__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) auto minmax(0, 0.95fr);
  gap: 0;
  align-items: stretch;
}

.proto-ledger__divider {
  width: 1px;
  margin: 0 var(--space-5);
  background: linear-gradient(
    180deg,
    transparent,
    rgba(45, 106, 79, 0.2) 15%,
    rgba(45, 106, 79, 0.2) 85%,
    transparent
  );
}

.proto-rail {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: var(--space-5);
}

.proto-rail__mark {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  font-size: 18px;
  color: var(--color-white);
  background: linear-gradient(145deg, var(--color-cyan), var(--brand-600));
  border-radius: var(--radius-sm);
  box-shadow: 0 4px 10px rgba(45, 106, 79, 0.25);
}

.proto-rail__mark--muted {
  background: linear-gradient(145deg, #8f7a53, var(--color-brown));
}

.proto-rail__title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-dark);
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  letter-spacing: 0.06em;
}

.proto-rail__sub {
  margin: 0;
  font-size: 12px;
  color: var(--ink-light);
  line-height: 1.5;
}

.proto-groups {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.proto-chip-group__label {
  display: block;
  font-size: 11px;
  letter-spacing: 0.2em;
  color: rgba(45, 106, 79, 0.85);
  margin-bottom: 8px;
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
}

.proto-chip-group__controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.proto-chip-group__controls--wrap {
  row-gap: 4px;
}

.proto-chip-group--actions {
  padding-top: var(--space-2);
}

.proto-chip-group :deep(.el-form-item) {
  margin-bottom: 4px;
}

.proto-tiles {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.proto-tile {
  padding: var(--space-4);
  border-radius: var(--radius-md);
  background: rgba(245, 241, 232, 0.85);
  border: 1px solid rgba(42, 42, 42, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.65);
}

.proto-tile__label {
  margin: 0 0 6px;
  font-size: 12px;
  color: var(--ink-light);
}

.proto-tile__value {
  margin: 0;
  font-size: 21px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.02em;
}

.proto-tile__value.total {
  color: var(--color-black);
}
.proto-tile__value.expense {
  color: var(--color-red);
}
.proto-tile__value.income {
  color: var(--color-green);
}
.proto-tile__value.plain {
  color: var(--color-cyan);
}

.proto-tile__unit {
  margin-left: 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--ink-light);
}

.proto-table-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
}

.proto-table-meta__title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-dark);
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  letter-spacing: 0.06em;
}

.proto-table-meta__desc {
  margin: 0;
  font-size: 12px;
  color: var(--ink-light);
  line-height: 1.5;
}

.proto-table-card {
  min-width: 0;
  max-width: 100%;
  width: 100%;
  box-sizing: border-box;
}

.proto-table-card :deep(.el-card__header) {
  padding: var(--space-5) var(--space-6) !important;
  border-bottom: 1px solid rgba(42, 42, 42, 0.08);
  min-width: 0;
}

.proto-table-card :deep(.el-card__body) {
  padding-top: var(--space-4) !important;
  min-width: 0;
  max-width: 100%;
}

@media (max-width: 1180px) {
  .proto-ledger__grid {
    grid-template-columns: minmax(0, 1fr);
  }
  .proto-ledger__divider {
    display: none;
  }
  .proto-hero {
    flex-direction: column;
  }
  .proto-hero__cta {
    align-items: flex-start;
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: flex-end;
  }
}

@media (max-width: 640px) {
  .proto-tiles {
    grid-template-columns: 1fr;
  }
}

/* 表体不内嵌纵向滚动：行数再高也由页面滚动；横向仍走内部 scrollbar 与表头同步 */
.bills-table-scroll {
  min-width: 0;
  max-width: 100%;
  overflow-x: hidden;
}

.bills-table-scroll :deep(.el-table__body-wrapper .el-scrollbar) {
  height: auto !important;
}

.bills-table-scroll :deep(.el-table__body-wrapper .el-scrollbar__wrap) {
  height: auto !important;
  max-height: none !important;
  overflow-x: auto !important;
  overflow-y: hidden !important;
}

.bills-table-scroll :deep(.el-table__body-wrapper .el-scrollbar__bar.is-vertical) {
  display: none !important;
}

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

:deep(td.col-category) {
  padding-left: 0 !important;
  padding-right: 0 !important;
}

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
