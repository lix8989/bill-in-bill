import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: () => import('../views/DashboardView.vue') },
  { path: '/bills', component: () => import('../views/BillsView.vue') },
  { path: '/bills-prototype', redirect: '/bills' },
  { path: '/categories', component: () => import('../views/CategoriesView.vue') },
  { path: '/imports', component: () => import('../views/ImportsView.vue') },
  { path: '/classifier-sync-logs', component: () => import('../views/ClassifierSyncLogsView.vue') },
  { path: '/classifier-sync-test', component: () => import('../views/ClassifierSyncTestView.vue') },
  { path: '/classifier-tasks', component: () => import('../views/ClassifyTasksView.vue') },
  { path: '/review-bills', component: () => import('../views/ReviewBillsView.vue') },
  { path: '/failed-bills', component: () => import('../views/FailedBillsView.vue') },
  { path: '/keyword-rules', component: () => import('../views/KeywordRulesView.vue') },
  { path: '/annual-report', component: () => import('../views/AnnualReportView.vue') },
  { path: '/guide', component: () => import('../views/GuideView.vue') },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
