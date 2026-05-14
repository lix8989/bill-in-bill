# 微信账单分析系统 - UI设计方案文档

## 一、项目概述

### 1.1 项目信息
- **项目名称**: 微信账单分析系统
- **设计风格**: 明亮科技感
- **设计日期**: 2026-05-12
- **版本**: v1.0 Final

### 1.2 设计目标
- 打造现代化、专业化的账单分析界面
- 保持数据清晰可读性，科技感作为点缀
- 提升用户体验，降低操作复杂度

---

## 二、设计系统

### 2.1 色彩系统

#### 主色调
```css
/* 品牌色 - 科技蓝 */
--brand-500: #3b82f6;    /* 主品牌色 */
--brand-600: #2563eb;    /* 深品牌色 */
--gradient-brand: linear-gradient(135deg, #3b82f6 0%, #2563eb 50%, #1d4ed8 100%);
```

#### 背景色系
```css
--bg-primary: #f8fafc;      /* 主背景 - 浅灰蓝 */
--bg-secondary: #ffffff;     /* 次级背景 - 纯白 */
--bg-elevated: #ffffff;      /* 卡片背景 - 纯白 */
--bg-subtle: #f1f5f9;        /* 微妙背景 */
```

#### 语义色
```css
/* 成功 - 收入 */
--success-500: #22c55e;
--success-600: #16a34a;
--gradient-success: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);

/* 危险 - 支出 */
--danger-500: #ef4444;
--danger-600: #dc2626;
--gradient-danger: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);

/* 警告 */
--warning-500: #f59e0b;
--warning-600: #d97706;

/* 信息 */
--info-500: #0ea5e9;
--info-600: #0284c7;
```

#### 文字色系（9级精确灰阶）
```css
--text-50: #f8fafc;   /* 极浅 */
--text-100: #f1f5f9;
--text-200: #e2e8f0;
--text-300: #cbd5e1;
--text-400: #94a3b8;  /* 弱化文字 */
--text-500: #64748b;  /* 次要文字 */
--text-600: #475569;  /* 常规文字 */
--text-700: #334155;  /* 主要文字 */
--text-800: #1e293b;
--text-900: #0f172a;  /* 标题文字 */
```

#### 边框色系
```css
--border-50: #f1f5f9;
--border-100: #e2e8f0;  /* 默认边框 */
--border-200: #cbd5e1;  /* 深色边框 */
--border-300: #94a3b8;
```

### 2.2 字体系统

#### 字体家族
```css
/* 主字体 */
font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 
             'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;

/* 等宽字体（数字、金额） */
font-family: 'JetBrains Mono', 'Courier New', monospace;
```

#### 字号层级
```css
--text-xs: 11px;     /* 标签、辅助文字 */
--text-sm: 12px;     /* 小字 */
--text-base: 13px;   /* 基础文字 */
--text-md: 14px;     /* 正文 */
--text-lg: 15px;     /* 卡片标题 */
--text-xl: 16px;     /* 小标题 */
--text-2xl: 18px;    /* 侧边栏标题 */
--text-3xl: 24px;    /* 统计数字 */
--text-4xl: 26px;    /* 页面标题 */
--text-5xl: 42px;    /* 引导页标题 */
```

#### 字重
```css
--font-light: 300;
--font-normal: 400;
--font-medium: 500;
--font-semibold: 600;
--font-bold: 700;
```

### 2.3 阴影系统
```css
--shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.04);
--shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
--shadow-md: 0 4px 8px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04);
--shadow-lg: 0 12px 24px rgba(0, 0, 0, 0.10), 0 6px 12px rgba(0, 0, 0, 0.06);
--shadow-xl: 0 20px 40px rgba(0, 0, 0, 0.12), 0 8px 16px rgba(0, 0, 0, 0.06);
```

### 2.4 圆角系统
```css
--radius-sm: 6px;    /* 小按钮、标签 */
--radius-md: 8px;    /* 按钮、输入框 */
--radius-lg: 12px;   /* 卡片 */
--radius-xl: 16px;   /* 大卡片 */
--radius-2xl: 20px;  /* Hero区域 */
--radius-full: 50%;  /* 圆形 */
```

### 2.5 间距系统
```css
--space-1: 4px;
--space-2: 8px;
--space-3: 12px;
--space-4: 16px;
--space-5: 20px;
--space-6: 24px;
--space-8: 32px;
--space-10: 40px;
--space-12: 48px;
```

