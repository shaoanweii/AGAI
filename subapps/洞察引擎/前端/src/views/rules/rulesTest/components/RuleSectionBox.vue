<script setup lang="ts">
// 通用的“内容灰底卡片”组件，渲染若干行文本（已排好文案，包含`：`等符号）
const props = defineProps<{
  lines: string[]
}>()
</script>

<template>
  <div class="section-box">
    <div v-for="(line, idx) in props.lines" :key="idx" class="line">{{ line }}</div>
  </div>
</template>

<style scoped lang="scss">
.section-box {
  background: #f5f7fa;
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #dde3ee;
  padding: 24px;
}
.line {
  white-space: pre-wrap;
  font-weight: 400;
  font-size: 14px;
  color: #1f2733;
  line-height: 20px;
}
</style>
