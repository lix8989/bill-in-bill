# 微信账单分析系统 - 水墨古风设计方案

## 一、设计概述

### 1.1 设计理念
**「水墨丹青 · 古韵新辉」**

将中国传统水墨画的意境与现代数据可视化相结合，打造既有东方美学底蕴，又不失实用性的账单分析系统。

**核心设计元素：**
- 🎨 水墨渲染效果
- 📜 古典色彩体系（朱砂、石青、藤黄、墨色）
- 🖌️ 毛笔书法字体
- 🏛️ 传统纹样装饰（祥云、回纹、如意纹）
- 📜 宣纸质感背景
- 🎭 印章元素点缀

### 1.2 设计原则
- **雅致为上**：不追求过度装饰，保持界面清爽
- **意境为先**：通过水墨渲染营造氛围，而非堆砌元素
- **实用为本**：确保数据清晰可读，古风只是外在表现
- **现代内核**：使用现代技术实现传统美学

---

## 二、色彩系统

### 2.1 主色调 - 五色体系

基于中国传统"五色观"（青、赤、黄、白、黑）

```css
/* 主色系 - 五色观 */
--color-cyan: #2D6A4F;        /* 石青 - 数据展示 */
--color-red: #C84630;         /* 朱砂 - 支出 */
--color-gold: #D4A853;         /* 藤黄 - 收入 */
--color-white: #F5F1E8;       /* 宣纸白 - 背景 */
--color-black: #1A1A1A;       /* 墨色 - 文字 */

/* 辅助色系 - 传统色彩 */
--color-indigo: #4A6FA5;      /* 靛蓝 - 辅助数据 */
--color-brown: #8B6914;       /* 赭石 - 强调 */
--color-green: #4A7C59;       /* 竹青 - 成功 */
--color-orange: #E07A5F;      /* 橘红 - 警告 */

/* 水墨色阶 */
--ink-light: #6B6B6B;         /* 淡墨 */
--ink-medium: #4A4A4A;        /* 中墨 */
--ink-dark: #2A2A2A;          /* 浓墨 */
--ink-black: #1A1A1A;         /* 焦墨 */
```

### 2.2 背景色系

```css
/* 宣纸质感背景 */
--bg-paper: #F5F1E8;                  /* 宣纸白 */
--bg-paper-texture: url('./paper-texture.png'); /* 宣纸纹理 */
--bg-ripple: radial-gradient(circle, rgba(45, 106, 79, 0.03) 0%, transparent 70%); /* 水墨晕染 */

/* 渐变背景 */
--bg-gradient-vertical: linear-gradient(180deg, #F5F1E8 0%, #EDE8DD 100%);
--bg-gradient-horizontal: linear-gradient(90deg, #F5F1E8 0%, #EDE8DD 100%);
```

### 2.3 数据色系

```css
/* 收入色系 */
--income-primary: #D4A853;           /* 藤黄 */
--income-gradient: linear-gradient(135deg, #D4A853, #C49A6A);
--income-shadow: 0 4px 12px rgba(212, 168, 83, 0.2);

/* 支出色系 */
--expense-primary: #C84630;          /* 朱砂 */
--expense-gradient: linear-gradient(135deg, #C84630, #A63820);
--expense-shadow: 0 4px 12px rgba(200, 70, 48, 0.2);

/* 中性色系 */
--neutral-primary: #2D6A4F;          /* 石青 */
--neutral-gradient: linear-gradient(135deg, #2D6A4F, #235A42);
```

---

## 三、字体系统

### 3.1 字体家族

```css
/* 标题字体 - 书法字体 */
font-family: 'Ma Shan Zheng', 'STKaiti', 'KaiTi', '楷体', serif;

/* 正文字体 - 宋体 */
font-family: 'Noto Serif SC', 'STSong', 'SimSun', '宋体', serif;

/* 数字字体 - 等宽字体 */
font-family: 'Noto Sans SC', 'STHeiti', 'SimHei', sans-serif;

/* 特殊字体 - 金额显示 */
font-family: 'JetBrains Mono', 'Courier New', monospace;
```

### 3.2 字号层级

```css
--font-xs: 11px;      /* 注释文字 */
--font-sm: 12px;      /* 小字 */
--font-base: 13px;    /* 正文 */
--font-md: 14px;      /* 强调正文 */
--font-lg: 15px;      /* 小标题 */
--font-xl: 16px;      /* 标题 */
--font-2xl: 18px;     /* 大标题 */
--font-3xl: 24px;     /* 统计数字 */
--font-4xl: 28px;     /* 页面标题 */
--font-5xl: 36px;     /* 特大标题 */
```

### 3.3 字重