---

## 三、组件设计规范

### 3.1 按钮（Button）

#### 主按钮
```css
.btn-primary {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 50%, #1d4ed8 100%);
    color: white;
    padding: 10px 18px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.20);
}

.btn-primary:hover {
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.30);
    transform: translateY(-1px);
}
```

#### 次按钮
```css
.btn-secondary {
    background: #ffffff;
    color: #334155;
    border: 1px solid #cbd5e1;
    padding: 10px 18px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
}

.btn-secondary:hover {
    background: #f1f5f9;
    border-color: #94a3b8;
}
```

#### 幽灵按钮
```css
.btn-ghost {
    background: transparent;
    color: #2563eb;
    padding: 10px 18px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
}

.btn-ghost:hover {
    background: #eff6ff;
}
```

#### 小按钮
```css
.btn-sm {
    padding: 6px 12px;
    font-size: 12px;
}
```

### 3.2 输入框（Input）

```css
.form-input, .form-select {
    padding: 10px 14px;
    font-size: 13px;
    color: #334155;
    background: #ffffff;
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    transition: all 0.15s ease;
}

.form-input:focus, .form-select:focus {
    outline: none;
    border-color: #60a5fa;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 下拉选择箭头 */
.form-select {
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2364748b' d='M6 8L2 4h8z'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 14px center;
    padding-right: 40px;
}
```

### 3.3 卡片（Card）

```css
.card {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 16px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
    overflow: hidden;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:hover {
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.card-header {
    padding: 20px 24px;
    border-bottom: 1px solid #f1f5f9;
}

.card-body {
    padding: 24px;
}

.card-footer {
    padding: 16px 24px;
    background: #f1f5f9;
    border-top: 1px solid #f1f5f9;
}
```

### 3.4 统计卡片（Metric Card）

```css
.metric-card {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 16px;
    padding: 22px;
    position: relative;
    overflow: hidden;
    transition: all 0.2s ease;
}

/* 顶部彩色条 */
.metric-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: #e2e8f0;
}

.metric-card.income::before {
    background: linear-gradient(135deg, #22c55e, #16a34a);
}

.metric-card.expense::before {
    background: linear-gradient(135deg, #ef4444, #dc2626);
}

.metric-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.10);
}

.metric-label {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: #94a3b8;
    margin-bottom: 12px;
}

.metric-value {
    font-size: 32px;
    font-weight: 700;
    color: #0f172a;
    margin-bottom: 8px;
    letter-spacing: -0.02em;
}

.metric-value.income {
    color: #16a34a;
}

.metric-value.expense {
    color: #dc2626;
}

.metric-source {
    display: flex;
    gap: 8px;
    align-items: center;
    font-size: 12px;
    color: #64748b;
    margin-top: 12px;
}

.source-wechat {
    color: #3b82f6;
}

.source-alipay {
    color: #22c55e;
}
```

### 3.5 标签（Tag）

```css
.tag {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 11px;
    font-weight: 500;
    letter-spacing: 0.02em;
    white-space: nowrap;
}

/* 左侧圆点装饰 */
.tag::before {
    content: '';
    width: 4px;
    height: 4px;
    border-radius: 50%;
}

.tag-success {
    background: #f0fdf4;
    color: #16a34a;
}
.tag-success::before { background: #22c55e; }

.tag-danger {
    background: #fef2f2;
    color: #dc2626;
}
.tag-danger::before { background: #ef4444; }

.tag-warning {
    background: #fffbeb;
    color: #d97706;
}
.tag-warning::before { background: #f59e0b; }

.tag-info {
    background: #f0f9ff;
    color: #0284c7;
}
.tag-info::before { background: #0ea5e9; }

.tag-brand {
    background: #eff6ff;
    color: #2563eb;
}
.tag-brand::before { background: #3b82f6; }
```

### 3.6 表格（Table）

