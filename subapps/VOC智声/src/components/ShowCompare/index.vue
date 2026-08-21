<!-- 显示差距（环比，同比）组件 -->
<template>
  <div class="show-compare">
    <!-- 提及数 -->
    <div v-if="compareKey === 'momTotalMentionValueRate'" class="flex items-center">
      <template v-if="numericValue > 0">
        <el-icon><CaretTop /></el-icon>
        <span class="value" :class="customClass">{{ fmtPer(compareValue) }} </span>
      </template>
      <template v-if="numericValue < 0">
        <el-icon class="red"><CaretBottom /></el-icon>
        <span class="value" :class="customClass">{{ fmtPer(compareValue) }} </span>
      </template>
      <template v-if="numericValue == 0">
        <span class="value" :class="customClass">{{ fmtPer(compareValue) }} </span>
      </template>
    </div>

    <!-- 体验值 -->
    <div v-if="compareKey === 'momExperienceValueRate'" class="flex items-center">
      <template v-if="numericValue > 0">
        <el-icon class="green"><CaretTop /></el-icon>
        <span class="value green" :class="customClass">{{ fmtPer(compareValue) }} </span>
      </template>
      <template v-if="numericValue < 0">
        <el-icon class="red"><CaretBottom /></el-icon>
        <span class="value red" :class="customClass">{{ fmtPer(compareValue) }} </span>
      </template>
      <template v-if="numericValue == 0">
        <span class="value" :class="customClass">{{ fmtPer(compareValue) }} </span>
      </template>
    </div>

    <!-- 负面提及率 -->
    <div v-if="compareKey === 'momNegativeMentionRate'" class="flex items-center">
      <template v-if="numericValue > 0">
        <el-icon class="red"><CaretTop /></el-icon>
        <span class="value red" :class="customClass">{{ fmtPer(compareValue) }} </span>
      </template>
      <template v-if="numericValue < 0">
        <el-icon class="green"><CaretBottom /></el-icon>
        <span class="value green" :class="customClass">{{ fmtPer(compareValue) }} </span>
      </template>
      <template v-if="numericValue == 0">
        <span class="value" :class="customClass">{{ fmtPer(compareValue) }} </span>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ShowCompareProps } from './types'
import { fmtNum, fmtPer , fmtFix } from '@/utils'


// 组件名称定义
defineOptions({
  name: 'ShowCompare'
})

// Props 定义
const props = withDefaults(defineProps<ShowCompareProps>(), {
  compareKey: 'momTotalMentionValueRate',
  compareValue: '',
  customClass: ''
})

// 计算数值类型用于比较
const numericValue = computed(() => {
  const value = parseFloat(props.compareValue as string)
  return isNaN(value) ? 0 : value
})

// 精确两位小数
// const toFixTwo = (data: number) => {
//   const result =
//     parseFloat(data.toString()).toString() == 'NaN' ? '-' : parseFloat(data.toString()).toFixed(1)
//   return result
// }

// // 数据千分位显示
// const Thousandth = (num: number) => {
//   if (num != undefined) {
//     var reg = /\d{1,3}(?=(\d{3})+$)/g
//     return (num + '').replace(reg, '$&,')
//   }
//   return '-'
// }

/**
 * 格式化百分比数值
 * @param data 原始数值
 * @returns 格式化后的百分比字符串
 */
// const formatPercent = (data: number | string): string => {
//   let numValue = parseFloat(data as string)

//   if (isNaN(numValue)) {
//     return '-'
//   }

//   // 转换为百分比
//   // numValue = numValue * 100

//   // 保留两位小数
//   numValue = toFixTwo(numValue) as unknown as number

//   // 添加千分位分隔符
//   numValue = Thousandth(numValue) as unknown as number

//   return numValue.toString()
// }
</script>

<style lang="scss" scoped>
.show-compare {
  position: relative;
  display: inline-block;
  vertical-align: middle;

  .font {
    color: rgba(0, 0, 0, 0.75);
    font-weight: 500;
    font-size: 12px;
  }
  .value {
    font-size: 14px;
    font-weight: 400;
    color: rgba(0, 0, 0, 0.75);
    line-height: 17px;
  }

  .green {
    color: #52c718 !important;
  }

  .red {
    color: #ff4a4d !important;
  }

  :deep(.el-icon-caret-bottom) {
    color: rgba(0, 0, 0, 0.5);
  }
}
</style>