```css
--font-light: 300;    /* 细 - 毛笔细 */
--font-regular: 400;  /* 常 - 毛笔中 */
--font-medium: 500;   /* 中 - 楷书 */
--font-semibold: 600; /* 粗 - 楷书粗 */
--font-bold: 700;     /* 浓 - 颜体 */
```

---

## 四、组件设计规范

### 4.1 按钮（Button）

#### 主按钮 - 印章风格
```css
.btn-primary {
    /* 印章效果 */
    width: 80px;
    height: 80px;
    border: 3px solid #C84630;
    border-radius: 4px;
    background: transparent;
    color: #C84630;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    position: relative;
    
    /* 印章纹理 */
    box-shadow: 
        inset 0 0 0 2px rgba(200, 70, 48, 0.1),
        0 4px 12px rgba(200, 70, 48, 0.3);
    
    /* 水墨晕染 */
    transition: all 0.3s ease;
}

.btn-primary::before {
    content: '';
    position: absolute;
    inset: 4px;
    border: 1px solid rgba(200, 70, 48, 0.3);
    border-radius: 2px;
    opacity: 0.6;
}

.btn-primary:hover {
    transform: translateY(-2px) rotate(-1deg);
    box-shadow: 
        inset 0 0 0 2px rgba(200, 70, 48, 0.15),
        0 8px 20px rgba(200, 70, 48, 0.4);
}

.btn-primary::after {
    content: '印';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 24px;
    opacity: 0;
    transition: opacity 0.3s ease;
}
```

#### 次按钮 - 墨色风格
```css
.btn-secondary {
    background: linear-gradient(135deg, #2A2A2A, #1A1A1A);
    color: #F5F1E8;
    border: 1px solid #4A4A4A;
    padding: 10px 20px;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    
    /* 水墨渗透效果 */
    box-shadow: 
        0 2px 8px rgba(42, 42, 42, 0.2),
        inset 0 1px 0 rgba(255, 255, 255, 0.1);
    
    transition: all 0.3s ease;
}

.btn-secondary:hover {
    background: linear-gradient(135deg, #3A3A3A, #2A2A2A);
    box-shadow: 
        0 4px 12px rgba(42, 42, 42, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.15);
    transform: translateY(-1px);
}
```

#### 幽灵按钮 - 简约风格
```css
.btn-ghost {
    background: transparent;
    color: #6B6B6B;
    border: 1px solid transparent;
    padding: 8px 16px;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    
    transition: all 0.3s ease;
}

.btn-ghost:hover {
    color: #2A2A2A;
    border-color: rgba(42, 42, 42, 0.2);
    background: rgba(42, 42, 42, 0.05);
}
```

### 4.2 卡片（Card）

```css
.card {
    background: #F5F1E8;
    border: none;
    border-radius: 8px;
    box-shadow: 
        0 4px 16px rgba(42, 42, 42, 0.08),
        0 1px 0 rgba(42, 42, 42, 0.05);
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
}

/* 水墨晕染边框 */
.card::before {
    content: '';
    position: absolute;
    inset: 0;
    border: 2px solid transparent;
    border-image: linear-gradient(
        135deg, 
        rgba(45, 106, 79, 0.3), 
        rgba(45, 106, 79, 0.1),
        rgba(45, 106, 79, 0.05),
        transparent
    ) 1;
    border-radius: 8px;
    pointer-events: none;
}

.card:hover {
    box-shadow: 
        0 8px 24px rgba(42, 42, 42, 0.12),
        0 2px 0 rgba(42, 42, 42, 0.08);
    transform: translateY(-2px);
}

.card-header {
    padding: 20px 24px;
    border-bottom: 1px solid rgba(42, 42, 42, 0.1);
    background: linear-gradient(180deg, rgba(42, 42, 42, 0.02), transparent);
}

.card-title {
    font-size: 16px;
    font-weight: 600;
    color: #2A2A2A;
    margin-bottom: 8px;
}

.card-subtitle {
    font-size: 13px;
    color: #6B6B6B;
}

.card-body {
    padding: 24px;
}

.card-footer {
    padding: 16px 24px;
    background: rgba(42, 42, 42, 0.02);
    border-top: 1px solid rgba(42, 42, 42, 0.08);
}
```

### 4.3 统计卡片（Metric Card）

