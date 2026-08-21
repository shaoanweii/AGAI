<script setup lang="ts">
interface ReportInterpretationPanelProps {
  text?: string
  loading?: boolean
}

const props = defineProps<ReportInterpretationPanelProps>()
</script>

<template>
  <div class="report-interpretation-panel">
    <van-loading v-if="props.loading && !props.text" size="22px" vertical>
      报告解读生成中...
    </van-loading>
    <template v-else>{{ props.text || '暂无报告解读' }}</template>
  </div>
</template>

<style scoped lang="scss">
.report-interpretation-panel {
  padding: 10px 12px;
  min-height: 96px;
  border: 1px solid #85d4f7;
  border-radius: 8px;
  background: #f7fcff;
  box-shadow: inset 0 0 0 1px rgba(133, 212, 247, 0.24);
  font-size: 13px;
  line-height: 24px;
  color: #28313f;
  text-align: justify;
  word-break: break-word;
  white-space: pre-wrap;
}

.report-interpretation-panel :deep(.van-loading) {
  min-height: 74px;
  align-items: center;
  justify-content: center;
}
</style>
