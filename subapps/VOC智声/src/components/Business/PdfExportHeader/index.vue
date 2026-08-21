<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  name: 'PdfExportHeader'
})

interface Props {
  title: string
  startDate?: string
  endDate?: string
}

const props = defineProps<Props>()

const dateRangeText = computed(() => {
  if (props.startDate && props.endDate) {
    return `${props.startDate} 至 ${props.endDate}`
  }

  return ''
})
</script>

<template>
  <div class="pdf-export-header" data-page-export-only data-page-export-display="block">
    <div class="pdf-export-header__title">{{ title }} <span v-if="dateRangeText" class="pdf-export-header__meta">{{ dateRangeText }}</span></div>
  </div>
</template>

<style lang="scss" scoped>
.pdf-export-header {
  display: none;
  margin-bottom: 24px;

  &__title {
    color: #1f2733;
    font-size: 28px;
    font-weight: 600;
    line-height: 36px;
  }

  &__meta {
    margin-left: 20px;
    margin-top: 8px;
    color: #333;
    font-size: 16px;
    line-height: 22px;
  }
}
</style>