```css
.metric-card {
    background: linear-gradient(135deg, #F5F1E8, #EDE8DD);
    border: none;
    border-radius: 8px;
    padding: 24px;
    position: relative;
    overflow: hidden;
    
    /* 水墨晕染 */
    box-shadow: 
        0 8px 24px rgba(42, 42, 42, 0.08),
        inset 0 1px 0 rgba(255, 255, 255, 0.5);
    
    transition: all 0.3s ease;
}

/* 顶部装饰条 - 祥云纹样 */
.metric-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, 
        transparent, 
        rgba(45, 106, 79, 0.5), 
        rgba(45, 106, 79, 0.3),
        transparent
    );
}

/* 右上角印章装饰 */
.metric-card::after {
    content: '印';
    position: absolute;
    top: 12px;
    right: 12px;
    width: 24px;
    height: 24px;
    border: 2px solid rgba(42, 42, 42, 0.2);
    border-radius: 4px;
    color: rgba(42, 42, 42, 0.3);
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.metric-card:hover::after {
    opacity: 1;
}

.metric-card.income::before {
    background: linear-gradient(90deg, 
        transparent, 
        rgba(212, 168, 83, 0.6), 
        rgba(212, 168, 83, 0.3),
        transparent
    );
}

.metric-card.expense::before {
    background: linear-gradient(90deg, 
        transparent, 
        rgba(200, 70, 48, 0.6), 
        rgba(200, 70, 48, 0.3),
        transparent
    );
}

.metric-label {
    font-size: 11px;
    color: #6B6B6B;
    letter-spacing: 0.1em;
    text-transform: uppercase;
    margin-bottom: 16px;
}

.metric-value {
    font-size: 32px;
    font-weight: 700;
    color: #1A1A1A;
    margin-bottom: 12px;
    font-family: 'JetBrains Mono', monospace;
}

.metric-value.income {
    color: #C49A6A;
    text-shadow: 0 2px 4px rgba(196, 154, 106, 0.2);
}

.metric-value.expense {
    color: #A63820;
    text-shadow: 0 2px 4px rgba(166, 56, 32, 0.2);
}

.metric-footer {
    font-size: 12px;
    color: #4A4A4A;
}

.metric-source {
    display: flex;
    gap: 12px;
    margin-top: 12px;
    font-size: 11px;
    color: #6B6B6B;
}

.source-wechat {
    color: #2D6A4F;
}

.source-alipay {
    color: #4A7C59;
}

.source-divider {
    color: rgba(42, 42, 42, 0.2);
}
```

### 4.4 表格（Table）

```css
.table-container {
    background: #F5F1E8;
    border: none;
    border-radius: 8px;
    overflow: hidden;
    
    /* 水墨晕染 */
    box-shadow: 
        0 4px 16px rgba(42, 42, 42, 0.08),
        0 1px 0 rgba(42, 42, 42, 0.05);
}

.tech-table {
    width: 100%;
    border-collapse: collapse;
}

/* 表头 - 传统纹样装饰 */
.tech-table thead {
    background: linear-gradient(180deg, 
        rgba(45, 106, 79, 0.08), 
        rgba(45, 106, 79, 0.02)
    );
    border-bottom: 2px solid rgba(45, 106, 79, 0.3);
}

.tech-table th {
    padding: 16px;
    text-align: left;
    font-size: 12px;
    font-weight: 600;
    color: #2A2A2A;
    letter-spacing: 0.1em;
    border-right: 1px solid rgba(42, 42, 42, 0.1);
}

.tech-table tbody tr {
    border-bottom: 1px solid rgba(42, 42, 42, 0.06);
    transition: all 0.2s ease;
}

.tech-table tbody tr:hover {
    background: rgba(45, 106, 79, 0.05);
}

.tech-table tbody tr:last-child {
    border-bottom: none;
}

.tech-table td {
    padding: 14px 16px;
    font-size: 13px;
    color: #4A4A4A;
    border-right: 1px solid rgba(42, 42, 42, 0.06);
}

.tech-table td:last-child {
    border-right: none;
}

/* 数字字体 */
.font-mono {
    font-family: 'JetBrains Mono', monospace;
}
```

### 4.5 图表组件

#### 纵向柱形图 - 竹节风格
```css
.chart-container {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 8px;
    height: 100%;
}

.chart-bar {
    flex: 0 0 20px;
    border-radius: 10px 10px 4px 4px;  /* 竹节形状 */
    position: relative;
    cursor: pointer;
    transition: all 0.3s ease;
    
    /* 水墨渲染效果 */
    background: linear-gradient(180deg, 
        var(--chart-color) 0%, 
        var(--chart-color-light) 100%
    );
    box-shadow: 
        inset 0 2px 4px rgba(255, 255, 255, 0.3),
        0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 竹节纹理 */
.chart-bar::before {
    content: '';
    position: absolute;
    inset: 20% 0;
    height: 1px;
    background: rgba(255, 255, 255, 0.3);
}

.chart-bar::after {
    content: '';
    position: absolute;
    inset: 40% 0;
    height: 1px;
    background: rgba(255, 255, 255, 0.2);
}

.chart-bar:hover {
    transform: scaleY(1.05);
    filter: brightness(1.1);
}

.chart-bar.income {
    --chart-color: #D4A853;
    --chart-color-light: #E8C9A0;
}

.chart-bar.expense {
    --chart-color: #C84630;
    --chart-color-light: #D86A50;
}
```

