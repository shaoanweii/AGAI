<script setup lang="ts">
import { ref } from 'vue'
import CategoryList from './categoryList.vue'
import RuleList from './ruleList.vue'
import { singleEventActions } from './store'

const categoryListRef = ref()
const ruleListRef = ref()

/**
 * 初始化单点事件页面依赖的公共字典与树结构，避免子组件重复请求。
 */
const init = () => {
  singleEventActions.initPageResources()
}

init()

/**
 * 处理左侧分类切换，将当前分类同步给规则列表。
 * @param item 当前点击的分类项
 */
const categoryItemClick = (item: any) => {
  if (ruleListRef.value) {
    ruleListRef.value.leftChange(item)
  }
}

/**
 * 规则列表变更后刷新分类列表，保证统计数据与列表状态一致。
 */
const refresh = () => {
  if (categoryListRef.value) {
    categoryListRef.value.refreshList(false)
  }
}
</script>

<template>
  <div class="page-container flex-col h-full">
    <div class="flex gap-16 h-full">
      <CategoryList
        ref="categoryListRef"
        @category-item-click="categoryItemClick"
        class="left-class card-class pr-24"
      />
      <RuleList
        ref="ruleListRef"
        class="card-class flex-1 h-full overflow-hidden"
        @refresh="refresh"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
$border-dark: #dfe2e8; // 分割线/描边/阴影

.gap-16 {
  gap: 16px;
}

.card-class {
  padding: 16px 24px;
  background: #ffffff;
  box-shadow: 0 1px 2px 0 rgba(10, 13, 18, 0.05);
  border-radius: 8px;
}

.left-class {
  width: 352px;
}
</style>
