<script setup lang="ts">
import { onMounted } from 'vue'
import CategoryPanel from './components/CategoryPanel.vue'
import RuleList from './components/RuleList.vue'
import { batchEventActions } from './store'
import { provideBatchEventContext } from './useBatchEventContext'

provideBatchEventContext()

/**
 * 页面初始化时加载所有资源，表单弹窗直接复用，避免重复请求。
 */
onMounted(async () => {
  await batchEventActions.initPageResources()
})
</script>

<template>
  <div class="page-container flex-col h-full">
    <div class="batch-event-layout flex gap-16 h-full">
      <CategoryPanel class="left-class" />
      <RuleList class="card-class right-class flex-1 h-full" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.gap-16 {
  gap: 16px;
}

.batch-event-layout {
  min-width: 0;
}

.card-class {
  padding: 16px 24px;
  background: #ffffff;
  box-shadow: 0 1px 2px 0 rgba(10, 13, 18, 0.05);
  border-radius: 8px;
  min-height: 0;
}

.left-class {
  // 左侧分类列固定占位，避免窄屏时被右侧表格继续挤压。
  height: 100%;
  flex: 0 0 320px;
  min-width: 320px;
}

.right-class {
  // 右侧规则区允许收缩，把横向溢出留给表格自身处理。
  min-width: 0;
  overflow: hidden;
}
</style>