#### 横向柱形图 - 笔触风格
```css
.chart-container.horizontal {
    flex-direction: column;
    justify-content: space-around;
    gap: 20px;
}

.chart-row {
    display: flex;
    align-items: center;
    gap: 16px;
}

.chart-row-label {
    width: 80px;
    font-size: 14px;
    color: #4A4A4A;
    font-weight: 500;
    text-align: right;
    flex-shrink: 0;
}

.chart-bar.horizontal {
    height: 20px;
    border-radius: 10px;
    flex: 1;
    min-width: 0;
    
    /* 笔触效果 */
    background: linear-gradient(90deg, 
        var(--chart-color), 
        var(--chart-color-light)
    );
    position: relative;
    
    /* 笔锋纹理 */
    mask-image: linear-gradient(90deg, 
        transparent, 
        black 2%, 
        black 98%, 
        transparent
    );
}

.chart-bar.horizontal::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 10px 0 0 10px;
}

.chart-row-value {
    width: 100px;
    font-size: 14px;
    font-family: 'JetBrains Mono', monospace;
    color: #2A2A2A;
    font-weight: 600;
    text-align: right;
    flex-shrink: 0;
}
```

#### 饼图 - 太极风格
```css
.pie-chart-container {
    position: relative;
    width: 240px;
    height: 240px;
    margin: 0 auto;
}

.pie-chart {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    position: relative;
    
    /* 水墨晕染 */
    box-shadow: 
        0 0 0 8px rgba(200, 70, 48, 0.1),
        inset 0 0 20px rgba(42, 42, 42, 0.05);
    
    background: conic-gradient(
        from 0deg,
        #C84630 0deg 90deg,    /* 朱砂 */
        #D4A853 90deg 180deg,   /* 藤黄 */
        #2D6A4F 180deg 270deg,   /* 石青 */
        #4A7C59 270deg 360deg    /* 竹青 */
    );
}

/* 太极中心 */
.pie-chart::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 80px;
    height: 80px;
    background: radial-gradient(circle, #F5F1E8, #EDE8DD);
    border-radius: 50%;
    
    /* 阴阳鱼效果 */
    box-shadow: 
        inset 0 0 20px rgba(42, 42, 42, 0.1),
        0 0 20px rgba(42, 42, 42, 0.05);
}
```

### 4.6 标签（Tag）

```css
.tag {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 14px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    
    /* 水墨晕染 */
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    
    transition: all 0.3s ease;
}

/* 古风标签色 */
.tag-success {
    background: rgba(74, 124, 89, 0.15);
    color: #4A7C59;
    border: 1px solid rgba(74, 124, 89, 0.3);
}

.tag-danger {
    background: rgba(200, 70, 48, 0.15);
    color: #C84630;
    border: 1px solid rgba(200, 70, 48, 0.3);
}

.tag-warning {
    background: rgba(212, 168, 83, 0.15);
    color: #C49A6A;
    border: 1px solid rgba(212, 168, 83, 0.3);
}

.tag-info {
    background: rgba(45, 106, 79, 0.15);
    color: #2D6A4F;
    border: 1px solid rgba(45, 106, 79, 0.3);
}

/* 装饰点 - 祥云纹 */
.tag::before {
    content: '☁';
    font-size: 10px;
    opacity: 0.6;
}
```

### 4.7 开关（Switch）

```css
.switch {
    position: relative;
    width: 48px;
    height: 24px;
    background: rgba(42, 42, 42, 0.15);
    border-radius: 12px;
    cursor: pointer;
    
    /* 墨色边框 */
    border: 2px solid #4A4A4A;
    
    transition: all 0.3s ease;
}

.switch.active {
    background: rgba(45, 106, 79, 0.2);
    border-color: #2D6A4F;
}

.switch::after {
    content: '';
    position: absolute;
    top: 2px;
    left: 2px;
    width: 18px;
    height: 18px;
    background: #F5F1E8;
    border-radius: 50%;
    
    /* 阴影效果 */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    
    transition: all 0.3s ease;
}

.switch.active::after {
    left: 24px;
    background: linear-gradient(135deg, #2D6A4F, #235A42);
}
```

---

## 五、装饰元素