```css
.table-container {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    overflow: hidden;
}

.tech-table {
    width: 100%;
    border-collapse: collapse;
}

/* 表头 */
.tech-table thead {
    background: #f1f5f9;
    border-bottom: 1px solid #e2e8f0;
}

.tech-table th {
    padding: 14px 16px;
    text-align: left;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: #94a3b8;
    white-space: nowrap;
}

.tech-table th:not(:last-child) {
    border-right: 1px solid #f1f5f9;
}

/* 表格行 */
.tech-table tbody tr {
    border-bottom: 1px solid #f1f5f9;
    transition: all 0.1s ease;
}

.tech-table tbody tr:hover {
    background: #f1f5f9;
}

.tech-table tbody tr:last-child {
    border-bottom: none;
}

.tech-table td {
    padding: 14px 16px;
    font-size: 13px;
    color: #334155;
}

.tech-table td:not(:last-child) {
    border-right: 1px solid #f1f5f9;
}

/* 数字字体 */
.font-mono {
    font-family: 'JetBrains Mono', monospace;
}
```

### 3.7 开关（Switch）

```css
.switch {
    position: relative;
    width: 44px;
    height: 24px;
    background: #cbd5e1;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.switch.active {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.switch::after {
    content: '';
    position: absolute;
    top: 2px;
    left: 2px;
    width: 20px;
    height: 20px;
    background: white;
    border-radius: 50%;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: all 0.2s ease;
}

.switch.active::after {
    left: 22px;
}
```

### 3.8 图表组件

#### 纵向柱形图
```css
.chart-container {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 4px;
    height: 100%;
}

.chart-bar {
    flex: 0 0 20px;  /* 固定宽度，不过粗 */
    border-radius: 3px 3px 0 0;
    cursor: pointer;
    transition: all 0.2s ease;
}

.chart-bar:hover {
    filter: brightness(1.15);
    transform: scaleY(1.05);
}

.chart-bar.income {
    background: linear-gradient(135deg, #22c55e, #16a34a);
}

.chart-bar.expense {
    background: linear-gradient(135deg, #ef4444, #dc2626);
}

.chart-bar.accent {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
}
```

#### 横向柱形图
```css
.chart-container.horizontal {
    flex-direction: column;
    justify-content: space-around;
    gap: 16px;
}

.chart-row {
    display: flex;
    align-items: center;
    gap: 12px;
}

.chart-row-label {
    width: 60px;
    font-size: 13px;
    color: #475569;
    font-weight: 500;
    text-align: right;
    flex-shrink: 0;
}

.chart-bar.horizontal {
    height: 24px;
    border-radius: 4px;
    flex: 1;
    min-width: 0;
}

.chart-row-value {
    width: 80px;
    font-size: 13px;
    font-family: 'JetBrains Mono', monospace;
    color: #334155;
    font-weight: 600;
    text-align: right;
    flex-shrink: 0;
}

/* 横坐标轴 */
.chart-axis {
    display: flex;
    justify-content: space-between;
    padding-top: 12px;
    margin-top: 8px;
    border-top: 1px solid #e2e8f0;
}

.chart-axis-label {
    font-size: 11px;
    color: #94a3b8;
    text-align: center;
}
```

---

## 四、页面布局设计

### 4.1 整体布局

```
┌─────────────────────────────────────────────────────┐
│  ┌─────────┬─────────────────────────────────────┐  │
│  │         │  [主内容区]                        │  │
│  │  侧边栏  │                                     │  │
│  │         │  - 各页面功能                      │  │
│  │ 280px   │  - 响应式布局                      │  │
│  │         │                                     │  │
│  └─────────┴─────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

#### 侧边栏设计
```css
.sidebar {
    width: 280px;
    background: #ffffff;
    border-right: 1px solid #e2e8f0;
    padding: 32px 20px 20px;
}

/* 品牌区 */
.brand {
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px solid #f1f5f9;
}

.brand-eyebrow {
    font-size: 10px;
    font-weight: 600;
    letter-spacing: 0.15em;
    text-transform: uppercase;
    color: #3b82f6;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.brand-eyebrow::before {
    content: '';
    width: 8px;
    height: 8px;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    border-radius: 50%;
}

.brand-title {
    font-size: 18px;
    font-weight: 700;
    color: #0f172a;
    margin-bottom: 8px;
}

.brand-tagline {
    font-size: 12px;
    color: #94a3b8;
    line-height: 1.5;
}

/* 导航分组 */
.nav-section {
    margin-bottom: 24px;
}

.nav-section-title {
    font-size: 10px;
    font-weight: 600;
    letter-spacing: 0.1em;
    text-transform: uppercase;
    color: #cbd5e1;
    padding: 0 12px;
    margin-bottom: 8px;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 8px;
    color: #475569;
    text-decoration: none;
    transition: all 0.15s ease;
    font-size: 13px;
    font-weight: 500;
    position: relative;
    margin-bottom: 2px;
}

