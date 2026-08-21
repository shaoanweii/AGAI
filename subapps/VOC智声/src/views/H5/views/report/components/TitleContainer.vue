<script setup lang="ts">/**
 * 容器：带左右结构标题（左：SortNum + 标题；右：数字 + 百分比）
 * - 背景提供两种渐变可选
 * - 标题与数值样式遵循视觉规范
 */
import {computed} from "vue";
import { formatRatePrefix } from '@/utils'

defineOptions({ name: 'ReportTitleContainer' })

type BgVariant = 'blue' | 'green'

const props = defineProps<{
  /** 标题文本 */
  title: string
  /** 排名（用于 SortNum），接口为 null 时显示 '-' 由 SortNum 处理 */
  rank: number | null
  /** 右侧普通数字（如 656） */
  number?: string | number
  /** 右侧百分比（如 32.96 或 -10），显示时自动添加正负号与 % */
  percent?: string | number
  /** 背景渐变：'blue' | 'green' */
  bg?: BgVariant
}>()

/**
 * 数值格式化仅做展示，不改变原始数据
 */
const displayNumber = computed(() => {
  const n = props.number
  return n === undefined || n === null ? '' : String(n)
})

const displayPercent = computed(() => {
  const p = props.percent
  if (p === undefined || p === null || p === '') return ''
  const num = typeof p === 'number' ? p : Number(p)
  if (Number.isNaN(num)) return String(p)
  // 统一：百分比保留一位小数，并在正数前加“+”
  return `${formatRatePrefix(num)}%`
})

const variantClass = computed(() => {
  const v = props.bg ?? 'blue'
  return v === 'green' ? 'report-title-card--bg-green' : 'report-title-card--bg-blue'
})

/**
 * 百分比颜色状态：小于 0 标绿，大于 0 标红
 */
const isNegativePercent = computed(() => {
  const p = props.percent
  if (p === undefined || p === null || p === '') return false
  const num = typeof p === 'number' ? p : Number(p)
  return !Number.isNaN(num) && num < 0
})

const isPositivePercent = computed(() => {
  const p = props.percent
  if (p === undefined || p === null || p === '') return false
  const num = typeof p === 'number' ? p : Number(p)
  return !Number.isNaN(num) && num > 0
})
</script>

<template>
  <div class="report-title-card" :class="variantClass">
    <div class="report-title-card__header">
      <div class="report-title-card__left">
        <SortNum :rank="rank" />
        <div class="report-title-card__title">{{ title }}</div>
      </div>
      <div class="report-title-card__right">
        <div class="report-title-card__number">{{ displayNumber }}</div>
        <div
          class="report-title-card__percent"
          :class="{
            'report-title-card__percent--negative': isNegativePercent,
            'report-title-card__percent--positive': isPositivePercent
          }"
        >{{ displayPercent }}</div>
      </div>
    </div>
    <div class="report-title-card__body">
      <slot />
    </div>
  </div>

</template>

<style scoped lang="scss">
/* 外层容器：两种渐变背景、圆角与内边距 */
.report-title-card {
  border-radius: 8px;
  padding: 16px 12px;
}

.report-title-card--bg-blue {
  background: linear-gradient(201deg, #C5E8FA 0%, #FFFFFF 100%);
}

.report-title-card--bg-green {
  background: linear-gradient(203deg, #CCF3EF 0%, #FFFFFF 100%);
}

/* 标题区域：左右结构 */
.report-title-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.report-title-card__left {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.report-title-card__title {
  font-weight: 500;
  font-size: 15px;
  color: #1F2733;
  text-align: center;
  font-style: normal;
  text-transform: none;
}

.report-title-card__right {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

/* 普通数字样式 */
.report-title-card__number {
  font-weight: 500;
  font-size: 14px;
  color: #1F2733;
  line-height: 20px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}

/* 百分比文本样式（默认深色） */
.report-title-card__percent {
  font-weight: 500;
  font-size: 14px;
  color: #1F2733;
  line-height: 24px;
  text-align: center;
  font-style: normal;
  text-transform: none;
}

/* 百分比为负：绿色 */
.report-title-card__percent--negative {
  color: #14CA64;
}

/* 百分比为正：红色 */
.report-title-card__percent--positive {
  color: #E5484D;
}

.report-title-card__body {
  margin-top: 12px;
}
</style>