### 5.1 祥云纹样

```css
/* 祥云装饰 */
.xiangyun {
    position: absolute;
    width: 120px;
    height: 40px;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 120 40'%3E%3Cpath fill='none' stroke='%232D6A4F' stroke-width='1' opacity='0.2' d='M10,30 Q30,10 60,30 Q90,50 110,30'/%3E%3Cpath fill='none' stroke='%232D6A4F' stroke-width='1' opacity='0.15' d='M20,35 Q40,15 70,35 Q100,55 115,35'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-size: contain;
}

/* 使用示例 */
.page-header::after {
    content: '';
    position: absolute;
    bottom: -20px;
    left: 50%;
    transform: translateX(-50%);
    width: 400px;
    height: 60px;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 400 60'%3E%3Cpath fill='none' stroke='%232D6A4F' stroke-width='1' opacity='0.15' d='M20,40 Q100,20 200,40 Q300,60 380,40'/%3E%3Cpath fill='none' stroke='%232D6A4F' stroke-width='1' opacity='0.1' d='M40,50 Q140,25 220,50 Q320,75 360,50'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-size: contain;
}
```

### 5.2 回纹边框

```css
/* 回纹装饰边框 */
.huawen-border {
    border: 2px solid transparent;
    border-image: repeating-linear-gradient(
        90deg,
        #2D6A4F 0px,
        #2D6A4F 10px,
        transparent 10px,
        transparent 20px
    ) 4;
    border-image-slice: 1;
}

/* 回纹卡片装饰 */
.card-huawen {
    position: relative;
}

.card-huawen::before {
    content: '';
    position: absolute;
    inset: 4px;
    border: 1px solid rgba(45, 106, 79, 0.2);
    background: linear-gradient(90deg, 
        transparent 49%, 
        rgba(45, 106, 79, 0.1) 50%, 
        transparent 51%
    );
    background-size: 20px 20px;
    pointer-events: none;
}
```

### 5.3 印章装饰

```css
/* 印章装饰 */
.seal {
    position: absolute;
    width: 60px;
    height: 60px;
    border: 3px solid #C84630;
    border-radius: 4px;
    color: #C84630;
    font-size: 14px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    
    /* 印章效果 */
    box-shadow: 
        inset 0 0 0 1px rgba(200, 70, 48, 0.1),
        0 4px 8px rgba(200, 70, 48, 0.2);
    
    /* 旋转角度 */
    transform: rotate(-5deg);
    
    /* 不透明度 */
    opacity: 0.8;
}

.seal-success {
    border-color: #4A7C59;
    color: #4A7C59;
}

.seal-large {
    width: 80px;
    height: 80px;
    font-size: 18px;
}
```

### 5.4 水墨晕染效果

```css
/* 水墨晕染背景 */
.ink-wash-bg {
    background: 
        radial-gradient(circle at 20% 30%, rgba(45, 106, 79, 0.05) 0%, transparent 50%),
        radial-gradient(circle at 80% 70%, rgba(200, 70, 48, 0.05) 0%, transparent 50%),
        radial-gradient(circle at 50% 50%, rgba(42, 42, 42, 0.02) 0%, transparent 50%),
        #F5F1E8;
}

/* 卡片水墨晕染 */
.ink-wash-card {
    position: relative;
    overflow: hidden;
}

.ink-wash-card::before {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(
        ellipse at 80% 20%, 
        rgba(45, 106, 79, 0.08) 0%, 
        transparent 50%
    );
    pointer-events: none;
}
```

---

## 六、布局设计

### 6.1 整体布局

```
┌─────────────────────────────────────────────────────┐
│  ┌─────────┬─────────────────────────────────────┐  │
│  │         │                                     │  │
│  │  侧边栏  │        主内容区                   │  │
│  │         │    (水墨晕染背景)                 │  │
│  │  毛笔   │                                     │  │
│  │  书风   │                                     │  │
│  │  导航   │                                     │  │
│  │         │                                     │  │
│  └─────────┴─────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### 6.2 侧边栏设计

```css
.sidebar {
    width: 260px;
    background: linear-gradient(180deg, #EDE8DD, #D8D2C7);
    border-right: 2px solid rgba(45, 106, 79, 0.2);
    position: relative;
    
    /* 水墨晕染 */
    box-shadow: inset 0 0 40px rgba(42, 42, 42, 0.05);
}

/* 品牌区 - 书法标题 */
.brand {
    padding: 32px 24px;
    text-align: center;
    border-bottom: 1px solid rgba(42, 42, 42, 0.1);
    position: relative;
}

/* 祥云装饰 */
.brand::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    width: 200px;
    height: 30px;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 30'%3E%3Cpath fill='none' stroke='%232D6A4F' stroke-width='1' opacity='0.2' d='M20,20 Q100,0 180,20'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-size: contain;
}