/* 左侧激活指示条 */
.nav-item::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 2px;
    height: 0;
    background: linear-gradient(180deg, #3b82f6, #2563eb);
    border-radius: 0 1px 1px 0;
    transition: height 0.15s ease;
}

.nav-item:hover {
    background: #f1f5f9;
    color: #1e293b;
}

.nav-item:hover::before {
    height: 16px;
}

.nav-item.active {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(37, 99, 235, 0.02));
    color: #2563eb;
}

.nav-item.active::before {
    height: 20px;
}
```

### 4.2 主内容区

```css
.content {
    padding: 32px;
    overflow-y: auto;
}

/* 页面头部 */
.page-header {
    margin-bottom: 32px;
}

.page-title {
    font-size: 26px;
    font-weight: 700;
    color: #0f172a;
    margin-bottom: 8px;
}

.page-description {
    font-size: 14px;
    color: #64748b;
}
```

---

## 五、核心页面设计

### 5.1 首页分析（Dashboard）

#### 布局结构
```
┌─────────────────────────────────────────────────────┐
│  [Hero区] 标题 + 年月筛选器 + 刷新按钮              │
├─────────────────────────────────────────────────────┤
│  [统计卡片网格 - 4个]                               │
│  年度收入 | 年度支出 | 月度收入 | 月度支出           │
├─────────────────────────────────────────────────────┤
│  [图表网格 - 2x2]                                  │
│  年度收支趋势图    | 年度支出分类图                  │
│  月度支出分类图    | 月度每日趋势图                  │
├─────────────────────────────────────────────────────┤
│  [表格网格 - 1x2]                                  │
│  年度Top10        | 月度Top10                       │
└─────────────────────────────────────────────────────┘
```

#### Hero区域
```css
.dashboard-hero {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 32px;
    margin-bottom: 32px;
    padding: 28px 32px;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(37, 99, 235, 0.03) 100%);
    border: 1px solid rgba(59, 130, 246, 0.1);
    border-radius: 20px;
}

.hero-kicker {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.15em;
    text-transform: uppercase;
    color: #3b82f6;
    margin-bottom: 12px;
}

.hero-title {
    font-size: 26px;
    font-weight: 700;
    color: #0f172a;
    margin-bottom: 8px;
}

.hero-copy {
    font-size: 14px;
    color: #64748b;
    max-width: 600px;
}
```

#### 统计卡片网格
```css
.summary-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 28px;
}

@media (max-width: 1200px) {
    .summary-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 768px) {
    .summary-grid {
        grid-template-columns: 1fr;
    }
}
```

#### 图表网格
```css
.chart-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 28px;
}

