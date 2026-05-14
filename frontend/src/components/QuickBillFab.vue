<template>
  <div class="quick-bill-fab" @mouseenter="expand" @mouseleave="collapse">
    <div class="fab-container" :class="{ expanded: isExpanded }">
      <button
        class="fab-button btn-seal-red"
        @click="showDialog = true"
      >
        <span class="fab-icon">✚</span>
        <span class="fab-text" v-show="isExpanded">补记一笔</span>
      </button>
    </div>

    <QuickBillDialog
      v-model="showDialog"
      @success="handleBillSuccess"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import QuickBillDialog from './QuickBillDialog.vue'

const isExpanded = ref(false)
const showDialog = ref(false)
const expandTimer = ref(null)
const collapseTimer = ref(null)

const expand = () => {
  clearTimeout(collapseTimer.value)
  expandTimer.value = setTimeout(() => {
    isExpanded.value = true
  }, 100)
}

const collapse = () => {
  clearTimeout(expandTimer.value)
  collapseTimer.value = setTimeout(() => {
    isExpanded.value = false
  }, 300)
}

const emit = defineEmits(['refresh'])

const handleBillSuccess = () => {
  emit('refresh')
}
</script>

<style scoped>
.quick-bill-fab {
  position: fixed;
  right: 0;
  bottom: 80px;
  z-index: 1000;
}

.fab-container {
  display: flex;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fab-button {
  width: 48px;
  height: 48px;
  padding: 0;
  border-radius: 12px 0 0 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  overflow: hidden;
  background: #C84630;
  color: #FFFFFF;
  border: none;
  box-shadow:
    0 4px 12px rgba(200, 70, 48, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  font-family: 'KaiTi', 'STKaiti', '楷体', serif;
  letter-spacing: 0.1em;
  cursor: pointer;
}

.fab-container.expanded .fab-button {
  width: 140px;
  padding: 0 20px;
  gap: 8px;
  border-radius: 12px;
}

.fab-button:hover {
  transform: translateY(-2px);
  box-shadow:
    0 6px 16px rgba(200, 70, 48, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.fab-button:focus {
  outline: none;
  box-shadow:
    0 4px 12px rgba(200, 70, 48, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2),
    0 0 0 3px rgba(200, 70, 48, 0.3);
}

.fab-icon {
  font-size: 18px;
  line-height: 1;
  flex-shrink: 0;
  transition: font-size 0.3s ease;
}

.fab-container.expanded .fab-icon {
  font-size: 20px;
}

.fab-text {
  font-size: 16px;
  font-weight: bold;
  white-space: nowrap;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.2s ease 0.1s;
}

.fab-container.expanded .fab-text {
  opacity: 1;
  transform: translateX(0);
}
</style>