.brand-title {
    font-family: 'Ma Shan Zheng', 'STKaiti', '楷体', serif;
    font-size: 22px;
    font-weight: 700;
    color: #1A1A1A;
    margin-bottom: 8px;
    letter-spacing: 0.1em;
}

.brand-tagline {
    font-family: 'Noto Serif SC', '宋体', serif;
    font-size: 13px;
    color: #6B6B6B;
    line-height: 1.6;
}

/* 导航列表 */
.nav-list {
    padding: 20px 16px;
}

.nav-section {
    margin-bottom: 24px;
}

.nav-section-title {
    font-size: 11px;
    font-weight: 600;
    color: #6B6B6B;
    letter-spacing: 0.15em;
    text-transform: uppercase;
    padding: 0 12px;
    margin-bottom: 12px;
    
    /* 竖线装饰 */
    border-left: 2px solid rgba(45, 106, 79, 0.3);
    padding-left: 8px;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 14px;
    color: #4A4A4A;
    text-decoration: none;
    transition: all 0.3s ease;
    font-size: 14px;
    font-weight: 500;
    position: relative;
    border-radius: 4px;
    margin-bottom: 4px;
}

/* 悬停效果 - 水墨晕染 */
.nav-item:hover {
    background: rgba(45, 106, 79, 0.08);
    color: #2A2A2A;
}

/* 激活效果 - 祥云装饰 */
.nav-item.active {
    background: linear-gradient(90deg, rgba(45, 106, 79, 0.15), rgba(45, 106, 79, 0.05));
    color: #2D6A4F;
    font-weight: 600;
}

.nav-item.active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 60%;
    background: linear-gradient(180deg, #2D6A4F, #235A42);
    border-radius: 0 2px 2px 0;
}

.nav-icon {
    font-size: 16px;
}
```

### 6.3 主内容区

```css
.content {
    padding: 32px;
    background: 
        /* 宣纸底色 */
        #F5F1E8,
        /* 水墨晕染 */
        radial-gradient(circle at 80% 20%, rgba(45, 106, 79, 0.04) 0%, transparent 50%),
        radial-gradient(circle at 20% 80%, rgba(200, 70, 48, 0.04) 0%, transparent 50%),
        radial-gradient(circle at 50% 50%, rgba(42, 42, 42, 0.02) 0%, transparent 50%);
    min-height: 100vh;
    position: relative;
}

/* 页面顶部装饰 */
.page-header {
    margin-bottom: 32px;
    position: relative;
    padding-bottom: 32px;
}

/* 标题装饰 - 下划线 */
.page-header::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, #2D6A4F, transparent);
}

.page-title {
    font-family: 'Ma Shan Zheng', 'STKaiti', '楷体', serif;
    font-size: 32px;
    font-weight: 700;
    color: #1A1A1A;
    margin-bottom: 12px;
}

.page-subtitle {
    font-family: 'Noto Serif SC', '宋体', serif;
    font-size: 15px;
    color: #6B6B6B;
    line-height: 1.6;
}
```

---

## 七、页面设计

### 7.1 首页分析（Dashboard）

#### 布局结构
```
┌─────────────────────────────────────────────────────┐
│  ◇ 总览                                              │
│  首页分析              [年份] [月份] [刷新印章]          │
│  查看年度与月度的收入支出情况...                     │
├─────────────────────────────────────────────────────┤
│  [年度收入] [年度支出] [月度收入] [月度支出]          │
│  印章装饰 / 水墨晕染                                  │
├─────────────────────────────────────────────────────┤
│  [年度收支趋势图]      [年度支出分类图]              │
│  竹节柱形图            横向笔触图                    │
├─────────────────────────────────────────────────────┤
│  [月度支出分类]        [月度每日趋势]                  │
│  太极饼图              水墨渲染图                    │
├─────────────────────────────────────────────────────┤
│  [年度Top10]           [月度Top10]                    │
│  宣纸卡片 / 印章装饰                                 │
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
    padding: 32px;
    background: linear-gradient(135deg, 
        rgba(45, 106, 79, 0.08) 0%, 
        rgba(45, 106, 79, 0.02) 100%
    );
    border: 2px solid rgba(45, 106, 79, 0.2);
    border-radius: 12px;
    position: relative;
    
    /* 水墨晕染 */
    box-shadow: 
        0 8px 24px rgba(42, 42, 42, 0.08),
        inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