@media (max-width: 768px) {
    .chart-grid {
        grid-template-columns: 1fr;
    }
}
```

### 5.2 账单明细（Bills）

```
┌─────────────────────────────────────────────────────┐
│  标题 + [新增账单] 按钮                              │
├─────────────────────────────────────────────────────┤
│  [筛选器栏]                                         │
│  年份 | 月份 | 分类 | 收支类型 | 来源 | 计入结算      │
├─────────────────────────────────────────────────────┤
│  [统计栏] 总金额 | 支出 | 收入 | 记录数             │
├─────────────────────────────────────────────────────┤
│  [数据表格]                                         │
│  + 分页器                                           │
└─────────────────────────────────────────────────────┘
```

### 5.3 分类设置（Categories）

```
┌─────────────────────────────────────────────────────┐
│  标题                                               │
├─────────────────────────────────────────────────────┤
│  [新增分类表单]                                     │
│  输入框 + [新增分类] 按钮                           │
├─────────────────────────────────────────────────────┤
│  [分类列表表格]                                     │
│  ID | 分类名称 | 操作（编辑/删除）                   │
└─────────────────────────────────────────────────────┘
```

### 5.4 账单导入（Imports）

```
┌─────────────────────────────────────────────────────┐
│  标题                                               │
├─────────────────────────────────────────────────────┤
│  [微信导入] [支付宝导入]                            │
│  上传区 + 导入按钮                                   │
├─────────────────────────────────────────────────────┤
│  [导入历史表格]                                     │
└─────────────────────────────────────────────────────┘
```

---

## 六、用户引导页面

### 6.1 页面结构

```
┌─────────────────────────────────────────────────────┐
│  🚀 新手引导                                        │
│  欢迎使用账单分析系统                               │
├─────────────────────────────────────────────────────┤
│  [步骤时间线]                                       │
│  ┌────┐                                            │
│  │ 1  │ 📥 导入账单数据                            │
│  └────┘                                            │
│       ┌────┐                                       │
│       │ 2  │ ⚙️ 配置分类规则                        │
│       └────┘                                       │
│  ┌────┐                                            │
│  │ 3  │ 🤖 执行自动分类                            │
│  └────┘                                            │
│       ┌────┐                                       │
│       │ 4  │ ✅ 复核分类结果                        │
│       └────┘                                       │
│  ┌────┐                                            │
│  │ 5  │ 📊 查看统计分析                            │
│  └────┘                                            │
│       ┌────┐                                       │
│       │ 6  │ 💳 查询账单明细                        │
│       └────┘                                       │
├─────────────────────────────────────────────────────┤
│  💡 使用技巧                                         │
│  [4个技巧卡片]                                      │
├─────────────────────────────────────────────────────┤
│  [行动号召]                                         │
│  开始导入账单 | 查看使用教程                         │
└─────────────────────────────────────────────────────┘
```

### 6.2 引导流程

1. **导入账单数据** - 支持微信Excel、支付宝CSV文件
2. **配置分类规则** - 分类管理、关键字规则、AI配置
3. **执行自动分类** - 智能批量分类
4. **复核分类结果** - 待复核列表、异常处理
5. **查看统计分析** - 收支趋势、分类占比、Top10排行
6. **查询账单明细** - 多维度筛选、数据导出

---

## 七、响应式设计

### 7.1 断点系统

```css
/* 小屏幕 */
@media (max-width: 768px) {
    .app-shell {
        grid-template-columns: 1fr;
    }
    
    .sidebar {
        display: none;
    }
    
    .summary-grid {
        grid-template-columns: 1fr;
    }
    
    .chart-grid {
        grid-template-columns: 1fr;
    }
    
    .table-grid {
        grid-template-columns: 1fr;
    }
}

