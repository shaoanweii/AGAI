<script setup lang="ts">
import type { H5DataSquareReportItem } from '@h5/api/dataSquare'
import squareFileIcon from '@/assets/svg/h5/square-file.svg'

defineOptions({
  name: 'DataSquareReportRow'
})

defineProps<{
  report: H5DataSquareReportItem
}>()

const emit = defineEmits<{
  click: [report: H5DataSquareReportItem]
}>()

/**
 * 触发报告点击，由页面决定跳转或预览行为。
 * @param report 当前报告数据
 */
const handleClick = (report: H5DataSquareReportItem) => {
  emit('click', report)
}
</script>

<template>
  <button class="report-row" type="button" @click="handleClick(report)">
    <span class="report-row__icon">
      <img :src="squareFileIcon" alt="" class="report-row__icon-img" />
    </span>
    <span class="report-row__name">{{ report.reportName || '' }}</span>
  </button>
</template>

<style scoped lang="scss">
.report-row {
  width: 100%;
  height: 38px;
  padding: 0 8px;
  border: 0;
  border-radius: 8px;
  background: linear-gradient( 90deg, #EBF4FD 0%, rgba(235,244,253,0.2) 100%);
  display: flex;
  align-items: center;
  text-align: left;
  color: #1f2733;
  font-size: 12px;
  line-height: 18px;
  font-weight: 500;
  overflow: hidden;

  &:active {
    background: #e9f3ff;
  }

  &__icon {
    width: 16px;
    height: 16px;
    margin-right: 8px;
    border-radius: 3px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    background: #237bff;
    color: #ffffff;
    font-size: 12px;

    &-img {
      width: 100%;
      height: 100%;
      object-fit: contain;
      display: block;
    }
  }

  &__name {
    min-width: 0;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-weight: 500;
    font-size: 12px;
    color: #1F2733;
    line-height: 20px;
  }
}
</style>