/* 四角装饰 - 云纹 */
.dashboard-hero::before {
    content: '';
    position: absolute;
    top: 10px;
    left: 10px;
    width: 40px;
    height: 40px;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'%3E%3Cpath fill='none' stroke='%232D6A4F' stroke-width='1' opacity='0.15' d='M10,30 Q20,10 30,30'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
}

.hero-kicker {
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.2em;
    color: #2D6A4F;
    margin-bottom: 16px;
    
    /* 印章装饰 */
    padding-left: 16px;
    position: relative;
}

.hero-kicker::before {
    content: '◆';
    position: absolute;
    left: 0;
    color: #2D6A4F;
    opacity: 0.5;
}

.hero-title {
    font-family: 'Ma Shan Zheng', 'STKaiti', '楷体', serif;
    font-size: 32px;
    font-weight: 700;
    color: #1A1A1A;
    margin-bottom: 12px;
    letter-spacing: 0.1em;
}

.hero-copy {
    font-family: 'Noto Serif SC', '宋体', serif;
    font-size: 14px;
    color: #4A4A4A;
    max-width: 600px;
    line-height: 1.8;
}
```

### 7.2 筛选器组件

```css
.filter-group {
    display: flex;
    gap: 16px;
    align-items: center;
}

.filter-item {
    display: flex;
    align-items: center;
    gap: 8px;
}

.filter-label {
    font-size: 13px;
    font-weight: 500;
    color: #4A4A4A;
    white-space: nowrap;
}

/* 古风下拉框 */
.filter-select {
    padding: 8px 32px 8px 14px;
    font-size: 13px;
    font-family: 'Noto Serif SC', '宋体', serif;
    color: #2A2A2A;
    background: #F5F1E8;
    border: 2px solid rgba(42, 42, 42, 0.2);
    border-radius: 4px;
    cursor: pointer;
    appearance: none;
    
    /* 装饰箭头 */
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='none' stroke='%234A4A4A' stroke-width='1.5' d='M3,5 L6,8 L9,5'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 12px center;
    
    transition: all 0.3s ease;
}

.filter-select:focus {
    outline: none;
    border-color: #2D6A4F;
    box-shadow: 0 0 0 3px rgba(45, 106, 79, 0.1);
}

/* 刷新按钮 - 印章风格 */
.btn-refresh {
    width: 60px;
    height: 60px;
    border: 2px solid #2D6A4F;
    border-radius: 4px;
    background: transparent;
    color: #2D6A4F;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    
    /* 印章效果 */
    box-shadow: 
        inset 0 0 0 1px rgba(45, 106, 79, 0.1),
        0 4px 8px rgba(45, 106, 79, 0.2);
    
    transition: all 0.3s ease;
}

.btn-refresh:hover {
    transform: translateY(-2px) rotate(-3deg);
    box-shadow: 
        inset 0 0 0 2px rgba(45, 106, 79, 0.15),
        0 8px 16px rgba(45, 106, 79, 0.3);
}

.btn-refresh::after {
    content: '刷新';
    display: block;
    margin-top: -2px;
    font-size: 12px;
}
```

---

## 八、特殊效果

### 8.1 水墨扩散动画

```css
@keyframes inkSpread {
    0% {
        transform: scale(0);
        opacity: 0.8;
    }
    100% {
        transform: scale(1.5);
        opacity: 0;
    }
}

.ink-spread {
    animation: inkSpread 0.8s ease-out;
}

/* 按钮点击时的水墨扩散效果 */
.btn-click-effect {
    position: relative;
    overflow: hidden;
}

.btn-click-effect::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(45, 106, 79, 0.3);
    transform: translate(-50%, -50%);
    transition: all 0.6s ease-out;
}

.btn-click-effect:active::before {
    width: 200px;
    height: 200px;
}
```

### 8.2 毛笔写字动画

```css
@keyframes brushWrite {
    0% {
        stroke-dasharray: 1000;
        stroke-dashoffset: 1000;
    }
    100% {
        stroke-dasharray: 1000;
        stroke-dashoffset: 0;
    }
}

.brush-text {
    fill: transparent;
    stroke: #1A1A1A;
    stroke-width: 1;
    animation: brushWrite 2s ease-in-out;
}
```

### 8.3 印章盖章动画

```css
@keyframes sealStamp {
    0% {
        transform: scale(2) rotate(-20deg);
        opacity: 0;
    }
    50% {
        transform: scale(1) rotate(-5deg);
        opacity: 1;
    }
    100% {
        transform: scale(1) rotate(-5deg);
        opacity: 0.8;
    }
}

.seal-animate {
    animation: sealStamp 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}