/* 中等屏幕 */
@media (max-width: 1200px) {
    .summary-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

/* Hero区域响应式 */
@media (max-width: 960px) {
    .dashboard-hero {
        flex-direction: column;
        align-items: stretch;
    }
    
    .hero-form {
        justify-content: flex-start;
    }
}
```

---

## 八、动画效果

### 8.1 页面过渡

```css
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.fade-in {
    animation: fadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
```

### 8.2 图表动画

```css
.chart-bar {
    transition: all 0.2s ease;
}

/* 加载时从0增长到目标高度 */
.chart-bar.animate {
    animation: growUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes growUp {
    from {
        height: 0;
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}
```

### 8.3 悬停效果

| 元素 | 悬停效果 |
|------|----------|
| 按钮 | 上浮1px + 阴影加深 |
| 卡片 | 阴影加深 |
| 统计卡片 | 上浮2px + 阴影加深 |
| 柱形图 | 亮度提升 + 轻微缩放 |
| 表格行 | 背景变色 |
| 导航项 | 背景变色 + 左侧光条滑入 |

---

## 九、可访问性

### 9.1 颜色对比度

所有文字与背景的对比度符合 WCAG AA 标准（4.5:1）

### 9.2 键盘导航

- 所有交互元素支持 Tab 键导航
- 聚焦状态清晰可见（蓝色光圈）

### 9.3 字体大小

最小字号不小于 11px，确保可读性

---

## 十、文件结构

### 10.1 原型文件

```
frontend/
├── prototype-exact-features.html    # 完整功能原型
├── guide.html                        # 用户引导页面
└── prototype-premium-tech.html      # 早期原型（已废弃）
```

### 10.2 设计文档

```
docs/
├── ui-redesign-tech-future.md        # 深色科技方案（已废弃）
└── ui-redesign-bright-tech.md        # 明亮科技方案（参考）
```

---

## 十一、实施建议

### 11.1 技术栈

- **框架**: Vue 3 + Element Plus
- **图表**: ECharts（深色主题配置）
- **构建**: Vite

### 11.2 CSS变量方案

使用CSS变量定义设计系统，便于主题切换：

```css
:root {
    /* 在全局样式文件中定义所有设计token */
    --brand-500: #3b82f6;
    --text-primary: #0f172a;
    /* ... */
}

/* 使用 */
.button {
    background: var(--brand-500);
    color: var(--text-primary);
}
```

### 11.3 实施优先级

**阶段一：基础样式（1天）**
1. 创建CSS变量文件
2. 更新全局样式
3. 升级侧边栏样式

**阶段二：核心页面（2-3天）**
1. DashboardView 完整实现
2. BillsView 升级
3. CategoriesView 升级

**阶段三：辅助页面（2天）**
1. 其他6个页面样式升级
2. 引导页面实现

**阶段四：优化打磨（1天）**
1. 响应式适配
2. 动画优化
3. 测试修复

**总计：5-7个工作日**

---

## 十一、核心交互流程

### 11.1 用户首次进入流程

#### 默认进入页面
用户首次访问系统时，默认进入**用户引导页面**（`/guide`）

```
访问根路径 / 或首次打开系统
        ↓
检查 localStorage 中是否存在 system_visited 标记
        ↓
    不存在 → 显示引导页面 (/guide)
    存在   → 直接进入首页 (/dashboard)
```

#### 实现方案

**方案一：路由守卫（推荐）**
```javascript
// router/index.js
{
  path: '/',
  beforeEnter: (to, from, next) => {
    const hasVisited = localStorage.getItem('system_visited')
    if (hasVisited) {
      next('/dashboard')
    } else {
      next('/guide')
    }
  }
}
```

**方案二：首页判断**
```javascript
// App.vue
const showGuide = ref(!localStorage.getItem('system_visited'))

function handleGuideComplete() {
  localStorage.setItem('system_visited', 'true')
  showGuide.value = false
}
```

### 11.2 用户引导页面交互

#### 页面功能
- 展示6步操作流程
- 提供使用技巧
- 多个入口按钮进入系统

#### 交互动作表

| 按钮/操作 | 动作描述 | 跳转目标 |
|-----------|----------|----------|
| "去导入" | 跳转到账单导入页面 | `/imports` |
| "去配置" | 跳转到分类设置页面 | `/categories` |
| "去分类" | 跳转到自动分类页面 | `/classifier-tasks` |
| "去复核" | 跳转到分类复核页面 | `/review-bills` |
| "去分析" | 跳转到首页分析 | `/dashboard` |
| "去查询" | 跳转到账单明细 | `/bills` |
| "开始导入账单" | 跳转到账单导入页面 | `/imports` |
| "我知道了，进入系统" | 跳转到首页分析 | `/dashboard` |

所有操作完成后都会记录 `system_visited = true`

### 11.3 主系统操作流程

#### 完整用户流程图

```
┌─────────────┐
│  首次访问   │
└──────┬──────┘
       ↓
┌─────────────────────────────────────────┐
│         引导页面 (/guide)               │
│  ┌─────────────────────────────────┐    │
│  │ 步骤1: 📥 导入账单              │    │
│  │   支持微信Excel / 支付宝CSV    │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ 步骤2: ⚙️ 配置分类             │    │
│  │   分类管理 / 关键字 / AI配置    │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ 步骤3: 🤖 执行自动分类         │    │
│  │   智能批量分类                 │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ 步骤4: ✅ 复核分类结果         │    │
│  │   待复核 / 异常处理            │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ 步骤5: 📊 查看统计分析         │    │
│  │   趋势 / 占比 / 排行           │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ 步骤6: 💳 查询账单明细         │    │
│  │   筛选 / 导出                  │    │
│  └─────────────────────────────────┘    │
│                                         │
│  [点击任意"去xxx"或"进入系统"按钮]      │
└────────────────┬────────────────────────┘
                 ↓
        记录 system_visited = true
                 ↓
┌─────────────────────────────────────────┐
│         主系统                         │
│                                         │
│  ┌──────────────┐  ┌──────────────┐    │
│  │ 账单导入     │  │ 分类设置      │    │
│  │ /imports     │  │ /categories  │    │
│  └──────┬───────┘  └───────┬──────┘    │
│         ↓                  ↓           │
│  ┌──────────────────────────────┐    │
│  │     自动分类                  │    │
│  │  /classifier-tasks          │    │
│  └──────┬───────────────────────┘    │
│         ↓                             │
│  ┌──────────────┐  ┌──────────────┐    │
│  │ 分类复核     │  │ 分类异常      │    │
│  │ /review      │  │ /failed      │    │
│  └──────┬───────┘  └───────┬──────┘    │
│         └──────────┬─────────┘           │
│                    ↓                     │
│         ┌───────────────────┐           │
│         │   首页分析         │           │
│         │   /dashboard       │           │
│         └───────┬───────────┘           │
│                 ↓                       │
│         ┌───────────────────┐           │
│         │   账单明细         │           │
│         │   /bills           │           │
│         └───────────────────┘           │
└─────────────────────────────────────────┘
```

### 11.4 关键页面交互细节

#### 首页分析（Dashboard）

**筛选器交互**
```javascript
// 年份选择
onYearChange(year) {
  selectedYear.value = year
  loadDashboard()  // 重新加载数据
}

// 月份选择  
onMonthChange(month) {
  selectedMonth.value = month
  loadDashboard()  // 重新加载数据
}

// 刷新按钮
onRefresh() {
  loadDashboard()  // 重新加载所有数据
}
```

**图表点击交互**
```javascript
// 年度收支趋势图 - 点击柱子
onTrendBarClick(params) {
  const month = params.name
  router.push({
    path: '/bills',
    query: { 
      year: selectedYear.value,
      month: `${selectedYear.value}-${month}`
    }
  })
}

// 年度支出分类图 - 点击柱子
onCategoryBarClick(params) {
  const category = params.name
  router.push({
    path: '/bills',
    query: { 
      year: selectedYear.value,
      category: categoryId
    }
  })
}

// 来源切换（全部/微信/支付宝）
onSourceChange(source) {
  trendSource.value = source
  renderCharts()  // 重新渲染图表
}
```

**查看账单按钮**
```javascript
// 年度图表 → 年度账单
goToBillsByYear() {
  router.push({
    path: '/bills',
    query: { year: selectedYear.value }
  })
}

// 月度图表 → 月度账单
goToBillsByMonth(month) {
  router.push({
    path: '/bills',
    query: { month: selectedMonth.value }
  })
}
```

#### 账单明细（Bills）

**筛选器交互**
```javascript
// 筛选查询
onFilterChange() {
  page.value = 1  // 重置到第一页
  loadBills()     // 加载账单数据
}

// 从首页跳转时自动填充筛选条件
// URL: /bills?year=2024&month=2024-05
onRouteQueryChange(query) {
  selectedYear.value = query.year || ''
  selectedMonth.value = query.month || ''
  selectedCategory.value = query.category || ''
  loadBills()
}
```

**表格操作交互**
```javascript
// 切换分类
onChangeCategory(row, categoryId) {
  updateBillCategory(row.id, categoryId)
    .then(() => {
      row.categoryId = categoryId
      ElMessage.success('分类已更新')
    })
}

// 切换计入结算开关
onChangeSettlementIncluded(row, value) {
  const previousValue = row.settlementIncluded
  row.settlementIncluded = value  // 乐观更新
  
  updateBillSettlement(row.id, value)
    .then(() => {
      ElMessage.success('已更新')
    })
    .catch(() => {
      row.settlementIncluded = previousValue  // 回滚
      ElMessage.error('更新失败')
    })
}

// 新增账单
onCreateBill() {
  createDialogVisible.value = true
}

// 分页导航
onPrevPage() {
  if (page.value > 1) {
    page.value--
    loadBills()
  }
}

onNextPage() {
  page.value++
  loadBills()
}
```

#### 账单导入（Imports）

**文件上传交互**
```javascript
// 文件选择
onWechatFileChange(event) {
  const file = event.target.files[0]
  if (validateFile(file, 'wechat')) {
    wechatFile.value = file
  }
}

onAlipayFileChange(event) {
  const file = event.target.files[0]
  if (validateFile(file, 'alipay')) {
    alipayFile.value = file
  }
}

// 文件验证
function validateFile(file, type) {
  // 类型检查
  if (type === 'wechat' && !file.name.endsWith('.xlsx')) {
    ElMessage.error('请上传 Excel 文件（.xlsx）')
    return false
  }
  
  if (type === 'alipay' && 
      !file.name.endsWith('.csv') && 
      !file.name.endsWith('.txt')) {
    ElMessage.error('请上传 CSV 或 TXT 文件')
    return false
  }
  
  // 大小检查（最大10MB）
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  
  return true
}

// 执行导入
onSubmitImport(type) {
  const file = type === 'wechat' ? wechatFile.value : alipayFile.value
  
  if (!file) {
    ElMessage.warning('请先选择文件')
    return
  }
  
  submitting.value = true
  
  const importFunc = type === 'wechat' ? importWechatBill : importAlipayBill
  
  importFunc(file)
    .then(response => {
      result.value = response.data.data
      
      if (response.data.data.failCount > 0) {
        ElMessage.warning('导入完成，部分记录未导入')
      } else {
        ElMessage.success('导入完成')
      }
      
      loadImportHistory()  // 刷新导入历史
    })
    .finally(() => {
      submitting.value = false
    })
}

// 拖拽上传
onDropFile(file, type) {
  if (validateFile(file, type)) {
    if (type === 'wechat') {
      wechatFile.value = file
      onSubmitImport('wechat')
    } else {
      alipayFile.value = file
      onSubmitImport('alipay')
    }
  }
}
```

### 11.5 页面间跳转关系

```
                    ┌─────────────┐
                    │  引导页面   │
                    │   /guide    │
                    └──────┬──────┘
                           │ 任意操作
                           ↓
        ┌──────────────────────────────────┐
        │         首页分析 /dashboard        │
        │  - 查看4个统计卡片               │
        │  - 查看4个图表                    │
        │  - 查看2个Top10表格               │
        └──────────────────────────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         ↓                 ↓                 ↓
    ┌─────────┐      ┌─────────┐      ┌─────────┐
    │账单导入  │      │分类设置  │      │自动分类  │
    │/imports  │      │/categories│     │/tasks   │
    └─────────┘      └─────────┘      └─────────┘
         │                                    │
         ↓                                    ↓
    ┌─────────┐      ┌─────────┐      ┌─────────┐
    │分类复核  │      │分类异常  │      │关键字规则│
    │/review  │      │/failed  │      │/keywords│
    └────┬────┘      └─────────┘      └─────────┘
         │
         ↓
    ┌─────────┐      ┌─────────┐
    │账单明细  │◄────►│同步日志  │
    │/bills   │      │/logs    │
    └─────────┘      └─────────┘
         ↑
         │ 从首页图表点击跳转
    ┌─────────┐
    │首页分析  │
    │/dashboard│
    └─────────┘
```

### 11.6 全局状态管理

#### 访问标记
```javascript
const STORAGE_KEY = 'system_visited'

// 检查是否首次访问
function isFirstVisit() {
  return !localStorage.getItem(STORAGE_KEY)
}

// 标记已访问
function markVisited() {
  localStorage.setItem(STORAGE_KEY, 'true')
}

// 重置访问标记（测试用）
function resetVisit() {
  localStorage.removeItem(STORAGE_KEY)
}
```

#### 用户偏好
```javascript
// 保存用户偏好
function savePreference(key, value) {
  const prefs = JSON.parse(localStorage.getItem('user_prefs') || '{}')
  prefs[key] = value
  localStorage.setItem('user_prefs', JSON.stringify(prefs))
}

// 获取用户偏好
function getPreference(key, defaultValue) {
  const prefs = JSON.parse(localStorage.getItem('user_prefs') || '{}')
  return prefs[key] ?? defaultValue
}

// 使用示例
const defaultYear = getPreference('defaultYear', new Date().getFullYear())
const pageSize = getPreference('pageSize', 20)
```

**文档版本**: v1.0 Final  
**创建日期**: 2026-05-12  
**最后更新**: 2026-05-12  
**状态**: 已完成

---

**更新日志**:
- 2026-05-12: 初始版本，完成设计系统和组件规范
- 2026-05-12: 新增核心交互流程章节（第十一章）

### 12.1 视觉验收

- [ ] 整体色调明亮舒适，无刺眼元素
- [ ] 字体层级清晰，可读性强
- [ ] 间距统一，视觉节奏协调
- [ ] 科技感通过细节体现（渐变、阴影、圆角）

### 12.2 功能验收

- [ ] 所有现有功能完整保留
- [ ] 图表横坐标正确显示
- [ ] 横向柱形图正确展示
- [ ] 响应式布局正常工作

### 12.3 性能验收

- [ ] 页面加载速度无明显下降
- [ ] 动画流畅无卡顿
- [ ] CSS变量正确应用

---

**文档版本**: v1.0 Final  
**创建日期**: 2026-05-12  
**设计师**: Claude  
**状态**: 已完成
