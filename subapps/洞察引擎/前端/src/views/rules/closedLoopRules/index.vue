<script setup lang="ts">
import useMiddlewareStore from '@/stores/modules/middleware'
import { ClosedLoopRulesTab } from '@/constant'
import SingleEvent from './SingleEvent/index.vue'
import BatchEvent from './BatchEvent/index.vue'

defineOptions({
  name: 'rulesClosedLoopRules'
})

// 闭环规则父页面仅负责承接头部页签状态并切换具体业务页面。
const middlewareStore = useMiddlewareStore()
</script>

<template>
  <div class="closed-loop-rules-page flex-col h-full">
    <SingleEvent v-if="middlewareStore.closedLoopRulesType === ClosedLoopRulesTab.SINGLE" />
    <BatchEvent v-else class="flex-1" />
  </div>
</template>

<style lang="scss" scoped></style>
