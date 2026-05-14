<template>
  <div class='app-shell'>
    <aside class='sidebar'>
      <div class='brand'>
        <div class='brand-logo'>
          <img src='/zhangshuo.png' alt='帐里说帐Logo' class='logo-image' />
          <div class='logo-seal-border'></div>
        </div>
        <p class='brand-eyebrow'>◆ 个人账单分析</p>
        <h1 class='brand-title'>帐里说帐</h1>
        <p class='brand-tagline'>一个属于我的账单助手</p>
      </div>
      <nav class='nav-list'>
        <div class='nav-section'>
          <p class='nav-section-title'>核心功能</p>
          <RouterLink to='/dashboard' class='nav-item'><span class='nav-icon'>📊</span><span>说帐总览</span></RouterLink>
          <RouterLink to='/bills' class='nav-item'><span class='nav-icon'>💳</span><span>逐笔说帐</span></RouterLink>
          <RouterLink to='/imports' class='nav-item'><span class='nav-icon'>📥</span><span>导入说帐</span></RouterLink>
          <RouterLink to='/annual-report' class='nav-item'><span class='nav-icon'>📜</span><span>年度账本</span></RouterLink>
        </div>
        <div class='nav-section'>
          <p class='nav-section-title'>智能分类</p>
          <RouterLink to='/classifier-tasks' class='nav-item'><span class='nav-icon'>🤖</span><span>自动说帐</span></RouterLink>
          <RouterLink to='/review-bills' class='nav-item'><span class='nav-icon'>✅</span><span>说帐复核</span></RouterLink>
          <RouterLink to='/failed-bills' class='nav-item'><span class='nav-icon'>⚠️</span><span>说帐疑问</span></RouterLink>
          <RouterLink to='/categories' class='nav-item'><span class='nav-icon'>🏷️</span><span>说帐分类</span></RouterLink>
          <RouterLink to='/keyword-rules' class='nav-item'><span class='nav-icon'>🔑</span><span>关键字规则</span></RouterLink>
        </div>
        <div class='nav-section'>
          <p class='nav-section-title'>系统</p>
          <RouterLink to='/classifier-sync-logs' class='nav-item'><span class='nav-icon'>📋</span><span>同步日志</span></RouterLink>
          <RouterLink to='/classifier-sync-test' class='nav-item'><span class='nav-icon'>🔧</span><span>同步调试</span></RouterLink>
          <RouterLink to='/guide' class='nav-item'><span class='nav-icon'>📖</span><span>使用指南</span></RouterLink>
        </div>
      </nav>
    </aside>
    <main class='content'>
      <RouterView :key="refreshKey" />
    </main>

    <!-- 快速记账浮动按钮 -->
    <QuickBillFab @refresh="handleRefresh" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink, RouterView } from 'vue-router'
import QuickBillFab from './components/QuickBillFab.vue'

const refreshKey = ref(0)

const handleRefresh = () => {
  // 通过改变key强制刷新RouterView
  refreshKey.value++
}
</script>