```

### 8.4 云朵飘动动画

```css
@keyframes cloudFloat {
    0%, 100% {
        transform: translateX(0);
    }
    50% {
        transform: translateX(20px);
    }
}

.cloud {
    animation: cloudFloat 8s ease-in-out infinite;
}
```

---

## 九、响应式设计

```css
/* 平板 */
@media (max-width: 1024px) {
    .app-shell {
        grid-template-columns: 220px 1fr;
    }
    
    .summary-grid {
        grid-template-columns: repeat(2, 1fr);
    }
    
    .chart-grid {
        grid-template-columns: 1fr;
    }
}

/* 手机 */
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
    
    .dashboard-hero {
        flex-direction: column;
        align-items: stretch;
    }
    
    .hero-form {
        width: 100%;
    }
}
```

---

## 十、实施建议

### 10.1 技术方案

#### CSS实现
- 使用 CSS Variables 定义色彩系统
- 使用 CSS Gradient 实现水墨效果
- 使用 SVG Data URI 实现装饰图案
- 使用 Mask 实现特殊纹理

#### 字体加载
```javascript
// Google Fonts 引入
import '@fontsource/ma-shan-zheng';
import '@fontsource/noto-serif-sc';

// 或使用本地字体
// '楷体', '宋体', '黑体'
```

#### 背景纹理
```javascript
// 使用 Canvas 生成宣纸纹理
function createPaperTexture() {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    
    // 纸张底色
    ctx.fillStyle = '#F5F1E8';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // 添加纹理噪点
    for (let i = 0; i < 10000; i++) {
        const x = Math.random() * canvas.width;
        const y = Math.random() * canvas.height;
        const opacity = Math.random() * 0.02;
        ctx.fillStyle = `rgba(42, 42, 42, ${opacity})`;
        ctx.fillRect(x, y, 1, 1);
    }
    
    return canvas.toDataURL();
}
```

### 10.2 实施优先级

**阶段一：基础样式（2天）**
1. 创建水墨风格 CSS 变量文件
2. 更新全局样式和背景
3. 实现基础组件（按钮、卡片）

**阶段二：页面布局（3天）**
1. 实现首页分析布局
2. 实现账单明细页面
3. 实现其他辅助页面

**阶段三：装饰元素（2天）**
1. 添加祥云、回纹装饰
2. 实现印章效果
3. 添加水墨晕染动画

**阶段四：优化打磨（1天）**
1. 字体加载优化
2. 动画性能优化
3. 响应式适配
4. 主题切换功能（水墨/现代）

**总计：7-8个工作日**

---

## 十一、主题切换

### 11.1 双主题支持

```css
/* 默认主题：水墨古风 */
:root {
    --bg-primary: #F5F1E8;
    --text-primary: #1A1A1A;
    --accent-color: #2D6A4F;
}

/* 现代主题：明亮科技 */
[data-theme="modern"] {
    --bg-primary: #f8fafc;
    --text-primary: #334155;
    --accent-color: #3b82f6;
}

/* 主题切换 */
.theme-toggle {
    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 1000;
}
```

### 11.2 过渡动画

```css
/* 主题切换过渡 */
* {
    transition: 
        background-color 0.3s ease,
        color 0.3s ease,
        border-color 0.3s ease,
        box-shadow 0.3s ease;
}
```

---

## 十二、设计规范总结

### 12.1 核心设计元素

| 元素 | 设计风格 | 关键特征 |
|------|----------|----------|
| 背景 | 宣纸质感 | 浅米色 + 纹理噪点 |
| 色彩 | 五色体系 | 石青、朱砂、藤黄、墨色 |
| 字体 | 书法字体 | 楷体、宋体 |
| 按钮 | 印章风格 | 方形印章 + 阴影 |
| 图表 | 竹节风格 | 圆润竹节 + 水墨渲染 |
| 装饰 | 传统纹样 | 祥云、回纹、印章 |
| 动画 | 水墨效果 | 扩散、渗透、飘动 |

### 12.2 设计理念实现

**"水墨丹青 · 古韵新辉"**

1. **水墨渲染**：所有卡片使用水墨晕染效果
2. **毛笔字**：标题使用书法字体
3. **传统色彩**：基于中国传统五色观
4. **古风装饰**：祥云、回纹、印章等元素
5. **现代内核**：保持数据清晰可读性
6. **雅致为上**：装饰适度，不喧宾夺主

### 12.3 可访问性

- 保持足够的颜色对比度（符合 WCAG AA 标准）
- 提供主题切换功能
- 支持键盘导航
- 字体大小适中

---

**文档版本**: v1.0  
**创建日期**: 2026-05-12  
**设计风格**: 水墨古风  
**状态**: 设计完成，待实施
